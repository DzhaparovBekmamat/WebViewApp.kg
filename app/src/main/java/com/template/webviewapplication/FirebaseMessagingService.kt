package com.template.webviewapplication

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

// class FBMessagingService : FirebaseMessagingService(): Этот класс FBMessagingService наследуется от
// FirebaseMessagingService. Он обрабатывает уведомления, связанные с Firebase Cloud Messaging (FCM) в приложении Android.
class FBMessagingService : FirebaseMessagingService() {
    val newToken = MutableLiveData<String>()
    // val newToken = MutableLiveData<String>(): Это переменная newToken типа MutableLiveData<String>,
    // которая используется для хранения нового токена уведомлений.

    override fun onNewToken(token: String) {
        Log.d("NewTokenFBM", "Refreshed token: $token")
        newToken.postValue(token)
    }
    // onNewToken(token: String): Этот метод вызывается, когда приложение получает новый токен уведомлений
    // от Firebase Cloud Messaging. В данном случае, при получении нового токена, он записывается в newToken,
    //  чтобы можно было отслеживать изменения в этом токене.
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FBM", "From: ${remoteMessage.from}")
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("FBM", "Message data payload: ${remoteMessage.data}")
        }
        remoteMessage.notification?.let {
            Log.d("FBM", "Message Notification Body: ${it.body}")
        }
    }
    // onMessageReceived(remoteMessage: RemoteMessage): Этот метод вызывается, когда приложение получает
    // новое уведомление через Firebase Cloud Messaging. Здесь обрабатывается приходящее уведомление. Если у
    // уведомления есть данные, они выводятся в логи. Если уведомление имеет текст уведомления, он также выводится в логи.
}
