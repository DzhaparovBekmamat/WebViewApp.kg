package com.template.webviewapplication

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.android.installreferrer.api.InstallReferrerClient
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging

class MainActivity : AppCompatActivity() {
    val viewModel: MainViewModel by viewModels()
    lateinit var webView: WebView
    lateinit var progressBar: ProgressBar
    private lateinit var referrerClient: InstallReferrerClient
    private var fileUploadCallback: ValueCallback<Array<Uri>>? = null
    private var telephonyCountryCode: String? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_main)
        progressBar = findViewById(R.id.progressBar)
        settingsWebView()
        referrerClient = InstallReferrerClient.newBuilder(this).build()
        getTelephonySimCountryCode()
        viewModel.getData(this, referrerClient) {
            val newUrl = "$BASE_URL?$telephonyCountryCode&$it"
            loadUrl(newUrl)
            Log.d("AllData", newUrl)
        }
        Firebase.messaging.subscribeToTopic("news").addOnCompleteListener {
            Log.d("FBTopics", if (it.isSuccessful) "Subscribed" else "Subscribe failed")
        }
        askNotificationPermission()
    }

    private fun getTelephonySimCountryCode() {
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) return
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        Log.d("telephony", telephonyManager.simCountryIso)
        telephonyCountryCode = "telephonyCountryCode=${telephonyManager.simCountryIso}"
    }

    private val fileUploadActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val results = result.data?.let {
                    WebChromeClient.FileChooserParams.parseResult(result.resultCode, it)
                }
                fileUploadCallback?.onReceiveValue(results)
            } else {
                fileUploadCallback?.onReceiveValue(null)
            }
            fileUploadCallback = null
        }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun settingsWebView() {
        webView = findViewById(R.id.webview)
        webView.webViewClient = WebViewClient()
        webView.settings.javaScriptEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.domStorageEnabled = true
        CookieManager.getInstance().setAcceptCookie(true)
        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                fileUploadCallback = filePathCallback ?: return false
                val intent = fileChooserParams?.createIntent()
                fileUploadActivityResultLauncher.launch(intent)
                return true
            }
        }
        loadUrl(BASE_URL)
    }

    fun loadUrl(url: String) {
        webView.loadUrl(url)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        Log.d("requestPermissionLauncher", isGranted.toString())
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
            } else if (shouldShowRequestPermissionRationale(
                    android.Manifest.permission.POST_NOTIFICATIONS
                )
            ) {
            } else {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
