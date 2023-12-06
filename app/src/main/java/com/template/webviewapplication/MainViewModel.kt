package com.template.webviewapplication

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URLEncoder

class MainViewModel : ViewModel() {
    var resultInstellReferrer: String? = null
    var resultFirebaseAnId: String? = null
    var resultTokenFBM: String? = null
    var resultAdvestingId: String? = null
    fun getData(
        context: Context, referrerClient: InstallReferrerClient, callback: (result: String) -> Unit
    ) {
        connectInstellReferrerClient(referrerClient) {
            resultInstellReferrer = it
            isAllData(callback)
        }
        getFirebaseAnalyticsId {
            resultFirebaseAnId = it
            isAllData(callback)
        }
        callGetTokenFBM {
            resultTokenFBM = it
            isAllData(callback)
        }
        callGetAdvestingId(context) {
            resultAdvestingId = it
            isAllData(callback)
        }
    }

    fun isAllData(callback: (result: String) -> Unit) {
        if (resultInstellReferrer != null && resultFirebaseAnId != null && resultTokenFBM != null && resultAdvestingId != null) {
            callback(resultInstellReferrer + "&" + resultFirebaseAnId + "&" + resultTokenFBM + "&" + resultAdvestingId + "&")
        }
    }

    fun connectInstellReferrerClient(
        referrerClient: InstallReferrerClient, callback: (result: String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            referrerClient.startConnection(object : InstallReferrerStateListener {
                override fun onInstallReferrerSetupFinished(p0: Int) {
                    when (p0) {
                        InstallReferrerClient.InstallReferrerResponse.OK -> {
                            val response: ReferrerDetails = referrerClient.installReferrer
                            val referrerUrl: String = response.installReferrer
                            Log.d("InstallReferrer", "OK")
                            callback(
                                "referrerUrl=" + URLEncoder.encode(referrerUrl, "utf-8")
                            )
                        }

                        InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> {
                            Log.d("InstallReferrer", "FEATURE_NOT_SUPPORTED")
                            callback("")
                        }

                        InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE -> {
                            Log.d("InstallReferrer", "SERVICE_UNAVAILABLE")
                            callback("")
                        }
                    }
                }

                override fun onInstallReferrerServiceDisconnected() {
                    Log.e("InstallReferrer", "ServiceDisconnected")
                    callback("")
                }
            })
        }
    }

    fun getFirebaseAnalyticsId(callback: (result: String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            Firebase.analytics.appInstanceId.addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d("FAID", it.result.toString())
                    val result = if (it.result == null) "" else "firebaseAnalyticsId=${it.result}"
                    callback(result)
                } else {
                    Log.d("FAID", "fail ${it.result.toString()}")
                    callback("")
                }
            }
        }
    }

    fun callGetAdvestingId(context: Context, callback: (result: String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val adid = AdvertisingIdClient.getAdvertisingIdInfo(context)
            Log.d("adid", adid.id.toString())
            val result = if (adid.id == null) "" else "advestingId=${adid.id}"
            callback(result)
        }
    }

    fun callGetTokenFBM(callback: (result: String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener {
                if (it.isSuccessful) {
                    val token = it.result
                    Log.d("TokenFBM", token)
                    callback("token=$token")
                } else {
                    Log.w(
                        "TokenFBM", "Fetching FCM registration token failed", it.exception
                    )
                    callback("")
                }
            }
        }
    }
}/*
class MainViewModel : ViewModel(): Это класс MainViewModel, который наследуется от класса ViewModel.
 ViewModel - это часть архитектурных компонентов Android, которая используется для хранения и управления данными,
  связанными с пользовательским интерфейсом и бизнес-логикой приложения.
Переменные:
resultInstellReferrer, resultFirebaseAnId, resultTokenFBM, resultAdvestingId: Это переменные для хранения результатов получения данных из различных источников.
Функция getData():
Эта функция предназначена для получения данных из различных источников (установки реферера, идентификатора
 Firebase Analytics, токена Firebase Messaging, рекламного идентификатора) и вызова колбэка с результатом.
Функция isAllData():
Эта функция проверяет наличие всех данных и вызывает переданный колбэк, если все данные получены.
Функции для получения данных из различных источников:
connectInstellReferrerClient(): Эта функция используется для получения данных из InstallReferrerClient,
 который предоставляет информацию о реферере установки приложения.
getFirebaseAnalyticsId(): Получает идентификатор Firebase Analytics.
callGetAdvestingId(): Получает рекламный идентификатор.
callGetTokenFBM(): Получает токен Firebase Messaging для отправки уведомлений.
Использование viewModelScope.launch и Dispatchers.IO:
viewModelScope.launch используется для запуска асинхронных операций.
Dispatchers.IO указывает, что операции будут выполняться в фоновом потоке, чтобы не блокировать пользовательский интерфейс.
Обработка результатов получения данных:
Результаты получения данных обрабатываются в соответствующих методах (onInstallReferrerSetupFinished,
 addOnCompleteListener и других), и если данные успешно получены, вызывается соответствующий колбэк с результатом.
 */