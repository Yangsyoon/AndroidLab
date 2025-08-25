package com.example.androidlab.Guardian

import android.animation.ValueAnimator
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import com.example.androidlab.BaseActivity
import com.example.androidlab.R
import com.example.androidlab.utils.HapticPrefs
import com.example.androidlab.utils.Haptics

// MPAndroidChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
// 커스텀 NumberPicker
import com.shawnlin.numberpicker.NumberPicker as HNumberPicker
class HapticEditorActivity : BaseActivity() {

    private lateinit var rgEmotion: RadioGroup
    //private lateinit var npPulses: NumberPicker
    private lateinit var npPulses: HNumberPicker
    private lateinit var seekPulseMs: SeekBar
    private lateinit var seekGapMs: SeekBar
    private lateinit var seekStartAmp: SeekBar
    private lateinit var seekEndAmp: SeekBar
    private lateinit var tvPulseMs: TextView
    private lateinit var tvGapMs: TextView
    private lateinit var tvStartAmp: TextView
    private lateinit var tvEndAmp: TextView

    // 그래프 관련
    private lateinit var chart: LineChart
    private var playheadAnimator: ValueAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_haptic_editor)

        rgEmotion = findViewById(R.id.rgEmotion)
        npPulses = findViewById(R.id.npPulses)
        seekPulseMs = findViewById(R.id.seekPulseMs)
        seekGapMs = findViewById(R.id.seekGapMs)
        seekStartAmp = findViewById(R.id.seekStartAmp)
        seekEndAmp = findViewById(R.id.seekEndAmp)
        tvPulseMs = findViewById(R.id.tvPulseMs)
        tvGapMs = findViewById(R.id.tvGapMs)
        tvStartAmp = findViewById(R.id.tvStartAmp)
        tvEndAmp = findViewById(R.id.tvEndAmp)

        // 차트
        chart = findViewById(R.id.hapticChart)
        initChart()

        // NumberPicker
        npPulses.minValue = 1
        npPulses.maxValue = 8
        npPulses.value = 3
        // ★ 펄스 개수(NumberPicker) 변경 시 즉시 그래프 갱신
        npPulses.setOnValueChangedListener { _, oldVal, newVal ->
            if (oldVal != newVal) {
                refreshChart()                 // 그래프 재그리기
                playheadAnimator?.cancel()     // 진행 커서 정지
                chart.highlightValues(null)    // 하이라이트(커서) 초기화
            }
        }
        // ★ (선택) 스크롤 중에도 자주 갱신하고 싶으면 주석 해제

        npPulses.setOnScrollListener { _, state ->
            if (state != NumberPicker.OnScrollListener.SCROLL_STATE_IDLE) {
                // 스크롤 중간 갱신 (너무 잦으면 끊김 있을 수 있음)
                // refreshChart()
            } else {
                // 멈췄을 때 확정 갱신
                refreshChart()
            }
        }

        // 라벨 업데이트
        fun updateLabels() {
            tvPulseMs.text  = "펄스 길이: ${seekPulseMs.progress} ms"
            tvGapMs.text    = "쉼 길이: ${seekGapMs.progress} ms"
            tvStartAmp.text = "강도 시작: ${seekStartAmp.progress}"
            tvEndAmp.text   = "강도 끝: ${seekEndAmp.progress}"
        }

        // 슬라이더 변경 → 라벨 + 그래프 갱신
        listOf(seekPulseMs, seekGapMs, seekStartAmp, seekEndAmp).forEach { sb ->
            sb.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    updateLabels()
                    refreshChart()
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }

        // 감정 변경 시 저장본 로드 or 기본값 → 그래프 갱신
        rgEmotion.setOnCheckedChangeListener { _, _ ->
            loadOrDefault(emotion())
            refreshChart()
        }
        loadOrDefault(emotion())
        updateLabels()
        refreshChart() // 최초 1회

        // 미리보기
        findViewById<Button>(R.id.btnPreview).setOnClickListener {
            val (pattern, amps) = buildLinearPattern(
                pulses = npPulses.value,
                pulseMs = seekPulseMs.progress,
                gapMs = seekGapMs.progress,
                startAmp = seekStartAmp.progress,
                endAmp = seekEndAmp.progress
            )
            preview(pattern, amps)        // 실제 진동 재생
            startPlayhead(pattern.sum())  // 재생 커서 동기화
        }

        // 저장
        findViewById<Button>(R.id.btnSave).setOnClickListener {
            val (pattern, amps) = buildLinearPattern(
                pulses = npPulses.value,
                pulseMs = seekPulseMs.progress,
                gapMs = seekGapMs.progress,
                startAmp = seekStartAmp.progress,
                endAmp = seekEndAmp.progress
            )
            HapticPrefs.save(this, emotion(), pattern, amps, -1)
            Toast.makeText(this, "저장되었습니다.", Toast.LENGTH_SHORT).show()
        }

        // 기본값으로 초기화(커스텀 삭제 + 기본값 UI 세팅)
        findViewById<Button>(R.id.btnReset).setOnClickListener {
            HapticPrefs.clear(this, emotion())
            loadOrDefault(emotion())
            updateLabels()
            refreshChart()
            Toast.makeText(this, "기본값으로 복원했어요.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPause() {
        super.onPause()
        Haptics.cancel(this) // 혹시 남아있는 진동 종료
        playheadAnimator?.cancel()
        chart.highlightValues(null)
    }

    /** 선택된 감정 문자열 */
    private fun emotion(): String = when (rgEmotion.checkedRadioButtonId) {
        R.id.rbJoy      -> "기쁨"
        R.id.rbSurprise -> "놀람"
        R.id.rbAnger    -> "화남"
        else            -> "슬픔"
    }

    /** 저장된 값이 있으면 UI에 로드, 없으면 기본 프리셋 */
    private fun loadOrDefault(emotion: String) {
        val loaded = HapticPrefs.load(this, emotion)
        if (loaded != null) {
            val (p, a, _) = loaded
            // 단순 선형 에디터에 맞춰 첫 구간만 역산해서 채워줌(최소 동작)
            val pulses = (p.size + 1) / 2 // 0(딜레이) 없이 ON/OFF 쌍 기준
            npPulses.value = pulses.coerceIn(1, 8)
            seekPulseMs.progress = p.getOrNull(1)?.toInt() ?: 200
            seekGapMs.progress   = p.getOrNull(2)?.toInt() ?: 100
            seekStartAmp.progress=  a.getOrNull(1)  ?: 170
            seekEndAmp.progress  = a.lastOrNull()  ?: 210
        } else {
            when (emotion) {
                "기쁨" -> { npPulses.value = 3; seekPulseMs.progress = 200;  seekGapMs.progress = 100; seekStartAmp.progress = 130; seekEndAmp.progress = 250 }
                "놀람" -> { npPulses.value = 2; seekPulseMs.progress = 150;   seekGapMs.progress = 1000; seekStartAmp.progress = 255; seekEndAmp.progress = 255 }
                "화남" -> { npPulses.value = 3; seekPulseMs.progress = 150;  seekGapMs.progress = 50;  seekStartAmp.progress = 200; seekEndAmp.progress = 200 }
                "슬픔" -> { npPulses.value = 3; seekPulseMs.progress = 1200; seekGapMs.progress = 400; seekStartAmp.progress = 130; seekEndAmp.progress = 70 }
            }
        }
    }

    /** 펄스-쉼 반복의 선형 패턴 생성 */
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

    /** 액티비티 내부 미리보기(반복 없음) */
    private fun preview(pattern: LongArray, amps: IntArray, repeat: Int = -1) {
        val vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        } ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = if (vib.hasAmplitudeControl())
                VibrationEffect.createWaveform(pattern, amps, repeat)
            else
                VibrationEffect.createWaveform(pattern, repeat)
            vib.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vib.vibrate(pattern, repeat)
        }
    }

    // ========= 그래프 관련 유틸 =========

    /** 차트 초기 설정 */
    private fun initChart() {
        chart.description.isEnabled = false
        chart.setTouchEnabled(true)
        chart.isDragEnabled = true
        chart.setScaleEnabled(true)
        chart.setPinchZoom(true)

        chart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 50f
            setDrawGridLines(false)
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String = "${value.toInt()} ms"
            }
        }
        chart.axisLeft.apply {
            axisMinimum = 0f
            axisMaximum = 255f   // 진폭 0~255
            granularity = 25f
        }
        chart.axisRight.isEnabled = false
        chart.legend.isEnabled = false
    }

    /** 현재 UI 값으로 파형을 새로 그리기 */
    private fun refreshChart() {
        val (pattern, amps) = buildLinearPattern(
            pulses = npPulses.value,
            pulseMs = seekPulseMs.progress,
            gapMs = seekGapMs.progress,
            startAmp = seekStartAmp.progress,
            endAmp = seekEndAmp.progress
        )
        val entries = toStaircaseEntries(pattern, amps)
        val ds = LineDataSet(entries, "Haptic Envelope").apply {
            mode = LineDataSet.Mode.STEPPED       // 계단형
            lineWidth = 2f
            setDrawCircles(false)
            setDrawValues(false)
            setDrawFilled(true)
            fillAlpha = 60
            highLightColor = 0xFF000000.toInt()
            highlightLineWidth = 1.2f
        }
        chart.data = LineData(ds)
        chart.xAxis.axisMinimum = 0f
        chart.xAxis.axisMaximum = (pattern.sum()).toFloat()
        chart.highlightValues(null)
        chart.invalidate()
    }

    /** Waveform(패턴/진폭)을 차트용 (t, amp) 점들로 ‘계단’ 변환 */
    private fun toStaircaseEntries(pattern: LongArray, amps: IntArray): List<Entry> {
        val out = ArrayList<Entry>()
        var t = 0L
        // 시작점(0,0) 표시
        out += Entry(0f, 0f)
        for (i in pattern.indices) {
            val dur = pattern[i]
            val amp = if (i < amps.size) amps[i] else 0
            // 구간 시작점(수직 모서리)
            out += Entry(t.toFloat(), amp.toFloat())
            t += dur
            // 구간 끝점
            out += Entry(t.toFloat(), amp.toFloat())
        }
        return out
    }

    /** 총 길이(ms) 동안 차트의 하이라이트 커서를 이동 */
    private fun startPlayhead(totalMs: Long) {
        playheadAnimator?.cancel()
        if (totalMs <= 0) return
        playheadAnimator = ValueAnimator.ofFloat(0f, totalMs.toFloat()).apply {
            duration = totalMs
            addUpdateListener { anim ->
                val x = anim.animatedValue as Float
                chart.highlightValues(arrayOf(Highlight(x, 0, 0)))
            }
            start()
        }
    }
}
