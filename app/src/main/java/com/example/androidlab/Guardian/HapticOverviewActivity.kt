package com.example.androidlab.Guardian

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androidlab.BaseActivity
import com.example.androidlab.R
import com.example.androidlab.utils.HapticPrefs

class HapticOverviewActivity : BaseActivity() {

    private lateinit var rv: RecyclerView
    private lateinit var adapter: HapticChartAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_haptic_overview)

        rv = findViewById(R.id.rvHapticCharts)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = HapticChartAdapter(
            context = this,
            onPreview = { item ->            // 미리듣기 콜백 (어댑터에서 호출)
                HapticChartAdapter.vibrateWaveform(this, item.pattern, item.amps, item.repeat)
            }
        )
        rv.adapter = adapter

        // 감정 목록(순서 원하는대로 조정 가능)
        val emotions = listOf("기쁨", "놀람", "화남", "슬픔")

        // 저장된 값 있으면 로드, 없으면 기본값 생성해서 리스트 구성
        val items = emotions.map { emo ->
            val loaded = HapticPrefs.load(this, emo) // Triple<LongArray, IntArray, Int>?
            if (loaded != null) {
                val (p, a, r) = loaded
                HapticItem(
                    emotion = emo,
                    isCustom = true,
                    pattern = p,
                    amps = a,
                    repeat = r
                )
            } else {
                val (p, a) = defaultPattern(emo)  // 기본 프리셋에서 패턴 생성
                HapticItem(
                    emotion = emo,
                    isCustom = false,
                    pattern = p,
                    amps = a,
                    repeat = -1
                )
            }
        }

        adapter.submit(items)
    }

    /**
     * HapticEditorActivity에 쓰던 기본 프리셋과 동일한 규칙으로
     * 감정별 기본 패턴(times, amps) 생성
     */
    private fun defaultPattern(emotion: String): Pair<LongArray, IntArray> {
        // 프리셋: (pulses, pulseMs, gapMs, startAmp, endAmp)
        val preset = when (emotion) {
            "기쁨" -> Quint(3, 200, 100, 130, 250)
            "놀람" -> Quint(2,  150, 1000, 255, 255)
            "화남" -> Quint(3, 150,  50, 200, 200)
            else   -> Quint(3,1200, 400, 130,  70) // "슬픔"
        }
        return buildLinearPattern(
            pulses = preset.pulses,
            pulseMs = preset.pulseMs,
            gapMs   = preset.gapMs,
            startAmp = preset.startAmp,
            endAmp   = preset.endAmp
        )
    }

    // 간단 유틸
    private data class Quint(
        val pulses: Int,
        val pulseMs: Int,
        val gapMs: Int,
        val startAmp: Int,
        val endAmp: Int
    )

    /** HapticEditorActivity와 동일한 규칙의 선형 패턴 생성 */
    private fun buildLinearPattern(
        pulses: Int, pulseMs: Int, gapMs: Int, startAmp: Int, endAmp: Int
    ): Pair<LongArray, IntArray> {
        val times = ArrayList<Long>()
        val amps  = ArrayList<Int>()
        times += 0L; amps += 0 // 시작 지연 0
        val clampedStart = startAmp.coerceIn(0, 255)
        val clampedEnd   = endAmp.coerceIn(0, 255)

        for (i in 0 until pulses) {
            val a = clampedStart + ((clampedEnd - clampedStart) * i) / maxOf(1, pulses - 1)
            times += pulseMs.toLong(); amps += a
            if (i < pulses - 1 && gapMs > 0) { times += gapMs.toLong(); amps += 0 }
        }
        return times.toLongArray() to amps.toIntArray()
    }
}
