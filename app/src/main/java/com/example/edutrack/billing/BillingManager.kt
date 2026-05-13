package com.example.edutrack.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * BillingManager: Gestiona suscripciones mediante Google Play Billing Library.
 *
 * IMPORTANTE:
 * - Este manager NO concede Premium localmente.
 * - Todas las compras se validan server-side mediante Cloud Function.
 * - El token de compra se envía al backend para verificación contra Google Play API.
 * - Solo si la verificación es exitosa, el usuario recibe Premium.
 *
 * TODO para PRODUCCIÓN:
 * 1. Reemplazar PRODUCT_IDS con IDs reales de Google Play Console
 * 2. Implementar launchBillingFlow() para mostrar Google Play Store
 * 3. Conectar onPurchaseSuccess() con Cloud Function verifyPremiumPurchase
 * 4. Manejar estados de suscripción: SUBSCRIPTION_STATE_ACTIVE, IN_GRACE_PERIOD, CANCELED, EXPIRED
 */

const val PRODUCT_ID_MONTHLY = "edutrack_premium_monthly"  // TODO: ID real de Play Console
const val PRODUCT_ID_ANNUAL = "edutrack_premium_annual"    // TODO: ID real de Play Console

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

    fun connect() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        billingClient?.startConnection(this)
    }

    fun disconnect() {
        billingClient?.endConnection()
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
        // TODO: Implementar cuando se establezca conexión real con Play Console
        /*
        val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
            .addProduct(productId, BillingClient.ProductType.SUBS)
            .build()

        billingClient?.queryProductDetailsAsync(queryProductDetailsParams) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                val productDetails = productDetailsList[0]
                val flowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(
                        listOf(
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                                .setOfferToken(productDetails.subscriptionOfferDetails?.get(0)?.offerToken ?: "")
                                .build()
                        )
                    )
                    .build()

                billingClient?.launchBillingFlow(activity, flowParams)
            } else {
                listener.onPurchaseError(billingResult.responseCode, billingResult.debugMessage)
            }
        }
        */
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<com.android.billingclient.api.Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                if (purchase.purchaseState == com.android.billingclient.api.Purchase.PurchaseState.PURCHASED) {
                    // IMPORTANTE: NUNCA conceder Premium localmente
                    // El token se envía al backend para verificación
                    val productId = purchase.products.firstOrNull() ?: continue
                    handlePurchase(productId, purchase.purchaseToken, purchase)
                }
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            listener.onPurchaseCanceled()
        } else {
            listener.onPurchaseError(billingResult.responseCode, billingResult.debugMessage)
        }
    }

    private fun handlePurchase(productId: String, purchaseToken: String, purchase: com.android.billingclient.api.Purchase) {
        // Reconocer la compra
        scope.launch {
            val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchaseToken)
                .build()

            billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // Enviar token al backend para verificación server-side
                    // NO conceder Premium hasta que el backend confirme
                    listener.onPurchaseSuccess(productId, purchaseToken)
                } else {
                    listener.onPurchaseError(billingResult.responseCode, "Failed to acknowledge purchase")
                }
            }
        }
    }

    private fun queryPurchases() {
        // TODO: Restaurar compras previas
        /*
        billingClient?.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                for (purchase in purchases) {
                    if (purchase.purchaseState == com.android.billingclient.api.Purchase.PurchaseState.PURCHASED) {
                        val productId = purchase.products.firstOrNull() ?: continue
                        listener.onPurchaseSuccess(productId, purchase.purchaseToken)
                    }
                }
            }
        }
        */
    }
}
