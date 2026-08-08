package com.example.core.automation

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils
import android.webkit.JavascriptInterface
import org.json.JSONObject

/**
 * Exposed to the Zoya web app as `window.ZoyaNativeBridge`. Every method
 * returns a JSON string (JavascriptInterface methods can't return
 * arbitrary objects to JS) which the web app's androidAutomation.ts parses.
 * All methods are synchronous and safe to call off the main thread.
 */
class AutomationJsBridge(private val context: Context) {

    @JavascriptInterface
    fun isNative(): Boolean = true

    @JavascriptInterface
    fun isAccessibilityServiceEnabled(): Boolean {
        return try {
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false
            val splitter = TextUtils.SimpleStringSplitter(':')
            splitter.setString(enabledServices)
            val target = "${context.packageName}/${ZoyaAccessibilityService::class.java.name}"
            splitter.any { it.equals(target, ignoreCase = true) } && ZoyaAccessibilityService.isRunning
        } catch (e: Exception) {
            false
        }
    }

    @JavascriptInterface
    fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    @JavascriptInterface
    fun launchApp(appName: String): String =
        result(ZoyaAccessibilityService.instance?.launchApp(appName) == true)

    @JavascriptInterface
    fun tapByText(text: String): String =
        result(ZoyaAccessibilityService.instance?.tapByText(text) == true)

    @JavascriptInterface
    fun tapAtCoordinates(x: Float, y: Float): String =
        result(ZoyaAccessibilityService.instance?.tapAtCoordinates(x, y) == true)

    @JavascriptInterface
    fun typeText(text: String): String =
        result(ZoyaAccessibilityService.instance?.typeText(text) == true)

    @JavascriptInterface
    fun scroll(direction: String): String =
        result(ZoyaAccessibilityService.instance?.scroll(direction) == true)

    @JavascriptInterface
    fun goBack(): String =
        result(ZoyaAccessibilityService.instance?.goBack() == true)

    @JavascriptInterface
    fun goHome(): String =
        result(ZoyaAccessibilityService.instance?.goHome() == true)

    @JavascriptInterface
    fun getScreenContent(): String {
        val content = ZoyaAccessibilityService.instance?.getScreenContent() ?: ""
        return JSONObject().put("content", content).toString()
    }

    @JavascriptInterface
    fun makeCall(number: String): String =
        result(ZoyaAccessibilityService.instance?.dialNumber(number) == true)

    @JavascriptInterface
    fun sendSms(number: String, message: String): String =
        result(ZoyaAccessibilityService.instance?.sendSms(number, message) == true)

    @JavascriptInterface
    fun sendWhatsAppMessage(number: String?, message: String): String =
        result(ZoyaAccessibilityService.instance?.sendWhatsAppMessage(number, message) == true)

    private fun result(success: Boolean): String =
        JSONObject().put("success", success).toString()
}
