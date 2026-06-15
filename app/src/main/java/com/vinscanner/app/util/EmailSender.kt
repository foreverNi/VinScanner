package com.vinscanner.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.vinscanner.app.data.VinRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object EmailSender {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)
    private val subjectFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)

    fun buildEmailIntent(
        context: Context,
        toAddress: String,
        subjectPrefix: String,
        records: List<VinRecord>
    ): Intent? {
        val date = subjectFormat.format(Date())
        val subject = "$subjectPrefix - $date"
        val body = buildBody(records)
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(toAddress))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        return if (intent.resolveActivity(context.packageManager) != null) intent else null
    }

    fun buildBody(records: List<VinRecord>): String {
        val header = "以下为本次采集的车辆 VIN 码列表：\n\n"
        val footer = "\n\n—— 来自 VinScanner Android App"
        val lines = records.mapIndexed { idx, record ->
            val time = dateFormat.format(Date(record.timestamp))
            String.format(Locale.US, "%3d   %-18s   %s", idx + 1, record.vin, time)
        }
        val count = "\n共计 ${records.size} 条。"
        return header + lines.joinToString("\n") + count + footer
    }
}
