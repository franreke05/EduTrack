package com.example.edutrack.data.ads

import android.app.Activity
import android.content.Context
import com.example.edutrack.core.AdMobIds
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class RewardedAdManager(private val context: Context) {
    private var rewardedAd: RewardedAd? = null

    fun load() {
        RewardedAd.load(
            context.applicationContext,
            AdMobIds.TEST_REWARDED_ID,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                }
            }
        )
    }

    fun show(activity: Activity, onRewardEarned: () -> Unit, onUnavailable: () -> Unit) {
        val ad = rewardedAd
        if (ad == null) {
            onUnavailable()
            load()
            return
        }
        rewardedAd = null
        ad.show(activity) {
            onRewardEarned()
            load()
        }
    }
}
