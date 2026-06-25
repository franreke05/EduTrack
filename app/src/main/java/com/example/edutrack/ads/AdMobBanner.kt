package com.example.edutrack.ads

import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.edutrack.domain.UserPlan
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration

/**
 * AdMobBanner: Muestra anuncios de AdMob solo para usuarios FREE.
 *
 * IMPORTANTE:
 * - Solo se muestra si userPlan == UserPlan.FREE
 * - Los usuarios PREMIUM nunca ven anuncios
 * - Usar test ad units durante desarrollo
 * - En producción, reemplazar con IDs reales de Google AdMob
 *
 * Test Ad Unit IDs (nunca usar en producción):
 * - Banner: ca-app-pub-3940256099942544/6300978111
 * - Interstitial: ca-app-pub-3940256099942544/1033173712
 * - Rewarded: ca-app-pub-3940256099942544/5224354917
 */

const val ADMOB_APP_ID = "ca-app-pub-xxxxxxxxxxxxxxxx"  // TODO: ID real de AdMob
const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"  // Test unit

object AdMobHelper {
    private var isInitialized = false

    fun initialize(context: Context) {
        if (!isInitialized) {
            // Público 13-17 (menores). En la UE esto obliga a anuncios NO
            // personalizados y aptos para menores (RGPD + política Familias de Play).
            MobileAds.setRequestConfiguration(
                RequestConfiguration.Builder()
                    .setTagForUnderAgeOfConsent(
                        RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE
                    )
                    .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_T)
                    .build()
            )
            MobileAds.initialize(context)
            isInitialized = true
        }
    }
}

@Composable
fun AdMobBanner(
    userPlan: UserPlan,
    modifier: Modifier = Modifier,
    context: Context? = null
) {
    // Solo mostrar para usuarios FREE
    if (userPlan != UserPlan.FREE) {
        return
    }

    // Inicializar AdMob si es necesario
    context?.let { AdMobHelper.initialize(it) }

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        factory = { ctx ->
            AdView(ctx).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = BANNER_AD_UNIT_ID
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}

/**
 * Ubicaciones permitidas para anuncios:
 * ✅ InicioActivity - después de la última card
 * ✅ PerfilActivity - entre secciones
 * ✅ Final de listas (LazyColumn)
 *
 * Ubicaciones PROHIBIDAS:
 * ❌ PaywallScreen (nunca mezclar ads y compras)
 * ❌ Formularios de notas (distracting)
 * ❌ Simulador (interfiere con estudio)
 * ❌ Pantallas de autenticación
 * ❌ Diálogos importantes
 */
