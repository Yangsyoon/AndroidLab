package com.example.androidlab.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE

object HapticPrefs {
    private const val PREF = "haptic_prefs"
    private fun sp(ctx: Context) = ctx.getSharedPreferences(PREF, MODE_PRIVATE)

    private fun keyPattern(emotion: String) = "h_${emotion}_pattern"
    private fun keyAmps(emotion: String)    = "h_${emotion}_amps"
    private fun keyRepeat(emotion: String)  = "h_${emotion}_repeat"

    /** 저장 (CSV 직렬화) */
    fun save(ctx: Context, emotion: String, pattern: LongArray, amps: IntArray, repeat: Int = -1) {
        require(pattern.size == amps.size) { "pattern과 amplitudes의 길이는 같아야 합니다." }
        sp(ctx).edit()
            .putString(keyPattern(emotion), pattern.joinToString(","))
            .putString(keyAmps(emotion),    amps.joinToString(","))
            .putInt(keyRepeat(emotion), repeat)
            .apply()
    }

    /** 로드 (없으면 null) */
    fun load(ctx: Context, emotion: String): Triple<LongArray, IntArray, Int>? {
        val pStr = sp(ctx).getString(keyPattern(emotion), null) ?: return null
        val aStr = sp(ctx).getString(keyAmps(emotion), null) ?: return null
        val repeat = sp(ctx).getInt(keyRepeat(emotion), -1)

        val pattern = pStr.split(",").mapNotNull { it.trim().toLongOrNull() }.toLongArray()
        val amps    = aStr.split(",").mapNotNull { it.trim().toIntOrNull() }.toIntArray()
        if (pattern.isEmpty() || pattern.size != amps.size) return null

        return Triple(pattern, amps, repeat)
    }

    fun clear(ctx: Context, emotion: String) {
        sp(ctx).edit()
            .remove(keyPattern(emotion))
            .remove(keyAmps(emotion))
            .remove(keyRepeat(emotion))
            .apply()
    }

    fun hasCustom(ctx: Context, emotion: String): Boolean =
        sp(ctx).contains(keyPattern(emotion)) && sp(ctx).contains(keyAmps(emotion))
}
