package com.vinscanner.app.data

/**
 * 单条VIN记录数据模型。
 * @property vin 17位VIN码
 * @property timestamp 采集时间（毫秒时间戳）
 * @property source 采集来源："scan" 扫码 或 "manual" 手动输入
 */
data class VinRecord(
    val vin: String,
    val timestamp: Long,
    val source: String
)
