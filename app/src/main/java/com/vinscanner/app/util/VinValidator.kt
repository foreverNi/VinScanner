package com.vinscanner.app.util

/**
 * VIN码校验工具。
 *
 * VIN（车辆识别代号）共17位，由数字和大写字母组成，不包含 I、O、Q 三个字母。
 * 这里实现轻量级格式校验，不做严格的校验位（第9位）合法性校验，
 * 因为不同厂家规则复杂，且在实际使用方有需要自行扩展。
 */
object VinValidator {

    private const val VIN_LENGTH = 17

    /** 校验通过字符：不包含 I / O / Q */
    private val VALID_CHARS = ('0'..'9').toSet() + ('A'..'Z').toSet() - setOf('I', 'O', 'Q')

    /**
     * 规范化输入：去除空白并转为大写，过滤掉非法字符。 */
    fun normalize(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        return raw.trim().uppercase().filter { it in VALID_CHARS }
    }

    /** 长度必须为 17 位且全为合法字符 */
    fun isValid(vin: String?): Boolean {
        if (vin == null) return false
        val normalized = normalize(vin)
        if (normalized.length != VIN_LENGTH) return false
        return normalized.all { it in VALID_CHARS }
    }

    fun formatError(vin: String): Boolean = isValid(vin)
}
