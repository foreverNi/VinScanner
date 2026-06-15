package com.vinscanner.app

import android.app.Application
import android.util.Log
import java.io.PrintWriter
import java.io.StringWriter

class VinScannerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            val summary = "${error.javaClass.simpleName}: ${error.message ?: "no message"}"
            Log.e(TAG, "Uncaught exception on ${thread.name}", error)
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putString(KEY_LAST_CRASH, summary)
                .putString(KEY_LAST_CRASH_STACK, error.stackTraceToStringCompat())
                .apply()
            previousHandler?.uncaughtException(thread, error)
        }
    }

    private fun Throwable.stackTraceToStringCompat(): String {
        val writer = StringWriter()
        printStackTrace(PrintWriter(writer))
        return writer.toString()
    }

    companion object {
        private const val TAG = "VinScannerApp"
        const val PREFS_NAME = "vin_scanner_crash"
        const val KEY_LAST_CRASH = "last_crash"
        const val KEY_LAST_CRASH_STACK = "last_crash_stack"
    }
}
