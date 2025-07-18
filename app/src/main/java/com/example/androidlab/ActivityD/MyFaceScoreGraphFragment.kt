package com.example.androidlab.ActivityD


import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.androidlab.R
import com.example.androidlab.database.MyFaceScore
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
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
        val dataSize = data.size
        val startX = if (dataSize < totalVisiblePoints) (totalVisiblePoints - dataSize) else 0

        val entries = data.mapIndexed { index, item ->
            val totalScore = item.emotion1Score + item.emotion2Score + item.emotion3Score + item.emotion4Score
            Entry((startX + index).toFloat(), totalScore.toFloat())
        }

        val lineDataSet = LineDataSet(entries, "").apply {
            color = requireContext().getColor(R.color.teal_700)
            valueTextSize = 10f
            setDrawCircles(true)
            setDrawValues(true)
        }

        chart.data = LineData(lineDataSet)
        chart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            val labels = data.map { it.date.toLocalDate().toString() }
            val paddedLabels = if (dataSize < totalVisiblePoints) {
                List(totalVisiblePoints - dataSize) { "" } + labels
            } else {
                labels
            }
            valueFormatter = IndexAxisValueFormatter(paddedLabels)
            granularity = 1f
            labelRotationAngle = -45f
            axisMinimum = 0f
            axisMaximum = (totalVisiblePoints - 1).toFloat()
        }
        chart.axisRight.isEnabled = false
        chart.description = Description().apply { text = "" }
        chart.invalidate()

        chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                e?.x?.toInt()?.let { index ->
                    val dataIndex = index - startX
                    if (dataIndex in data.indices) {
                        showFaceScoreDialog(data[dataIndex])
                    }
                }
            }

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
