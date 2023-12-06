package com.template.webviewapplication

import android.content.Intent
import android.graphics.Bitmap
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.MutableLiveData
// class WevViewClient : WebViewClient(): Это объявляет класс с именем WevViewClient, который наследуется
// от класса WebViewClient. Этот класс используется для управления поведением WebView.
class WevViewClient : WebViewClient() {
    val progressLoadingPage = MutableLiveData(1)
    // val progressLoadingPage = MutableLiveData(1): Создает переменную progressLoadingPage типа MutableLiveData,
    // которая хранит значение типа Int и инициализируется значением 1. MutableLiveData - это класс из Android Jetpack,
    // который позволяет наблюдать за изменениями значения.
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        // override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean:
        // Этот метод вызывается, когда происходит попытка загрузки URL в WebView. Он возвращает true или false,
        // указывая, следует ли обрабатывать URL в приложении или позволить системе открыть его во внешнем браузере.
        // В данном случае, если URL принадлежит к BASE_URL, то он откроется в текущем WebView. В противном случае,
        // он откроется во внешнем браузере.
        progressLoadingPage.postValue(1)
        return if (request == null) false
        else {
            if (BASE_URL.contains(request.url.host!!)) false
            else {
                view ?: false
                startActivity(view!!.context, Intent(Intent.ACTION_VIEW, request.url), null)
                // startActivity(view!!.context, Intent(Intent.ACTION_VIEW, request.url), null): Этот
                // код отвечает за запуск новой активности для открытия URL. Он использует Intent для
                // вызова действия ACTION_VIEW, которое открывает внешнюю активность для загрузки данного URL.
                true
            }
        }
    }
    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        // override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?):
        // Этот метод вызывается, когда начинается загрузка страницы в WebView. В данном коде, он устанавливает
        // значение progressLoadingPage в 2, чтобы показать, что страница начала загружаться.
        super.onPageStarted(view, url, favicon)
        progressLoadingPage.postValue(2)
    }
    override fun onPageFinished(view: WebView?, url: String?) {
        // override fun onPageFinished(view: WebView?, url: String?): Этот метод вызывается, когда страница
        // полностью загружена в WebView. В этом коде, он устанавливает значение progressLoadingPage в 3,
        // чтобы показать, что загрузка страницы завершена.
        super.onPageFinished(view, url)
        progressLoadingPage.postValue(3)
    }
}
