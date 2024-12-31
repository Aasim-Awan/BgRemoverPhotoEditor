package com.photoTools.bgEraser

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.homeworkoutmate.dailyfitnessapp.gymexercise.utils.AdsManager
import com.photoTools.bgEraser.databinding.ActivityOnboardingBinding

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private var adsIterator = 0
    private val handler = Handler(Looper.getMainLooper())
    private val runnable = Runnable {
        checkAdsAvailable()
        adsIterator += 1
        Log.d("AD_LOG", "$adsIterator")
    }
    private val adsManager: AdsManager by lazy {
        AdsManager.getInstance(this)
    }
    private var showInterstitial: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        activateRemoteConfig()

        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.statusBars())

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this@OnboardingActivity, DashboardActivity::class.java))
            finish()
        }, 2000)

    }

    private fun activateRemoteConfig() {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(3600)
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_default)
        remoteConfig.fetchAndActivate().addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val activeValue = remoteConfig.getValue("banner_ad")
                val activeValue1 = remoteConfig.getValue("interstitial_ad")
                val activeValue2 = remoteConfig.getValue("native_ad")
                Log.d("remote_config", "Config params updated: " + activeValue.asBoolean())
                Log.d(
                    "remote_config",
                    "Config params updated: " + activeValue1.asBoolean()
                )
                Log.d(
                    "remote_config",
                    "Config params updated: " + activeValue2.asBoolean()
                )
            } else {
                Log.d("remote_config", "Config params updated: Error")
            }
        }
    }

    private fun checkAdsAvailable(isOnclick: Boolean = false) {
        if (AppController.openAppAd != null) {
            AppController.openAppAd!!.show(this)
            AppController.openAppAd!!.fullScreenContentCallback =
                object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent()
                        val intent = Intent(this@OnboardingActivity, DashboardActivity::class.java)
                        startActivity(intent)
                        finish()
                    }

                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                        super.onAdFailedToShowFullScreenContent(p0)
                        val intent = Intent(this@OnboardingActivity, DashboardActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
        } else {
            adsManager.showAd(this, object : AdsManager.IInterstitialListener{
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    interstitialAd.show(this@OnboardingActivity)
                    interstitialAd.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent()
                                Handler(Looper.getMainLooper()).postDelayed({
                                    startActivity(Intent(this@OnboardingActivity, DashboardActivity::class.java))
                                    finish()
                                }, 600)
                            }

                            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                super.onAdFailedToShowFullScreenContent(p0)
                                Handler(Looper.getMainLooper()).postDelayed({
                                    startActivity(Intent(this@OnboardingActivity, DashboardActivity::class.java))
                                    finish()
                                }, 600)
                            }
                        }
                }

                override fun onError(message: String) {
                    Log.d("AD_LOG", message)
                    //binding.rlLoadingCont.visibility = View.GONE
                    startActivity(Intent(this@OnboardingActivity, DashboardActivity::class.java))
                    finish()
                }

            })
        }
    }

}