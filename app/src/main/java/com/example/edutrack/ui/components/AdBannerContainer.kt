package com.example.edutrack.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.edutrack.core.AdMobIds
import com.example.edutrack.data.ads.AdManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun AdBannerContainer(
    isPremium: Boolean,
    modifier: Modifier = Modifier
) {
    if (isPremium) return
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        AdManager.initialize(context)
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
            AndroidView(
                modifier = Modifier.fillMaxWidth(),
                factory = { viewContext ->
                    AdView(viewContext).apply {
                        setAdSize(AdSize.BANNER)
                        adUnitId = AdMobIds.TEST_BANNER_ID
                        loadAd(AdRequest.Builder().build())
                    }
                }
            )
        }
    }
}
