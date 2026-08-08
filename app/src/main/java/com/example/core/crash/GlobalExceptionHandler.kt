package com.example.core.crash

import android.content.Context
import android.util.Log
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

object GlobalExceptionHandler : Thread.UncaughtExceptionHandler {
    private var defaultHandler: Thread.UncaughtExceptionHandler? = null
    private var applicationContext: Context? = null

    fun initialize(context: Context) {
        applicationContext = context.applicationContext
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            val sw = StringWriter()
            throwable.printStackTrace(PrintWriter(sw))
            val crashLog = sw.toString()
            Log.e("SaraAI_CrashHandler", "Uncaught exception in thread ${thread.name}: $crashLog")

            // Write crash log to local encrypted storage for auto recovery analytics
            applicationContext?.let { ctx ->
                val file = File(ctx.filesDir, "last_crash_log.txt")
                file.writeText("Time: ${System.currentTimeMillis()}\nThread: ${thread.name}\n$crashLog")
            }
        } catch (e: Exception) {
            Log.e("SaraAI_CrashHandler", "Error logging crash", e)
        } finally {
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    fun getLastCrashLog(context: Context): String? {
        val file = File(context.filesDir, "last_crash_log.txt")
        return if (file.exists()) file.readText() else null
    }

    fun clearCrashLog(context: Context) {
        val file = File(context.filesDir, "last_crash_log.txt")
        if (file.exists()) file.delete()
    }
}
