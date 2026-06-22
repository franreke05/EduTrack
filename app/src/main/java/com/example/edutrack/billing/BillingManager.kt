package com.example.edutrack.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// IDs reales de Google Play Console — registrar en Play Console antes del launch.
// TODO: reemplazar por los IDs definitivos cuando estén aprobados en Play Console.
const val PRODUCT_ID_MONTHLY = "edutrack_premium_monthly"
const val PRODUCT_ID_ANNUAL = "edutrack_premium_annual"

private const val PACKAGE_NAME = "com.edutrack.app"

interface BillingManagerListener {
    fun onBillingConnected()
    fun onBillingDisconnected()
    fun onPurchaseSuccess(productId: String, purchaseToken: String)
    fun onPurchaseError(errorCode: Int, errorMessage: String)
    fun onPurchaseCanceled()
}

class BillingManager(
    private val context: Context,
    private val listener: BillingManagerListener
) : PurchasesUpdatedListener, BillingClientStateListener {

    private var billingClient: BillingClient? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    private val functions = FirebaseFunctions.getInstance()

    fun connect() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()
        billingClient?.startConnection(this)
    }

    fun disconnect() {
        billingClient?.endConnection()
        billingClient = null
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            listener.onBillingConnected()
            queryPurchases()
        } else {
            listener.onBillingDisconnected()
        }
    }

    override fun onBillingServiceDisconnected() {
        listener.onBillingDisconnected()
    }

    fun launchBillingFlow(activity: Activity, productId: String) {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
            )
            .build()

        billingClient?.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK ||
                productDetailsList.isEmpty()) {
                listener.onPurchaseError(billingResult.responseCode, billingResult.debugMessage)
                return@queryProductDetailsAsync
            }
            val productDetails = productDetailsList[0]
            val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
                ?: run {
                    listener.onPurchaseError(-1, "Sin oferta disponible para $productId")
                    return@queryProductDetailsAsync
                }
            val flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .setOfferToken(offerToken)
                            .build()
                    )
                )
                .build()
            billingClient?.launchBillingFlow(activity, flowParams)
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        when {
            billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null -> {
                purchases.forEach { purchase ->
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        handlePurchase(purchase)
                    }
                }
            }
            billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED ->
                listener.onPurchaseCanceled()
            else ->
                listener.onPurchaseError(billingResult.responseCode, billingResult.debugMessage)
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        scope.launch {
            val ackParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient?.acknowledgePurchase(ackParams) { result ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    val productId = purchase.products.firstOrNull() ?: return@acknowledgePurchase
                    verifyWithServer(productId, purchase.purchaseToken)
                } else {
                    listener.onPurchaseError(result.responseCode, "Error al confirmar compra")
                }
            }
        }
    }

    // Envía el token al backend para validación server-side.
    // NUNCA concede Premium localmente — solo el backend escribe premiumCache.
    private fun verifyWithServer(productId: String, purchaseToken: String) {
        val payload = hashMapOf(
            "packageName" to PACKAGE_NAME,
            "productId" to productId,
            "purchaseToken" to purchaseToken
        )
        functions.getHttpsCallable("verifyPremiumPurchase")
            .call(payload)
            .addOnSuccessListener { result ->
                @Suppress("UNCHECKED_CAST")
                val data = result.data as? Map<String, Any>
                val isPremium = data?.get("isPremium") as? Boolean ?: false
                if (isPremium) {
                    listener.onPurchaseSuccess(productId, purchaseToken)
                } else {
                    listener.onPurchaseError(-1, data?.get("message") as? String ?: "Verificación fallida")
                }
            }
            .addOnFailureListener { e ->
                listener.onPurchaseError(-1, "Error de verificación: ${e.message}")
            }
    }

    // Restaura suscripciones activas al reconectar (ej: reinstalación de la app).
    private fun queryPurchases() {
        billingClient?.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                purchases.forEach { purchase ->
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        val productId = purchase.products.firstOrNull() ?: return@forEach
                        // Revalida contra el servidor para actualizar premiumCache
                        verifyWithServer(productId, purchase.purchaseToken)
                    }
                }
            }
        }
    }
}
