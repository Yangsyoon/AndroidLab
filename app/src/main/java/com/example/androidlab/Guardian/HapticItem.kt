package com.example.androidlab.Guardian

data class HapticItem(
    val emotion: String,        // "기쁨" / "놀람" / "화남" / "슬픔"
    val isCustom: Boolean,      // 커스텀 여부
    val pattern: LongArray,     // waveform times
    val amps: IntArray,         // waveform amplitudes (0~255)
    val repeat: Int = -1        // 반복 인덱스
)