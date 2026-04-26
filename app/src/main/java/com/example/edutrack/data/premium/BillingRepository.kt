package com.example.edutrack.data.premium

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.example.edutrack.BuildConfig
import com.example.edutrack.core.BillingProductIds
import com.example.edutrack.data.setPremiumEntitlement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class PremiumProduct(
    val id: String,
    val title: String,
    val price: String,
    val periodLabel: String,
    val productDetails: ProductDetails
)

data class BillingUiState(
    val isConnected: Boolean = false,
    val isLoading: Boolean = false,
    val products: List<PremiumProduct> = emptyList(),
    val hasPendingPurchase: Boolean = false,
    val message: String? = null
)

class BillingRepository(private val context: Context) : PurchasesUpdatedListener {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _state = MutableStateFlow(BillingUiState())
    val state: StateFlow<BillingUiState> = _state

    private val billingClient = BillingClient.newBuilder(appContext)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .enablePrepaidPlans()
                .build()
        )
        .enableAutoServiceReconnection()
        .build()

    suspend fun connectAndRefresh() {
        _state.update { it.copy(isLoading = true, message = null) }
        val connected = ensureConnected()
        if (!connected) {
            _state.update { it.copy(isLoading = false, isConnected = false, message = "Google Play Billing no está disponible.") }
            return
        }
        queryProducts()
        refreshPurchases(showMessage = false)
    }

    fun launchPurchase(activity: Activity, product: PremiumProduct) {
        if (!billingClient.isReady) {
            _state.update { it.copy(message = "Conectando con Google Play. Inténtalo de nuevo en unos segundos.") }
            scope.launch { connectAndRefresh() }
            return
        }
        val offerToken = product.productDetails.subscriptionOfferDetails
            ?.firstOrNull()
            ?.offerToken
        if (offerToken.isNullOrBlank()) {
            _state.update { it.copy(message = "Este producto aún no tiene una oferta activa en Play Console.") }
            return
        }
        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(product.productDetails)
            .setOfferToken(offerToken)
            .build()
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()
        val result = billingClient.launchBillingFlow(activity, params)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            _state.update { it.copy(message = billingMessage(result)) }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                val updatedPurchases = purchases.orEmpty()
                if (updatedPurchases.isEmpty()) {
                    _state.update { it.copy(message = "No se recibió ninguna compra nueva.") }
                } else {
                    updatedPurchases.forEach { purchase ->
                        scope.launch { processPurchase(purchase) }
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _state.update { it.copy(message = "Compra cancelada.") }
            }
            else -> {
                _state.update { it.copy(message = billingMessage(billingResult)) }
            }
        }
    }

    suspend fun refreshPurchases(showMessage: Boolean = false) {
        if (!ensureConnected()) {
            _state.update { it.copy(isConnected = false, message = "No se pudo conectar con Google Play Billing.") }
            return
        }
        val result = billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )
        val hasPremium = result.purchasesList.any { purchase ->
            purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                purchase.products.any { it in BillingProductIds.ALL }
        }
        val hasPending = result.purchasesList.any { purchase ->
            purchase.purchaseState == Purchase.PurchaseState.PENDING &&
                purchase.products.any { it in BillingProductIds.ALL }
        }
        appContext.setPremiumEntitlement(hasPremium)
        result.purchasesList.forEach { purchase ->
            processPurchase(purchase)
        }
        _state.update {
            it.copy(
                hasPendingPurchase = hasPending,
                message = when {
                    hasPending -> "Tu compra está pendiente. Premium se activará cuando Google Play confirme el pago."
                    showMessage && hasPremium -> "Compras restauradas. Premium está activo."
                    showMessage -> "No encontramos una suscripción Premium activa."
                    else -> it.message
                }
            )
        }
    }

    private suspend fun queryProducts() {
        val productList = BillingProductIds.ALL.map { productId ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()
        val result = billingClient.queryProductDetails(params)
        if (result.billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            _state.update {
                it.copy(
                    isConnected = true,
                    isLoading = false,
                    message = billingMessage(result.billingResult)
                )
            }
            return
        }
        val products = result.productDetailsList.orEmpty().mapNotNull { details ->
            val offer = details.subscriptionOfferDetails?.firstOrNull()
            val price = offer?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice
            if (price == null) {
                null
            } else {
                PremiumProduct(
                    id = details.productId,
                    title = if (details.productId == BillingProductIds.PREMIUM_YEARLY) "Premium anual" else "Premium mensual",
                    price = price,
                    periodLabel = if (details.productId == BillingProductIds.PREMIUM_YEARLY) "al año" else "al mes",
                    productDetails = details
                )
            }
        }.sortedBy { if (it.id == BillingProductIds.PREMIUM_YEARLY) 0 else 1 }
        _state.update {
            it.copy(
                isConnected = true,
                isLoading = false,
                products = products,
                message = if (products.isEmpty()) "Configura los productos de suscripción en Play Console." else null
            )
        }
    }

    private suspend fun processPurchase(purchase: Purchase) {
        if (purchase.products.none { it in BillingProductIds.ALL }) return
        if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            _state.update {
                it.copy(
                    hasPendingPurchase = true,
                    message = "Compra pendiente. Premium se activará cuando Google Play confirme el pago."
                )
            }
            return
        }
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return

        appContext.setPremiumEntitlement(true)
        if (!purchase.isAcknowledged) {
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            val result = billingClient.acknowledgePurchase(params)
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                logDebug("Purchase acknowledgement failed: ${result.debugMessage}")
            }
        }
        _state.update { it.copy(message = "Premium activado.") }
    }

    private suspend fun ensureConnected(): Boolean {
        if (billingClient.isReady) return true
        return suspendCancellableCoroutine { continuation ->
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    continuation.resume(billingResult.responseCode == BillingClient.BillingResponseCode.OK)
                }

                override fun onBillingServiceDisconnected() {
                    _state.update { it.copy(isConnected = false) }
                }
            })
        }
    }

    private fun billingMessage(result: BillingResult): String =
        if (BuildConfig.DEBUG && result.debugMessage.isNotBlank()) {
            "Billing: ${result.debugMessage}"
        } else {
            "No se pudo completar la operación de compra."
        }

    private fun logDebug(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d("BillingRepository", message)
        }
    }
}
