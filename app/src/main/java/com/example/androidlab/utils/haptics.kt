package com.example.androidlab.utils
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object Haptics {

    private fun getVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    fun vibrateEmotion(context: Context, emotion: String) {
        val vibrator = getVibrator(context) ?: return

        // 패턴/진폭 기본값 (무표정과 유사)
        var pattern = longArrayOf(0, 600, 300)
        var amplitudes = intArrayOf(0, 160, 0)

        when (emotion) {
            // 😢 슬픔: 감쇠되는 에너지, 길게
            "슬픔" -> {
                pattern = longArrayOf(0, 1200, 400, 1200, 400, 1200)
                amplitudes = intArrayOf(0, 130, 0, 100, 0, 70)
            }

            // 😠 화남: 강하고 빠른 펄스 반복
            "화남" -> {
                pattern = longArrayOf(0, 150, 50, 150, 50, 150, 50)
                amplitudes = intArrayOf(0, 200, 0, 200, 0, 200, 0)
            }

            // 😊 기쁨: 점점 커지는 경쾌한 리듬
            "기쁨" -> {
                pattern = longArrayOf(0, 200, 100, 200, 100, 200, 100)
                amplitudes = intArrayOf(0, 130, 0, 190, 0, 250, 0)
            }

            // 😲 놀람: 짧고 강한 단발
            "놀람" -> {
                pattern = longArrayOf(0, 150, 1000, 150)
                amplitudes = intArrayOf(0, 255, 0, 255)
            }

            // 😐 무표정(기본)
            "무표정" -> {
                pattern = longArrayOf(0, 600, 300)
                amplitudes = intArrayOf(0, 160, 0)
            }
        }
        /*
        // 실행
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 일부 기기에서 진폭 제어 미지원 시, amplitude-less로 fallback
            val effect = if (vibrator.hasAmplitudeControl()) {
                VibrationEffect.createWaveform(pattern, amplitudes, -1)
            } else {
                VibrationEffect.createWaveform(pattern, -1)
            }
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
        */
        // ★★★ 추가: 에디터에서 저장된 "커스텀 파형"이 있으면 그것을 우선 적용
        // HapticPrefs.kt가 같은 패키지(utils)에 있으니 import 없이 바로 사용 가능
        HapticPrefs.load(context, emotion)?.let { (p, a) ->
            pattern = p
            amplitudes = a

        }

        // 실행
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // ★ 추가: amplitude 제어 가능 + 길이 일치할 때만 진폭 배열 버전 사용 (안전 가드)
            val supportsAmp = vibrator.hasAmplitudeControl() && pattern.size == amplitudes.size
            val effect = if (supportsAmp) {
                VibrationEffect.createWaveform(pattern, amplitudes, -1) // ★ repeat 사용
            } else {
                VibrationEffect.createWaveform(pattern, -1) // amplitude 미지원/불일치 시 fallback
            }
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1) // ★ repeat 사용
        }

    }

    fun cancel(context: Context) {
        getVibrator(context)?.cancel()
    }
}
