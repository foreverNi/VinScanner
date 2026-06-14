package com.vinscanner.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.vinscanner.app.data.VinRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 通过系统邮件客户端（ACTION_SENDTO）发送VIN列表邮件。
 *
 * 采用 mailto: 协议而非SMTP直连，原因：
 *   1. 避免在应用内硬编码邮箱密码（安全）
 *   2. 用户可选择熟悉的邮件客户端（Gmail、QQ邮箱、163邮箱等）
 */
object EmailSender {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)
    private val subjectFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)

    /**
     * 构建邮件Intent。如果没有邮件客户端可处理，返回null。
     */
    fun buildEmailIntent(
        context: Context,
        toAddress: String,
        subjectPrefix: String,
        records: List<VinRecord>
    ): Intent? {
        val date = subjectFormat.format(Date())
        val subject = "$subjectPrefix - $date"
        val body = buildBody(records)
        val data = Uri.parse("mailto:")
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            this.data = data
            putExtra(Intent.EXTRA_EMAIL, arrayOf(toAddress))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        return if (intent.resolveActivity(context.packageManager) != null) intent else null
    }

    /**
     * 构建纯文本邮件正文。
     */
    fun buildBody(records: List<VinRecord>): String {
        val header = "以下为本次采集的车辆VIN码列表：\n\n"
        val footer = "\n\n—— 来自 VinScanner Android App"
        val lines = records.mapIndexed { idx, r ->
            val time = dateFormat.format(Date(r.timestamp))
            String.format("%3d   %-18s   %s", idx + 1, r.vin, time)
        }
        val count = "\n共计 ${records.size} 条。"
        return header + lines.joinToString("\n") + count + footer
    }
}
