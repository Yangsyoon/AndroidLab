package com.example.androidlab.Guardian

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.androidlab.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import kotlin.math.max

class HapticChartAdapter(
    private val context: Context,
    private val onPreview: (HapticItem) -> Unit
) : RecyclerView.Adapter<HapticChartAdapter.VH>() {

    private val items = mutableListOf<HapticItem>()

    fun submit(newItems: List<HapticItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_haptic_chart, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.bind(item, onPreview)
    }

    override fun getItemCount(): Int = items.size

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {

        private val ivIcon: ImageView = view.findViewById(R.id.ivIcon)
        private val tvEmotion: TextView = view.findViewById(R.id.tvEmotion)
        private val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        private val tvSummary: TextView = view.findViewById(R.id.tvSummary)
        private val btnPreview: ImageButton = view.findViewById(R.id.btnPreview)
        private val chart: LineChart = view.findViewById(R.id.chart)

        init {
            initChart(chart)
        }

        fun bind(item: HapticItem, onPreview: (HapticItem) -> Unit) {
            tvEmotion.text = item.emotion
            tvStatus.text = if (item.isCustom) "커스텀" else "기본"
            tvStatus.setTextColor(if (item.isCustom) Color.parseColor("#004D40") else Color.parseColor("#1A237E"))

            // 요약(총 길이, 피크, 펄스 수)
            val totalMs = item.pattern.sum()
            val peak = item.amps.maxOrNull() ?: 0
            val pulses = estimatePulses(item.pattern)
            tvSummary.text = "총 길이: ${totalMs} ms · 피크: $peak · 펄스: $pulses"

            // 그래프 데이터
            renderChart(chart, item.pattern, item.amps)

            btnPreview.setOnClickListener { onPreview(item) }
        }

        private fun estimatePulses(pattern: LongArray): Int {
            // 패턴이 [on, off, on, off, ...] 구조라고 가정 (시작지연 0 포함)
            // on/off 쌍 수를 대략 추정
            if (pattern.isEmpty()) return 0
            // times[0]=0은 시작지연으로 넣었다고 가정 -> 이후 on/off 구간 개수 계산
            val segments = max(0, pattern.size - 1)
            return (segments + 1) / 2
        }
    }

    // ======== Chart Helpers ========

    private fun initChart(chart: LineChart) {
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
            axisMaximum = 255f
            granularity = 25f
        }
        chart.axisRight.isEnabled = false
        chart.legend.isEnabled = false
    }

    private fun renderChart(chart: LineChart, pattern: LongArray, amps: IntArray) {
        val entries = toStaircaseEntries(pattern, amps)
        val ds = LineDataSet(entries, "Haptic Envelope").apply {
            mode = LineDataSet.Mode.STEPPED
            lineWidth = 2f
            setDrawCircles(false)
            setDrawValues(false)
            setDrawFilled(true)
            fillAlpha = 60
            highLightColor = Color.BLACK
            highlightLineWidth = 1.2f
        }
        chart.data = LineData(ds)
        chart.xAxis.axisMinimum = 0f
        chart.xAxis.axisMaximum = (pattern.sum()).toFloat()
        chart.highlightValues(null)
        chart.invalidate()
    }

    private fun toStaircaseEntries(pattern: LongArray, amps: IntArray): List<Entry> {
        val out = ArrayList<Entry>()
        var t = 0L
        out += Entry(0f, 0f)
        for (i in pattern.indices) {
            val dur = pattern[i]
            val amp = if (i < amps.size) amps[i] else 0
            out += Entry(t.toFloat(), amp.toFloat())
            t += dur
            out += Entry(t.toFloat(), amp.toFloat())
        }
        return out
    }

    // ======== Preview Helper (진동 재생) ========
    companion object {
        fun vibrateWaveform(context: Context, pattern: LongArray, amps: IntArray, repeat: Int = -1) {
            val vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
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
    }
}
