package com.example.core.automation

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.util.Log

/**
 * Lets Zoya actually control the screen (tap, scroll, type, read content,
 * launch apps) instead of just describing what the user should tap.
 * Requires the user to manually enable it once under
 * Settings -> Accessibility -> Sara AI (Android does not allow apps to
 * turn this on for themselves).
 */
class ZoyaAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "ZoyaAccessibilityService"

        /** Null when the service isn't currently running/enabled by the user. */
        @Volatile
        var instance: ZoyaAccessibilityService? = null
            private set

        val isRunning: Boolean get() = instance != null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d(TAG, "Zoya accessibility service connected")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance === this) instance = null
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // No passive listening needed; all actions are performed on-demand
        // when the JS bridge calls into this service.
    }

    override fun onInterrupt() {}

    // ---- Actions callable from the JS bridge ----

    /** All content roots currently on screen (handles multi-window/pop-ups), most relevant first. */
    private fun activeRoots(): List<AccessibilityNodeInfo> {
        val roots = mutableListOf<AccessibilityNodeInfo>()
        try {
            windows?.forEach { window -> window.root?.let { roots.add(it) } }
        } catch (e: Exception) {
            Log.w(TAG, "Could not read window list", e)
        }
        if (roots.isEmpty()) {
            rootInActiveWindow?.let { roots.add(it) }
        }
        return roots
    }

    /** Finds the best clickable node whose text/description/id contains [text] and taps it. */
    fun tapByText(text: String): Boolean {
        for (root in activeRoots()) {
            val target = findBestNodeByText(root, text) ?: continue
            if (clickNodeOrAncestor(target)) return true
        }
        return false
    }

    fun tapAtCoordinates(x: Float, y: Float): Boolean {
        val path = Path().apply { moveTo(x, y) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 100))
            .build()
        return dispatchGesture(gesture, null, null)
    }

    fun typeText(text: String): Boolean {
        for (root in activeRoots()) {
            val focused = findFocusedEditable(root) ?: continue
            val args = Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
            }
            if (focused.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)) return true
        }
        return false
    }

    fun scroll(direction: String): Boolean {
        for (root in activeRoots()) {
            val scrollable = findScrollable(root) ?: continue
            val action = if (direction == "up")
                AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
            else
                AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
            if (scrollable.performAction(action)) return true
        }
        return false
    }

    fun goBack(): Boolean = performGlobalAction(GLOBAL_ACTION_BACK)

    fun goHome(): Boolean = performGlobalAction(GLOBAL_ACTION_HOME)

    /** Concatenates visible text on screen, for Zoya to "read" the current screen. */
    fun getScreenContent(): String {
        val sb = StringBuilder()
        for (root in activeRoots()) {
            collectText(root, sb)
        }
        return sb.toString().trim().take(4000)
    }

    fun launchApp(appName: String): Boolean {
        return try {
            val pm = packageManager
            val apps = pm.getInstalledApplications(0)
            val match = apps.firstOrNull {
                pm.getApplicationLabel(it).toString().equals(appName, ignoreCase = true)
            } ?: apps.firstOrNull {
                pm.getApplicationLabel(it).toString().contains(appName, ignoreCase = true)
            } ?: return false

            val launchIntent = pm.getLaunchIntentForPackage(match.packageName) ?: return false
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(launchIntent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "launchApp failed", e)
            false
        }
    }

    /** Opens the dialer pre-filled with [number]; does not require CALL_PHONE permission. */
    fun dialNumber(number: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_DIAL, android.net.Uri.parse("tel:$number"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "dialNumber failed", e)
            false
        }
    }

    /** Opens the Messages app pre-filled; does not require SEND_SMS permission. */
    fun sendSms(number: String, message: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_SENDTO, android.net.Uri.parse("smsto:$number"))
            intent.putExtra("sms_body", message)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "sendSms failed", e)
            false
        }
    }

    fun sendWhatsAppMessage(number: String?, message: String): Boolean {
        return try {
            val uri = if (!number.isNullOrBlank()) {
                "https://wa.me/${number.filter { it.isDigit() }}?text=${android.net.Uri.encode(message)}"
            } else {
                "https://wa.me/?text=${android.net.Uri.encode(message)}"
            }
            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(uri))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "sendWhatsAppMessage failed", e)
            false
        }
    }

    // ---- Node-tree helpers ----

    /**
     * Searches the whole tree and picks the best match for [text]: an exact,
     * clickable match wins; then a clickable partial match; then any partial
     * match (which clickNodeOrAncestor will walk up from).
     */
    private fun findBestNodeByText(node: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        var bestPartial: AccessibilityNodeInfo? = null
        var bestClickablePartial: AccessibilityNodeInfo? = null

        fun visit(n: AccessibilityNodeInfo) {
            val nodeText = n.text?.toString() ?: ""
            val nodeDesc = n.contentDescription?.toString() ?: ""
            val exact = nodeText.equals(text, ignoreCase = true) || nodeDesc.equals(text, ignoreCase = true)
            val partial = !exact && (nodeText.contains(text, ignoreCase = true) || nodeDesc.contains(text, ignoreCase = true))

            if (exact) {
                if (n.isClickable) {
                    bestClickablePartial = n
                } else if (bestClickablePartial == null) {
                    bestClickablePartial = n
                }
            } else if (partial) {
                if (n.isClickable && bestClickablePartial == null) bestClickablePartial = n
                if (bestPartial == null) bestPartial = n
            }
            for (i in 0 until n.childCount) {
                n.getChild(i)?.let { visit(it) }
            }
        }
        visit(node)
        return bestClickablePartial ?: bestPartial
    }

    private fun clickNodeOrAncestor(node: AccessibilityNodeInfo): Boolean {
        var current: AccessibilityNodeInfo? = node
        while (current != null) {
            if (current.isClickable) {
                return current.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
            current = current.parent
        }
        return false
    }

    private fun findFocusedEditable(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isFocused && node.isEditable) return node
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findFocusedEditable(child)
            if (found != null) return found
        }
        return null
    }

    private fun findScrollable(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isScrollable) return node
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findScrollable(child)
            if (found != null) return found
        }
        return null
    }

    private fun collectText(node: AccessibilityNodeInfo, sb: StringBuilder) {
        val t = node.text?.toString()
        if (!t.isNullOrBlank()) {
            sb.append(t).append(". ")
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            collectText(child, sb)
        }
    }
}
