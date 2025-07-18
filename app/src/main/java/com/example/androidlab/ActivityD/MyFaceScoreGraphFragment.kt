package com.example.androidlab.ActivityD


import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.androidlab.R
import com.example.androidlab.database.MyFaceScore
import com.example.androidlab.database.TestScore
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MyFaceScoreGraphFragment : Fragment(R.layout.fragment_my_face_score_graph) {

    private val viewModel: MyFaceScoreViewModel by activityViewModels()
    private lateinit var lineChart: LineChart

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lineChart = view.findViewById(R.id.testScoreChart)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.faceScores.collectLatest { scores ->
                drawFaceScoreLineChart(lineChart, scores)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun drawFaceScoreLineChart(chart: LineChart, data: List<MyFaceScore>) {
        val totalVisiblePoints = 7
        val spacing = 2f // ✅ 데이터 간 간격 (x값 간격)
        val dataSize = data.size
        val startX = if (dataSize < totalVisiblePoints) (totalVisiblePoints - dataSize) else 0

        // ✅ Entry 만들기 (x 값에 spacing 반영)
        val entries = data.mapIndexed { index, item ->
            val totalScore = item.emotion1Score + item.emotion2Score + item.emotion3Score + item.emotion4Score
            Entry((startX + index) * spacing, totalScore.toFloat())
        }

        // ✅ DataSet 구성
        val lineDataSet = LineDataSet(entries, "").apply {
            color = requireContext().getColor(R.color.teal_700)
            valueTextSize = 10f
            setDrawCircles(true)
            setDrawValues(true)
        }

        chart.data = LineData(lineDataSet)

        // ✅ X축 설정
        chart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            granularity = spacing // x 값 간격
            labelRotationAngle = -45f
            axisMinimum = 0f

            // ✅ 전체 데이터가 많으면 scroll 가능하게
            axisMaximum = if (dataSize > totalVisiblePoints) {
                ((startX + dataSize - 1) * spacing)
            } else {
                ((totalVisiblePoints - 1) * spacing)
            }

            // ✅ 커스텀 레이블 포매터 (간격 맞춰 날짜 표시)
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val index = ((value / spacing).toInt()) - startX
                    return if (index in data.indices) {
                        data[index].date.toLocalDate().toString()
                    } else {
                        ""
                    }
                }
            }
        }

        // ✅ 차트 시각 옵션
        chart.axisRight.isEnabled = false
        chart.description = Description().apply { text = "" }
        chart.setTouchEnabled(true)
        chart.isDragEnabled = true
        chart.setScaleEnabled(true)
        chart.setPinchZoom(true)
        chart.setExtraOffsets(0f,0f,0f,40f)

        chart.setVisibleXRangeMaximum((totalVisiblePoints - 1) * spacing + 1) // ✅ 한 화면에 보이는 개수 제한
        chart.invalidate()

        // ✅ 단일 탭 시에만 다이얼로그 표시
        chart.setOnChartGestureListener(object : OnChartGestureListener {
            override fun onChartSingleTapped(me: MotionEvent) {
                val highlight = chart.getHighlightByTouchPoint(me.x, me.y)
                val entry = highlight?.let { chart.data.getEntryForHighlight(it) }
                if (entry != null) {
                    val index = (entry.x / spacing).toInt() - startX
                    if (index in data.indices) {
                        showFaceScoreDialog(data[index])
                    }
                }
            }

            override fun onChartGestureStart(me: MotionEvent?, lastPerformedGesture: ChartTouchListener.ChartGesture?) {}
            override fun onChartGestureEnd(me: MotionEvent?, lastPerformedGesture: ChartTouchListener.ChartGesture?) {}
            override fun onChartLongPressed(me: MotionEvent?) {}
            override fun onChartDoubleTapped(me: MotionEvent?) {}
            override fun onChartFling(me1: MotionEvent?, me2: MotionEvent?, velocityX: Float, velocityY: Float) {}
            override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {}
            override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {}
        })

        chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {}
            override fun onNothingSelected() {}
        })
    }

    private fun showFaceScoreDialog(score: MyFaceScore) {
        val message = """
            감정1: ${score.emotion1Score}점
            감정2: ${score.emotion2Score}점
            감정3: ${score.emotion3Score}점
            감정4: ${score.emotion4Score}점
        """.trimIndent()

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("표정 점수")
            .setMessage(message)
            .setPositiveButton("닫기") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}
