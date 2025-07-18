package com.example.androidlab.ActivityD


import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.androidlab.R
import com.example.androidlab.database.TestScore
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.collections.map
import kotlin.collections.mapIndexed

class TestScoreGraphFragment : Fragment(R.layout.fragment_test_score_graph) {

    private val viewModel: TestScoreViewModel by activityViewModels()
    private lateinit var lineChart: LineChart

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lineChart = view.findViewById(R.id.testScoreChart)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.testScores.collectLatest { scores ->
                drawTestScoreLineChart(lineChart, scores)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun drawTestScoreLineChart(chart: LineChart, data: List<TestScore>) {
        val totalVisiblePoints = 7
        val dataSize = data.size
        val startX = if (dataSize < totalVisiblePoints) (totalVisiblePoints - dataSize) else 0

        val entries = data.mapIndexed { index, item ->
            val totalScore = item.emotion1Correct + item.emotion1Correct + item.emotion1Correct + item.emotion1Correct
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
                        showBarChartDialog(data[dataIndex])
                    }
                }
            }

            override fun onNothingSelected() {}
        })
    }

    private fun showBarChartDialog(score: TestScore) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_bar_chart, null)
        val chart = dialogView.findViewById<BarChart>(R.id.dialogBarChart)
        val closeButton = dialogView.findViewById<Button>(R.id.closeButton)

        drawHorizontalBarChart(chart, score)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        closeButton.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
    private fun drawHorizontalBarChart(chart: BarChart, score: TestScore) {
        val corrects = listOf(score.emotion1Correct, score.emotion2Correct, score.emotion3Correct, score.emotion4Correct)
        val wrongs = listOf(score.emotion1Wrong, score.emotion2Wrong, score.emotion3Wrong, score.emotion4Wrong)

        val entries = corrects.indices.map { i ->
            BarEntry(i.toFloat(), floatArrayOf(corrects[i].toFloat(), wrongs[i].toFloat()))
        }

        val dataSet = BarDataSet(entries, "감정 탐정 점수").apply {
            setColors(intArrayOf(R.color.blue, R.color.red), requireContext())
            stackLabels = arrayOf("맞음", "틀림")
        }

        chart.data = BarData(dataSet).apply { barWidth = 0.5f }

        chart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(listOf("감정1", "감정2", "감정3", "감정4"))
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
        }

        chart.axisLeft.axisMinimum = 0f
        chart.axisRight.isEnabled = false
        chart.description = Description().apply { text = "" }
        chart.invalidate()
    }
}
