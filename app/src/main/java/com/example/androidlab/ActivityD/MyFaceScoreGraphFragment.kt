package com.example.androidlab.ActivityD


import android.app.AlertDialog
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.androidlab.R
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.HorizontalBarChart
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
        lineChart = view.findViewById(R.id.faceScoreChart)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.faceScores.collectLatest { scores ->
                drawScoreLineChart(lineChart, scores)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun drawScoreLineChart(chart: LineChart, data: List<MyFaceScore>) {
        val totalVisiblePoints = 7
        val spacing = 2f // ✅ 데이터 간 간격 (x값 간격)
        val dataSize = data.size
        val startX = if (dataSize < totalVisiblePoints) (totalVisiblePoints - dataSize) else 0

        // ✅ Entry 만들기 (x 값에 spacing 반영)
        val entries = data.mapIndexed { index, item ->
            val totalScore = item.angryCorrect + item.happyCorrect +
                    item.neutralCorrect + item.surprisedCorrect + item.sadCorrect
            val wrongScore = item.angryWrong + item.happyWrong +
                    item.neutralWrong + item.surprisedWrong + item.sadWrong

            val finalScore = if (totalScore + wrongScore > 0) {
                (totalScore * 100 / (totalScore + wrongScore)).toInt()
            } else {
                0
            }
            Entry((startX + index) * spacing, finalScore.toFloat())
        }

        // ✅ DataSet 구성
        val lineDataSet = LineDataSet(entries, "").apply {
            /*
            color = requireContext().getColor(R.color.teal_700)
            valueTextSize = 10f
            setDrawCircles(true)
            setDrawValues(true)
            */
            // ▶︎ 라인/포인트를 굵고 선명하게
            lineWidth = 3.5f
            setDrawCircles(true)
            circleRadius = 4.5f
            setDrawCircleHole(false)

            // ▶︎ 배경 이미지 위에서도 잘 보이도록 높은 대비 색상(필요시 변경)
            val fg = requireContext().getColor(android.R.color.holo_green_dark)
            color = fg
            setCircleColor(fg)
            valueTextColor = fg
            valueTextSize = 11f

            // ▶︎ 부드러운 곡선 + 채움(투명도)
            mode = LineDataSet.Mode.LINEAR
            setDrawFilled(true)
            fillColor = fg
            fillAlpha = 150   // 0~255

            // 하이라이트(선택 시) 색상
            highLightColor = requireContext().getColor(android.R.color.holo_orange_light)
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
                        data[index].date.toString()
                    } else {
                        ""
                    }
                }
            }
        }

        // ✅ 차트 시각 옵션
        chart.axisLeft.apply {
            axisMinimum = 0f
            axisMaximum = 100f
        }
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
                        showBarChartDialog(data[index])
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




    private fun showBarChartDialog(score: MyFaceScore) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_bar_chart, null)
        val chart = dialogView.findViewById<HorizontalBarChart>(R.id.dialogBarChart)
        val closeButton = dialogView.findViewById<Button>(R.id.closeButton)

        drawHorizontalBarChart(chart, score)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        closeButton.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
    private fun drawHorizontalBarChart(chart: HorizontalBarChart, score: MyFaceScore) {
        val corrects = listOf(
            score.angryCorrect,
            score.happyCorrect,
            score.neutralCorrect,
            score.surprisedCorrect,
            score.sadCorrect
        )
        val wrongs = listOf(
            score.angryWrong,
            score.happyWrong,
            score.neutralWrong,
            score.surprisedWrong,
            score.sadWrong
        )

        // 스택형 바 데이터 (맞음 + 틀림)
        val entries = corrects.indices.map { i ->
            BarEntry(i.toFloat(), floatArrayOf(corrects[i].toFloat(), wrongs[i].toFloat()))
        }

        val dataSet = BarDataSet(entries, "정답 개수").apply {
            setColors(intArrayOf(R.color.soft_green2, R.color.soft_green), requireContext())
            stackLabels = arrayOf("맞음", "틀림")
        }

        chart.data = BarData(dataSet).apply {
            barWidth = 0.5f
            setValueFormatter(object : ValueFormatter() { // 막대 위 숫자 정수 처리
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString()
                }
            })
        }

        // X축 (카테고리)
        chart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(listOf("화남", "기쁨", "무표정", "놀람", "슬픔"))
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            setDrawGridLines(false)
        }

        // Y축 (값, 정수만 표시)
        chart.axisLeft.apply {
            axisMinimum = 0f
            granularity = 1f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString()
                }
            }
        }

        chart.axisRight.isEnabled = false
        chart.description = Description().apply { text = "" }

        chart.invalidate()
    }

}
