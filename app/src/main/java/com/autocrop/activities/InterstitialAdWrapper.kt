package com.autocrop.activities

import android.app.Activity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import timber.log.Timber


class InterstitialAdWrapper(
    private val activity: Activity,
    unitId: String,
    private val onAdClosed: () -> Unit,
    immersiveMode: Boolean = false,
    private val logTag: String = "InterstitialAd"
){

    private var ad: InterstitialAd? = null

    init {
        InterstitialAd.load(activity, unitId, AdRequest.Builder().build(), object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                ad = null
                Timber.i("Couldn't load $logTag: ${adError.message}")
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Timber.i("Successfully loaded $logTag")

                ad = interstitialAd.apply {
                    fullScreenContentCallback = object: FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            Timber.i("$logTag was dismissed")

                            onAdClosed()
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                            Timber.i("$logTag failed to show")

                            onAdClosed()
                        }

                        override fun onAdShowedFullScreenContent() {
                            ad = null
                            Timber.i("$logTag showed fullscreen content")
                        }
                    }
                    setImmersiveMode(immersiveMode)
                }
            }
        })
    }

    fun showAd(){
        if (ad != null)
            ad?.show(activity)
                .also { Timber.i("Showing Interstitial ad") }
        else
            onAdClosed()
                .also { Timber.i("Interstitial ad wasn't ready yet") }
    }
}