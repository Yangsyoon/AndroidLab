package com.example.androidlab.ActivityD

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androidlab.R

import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TestScoreListFragment : Fragment(R.layout.fragment_test_score_list) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TestScoreAdapter
    private val viewModel: TestScoreViewModel by activityViewModels()  // 공유 ViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView)
        adapter = TestScoreAdapter { score ->
            showBarChartDialog(score)
        }

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.testScores.collectLatest { list ->
                adapter.submitList(list)
            }
        }

    }

    private fun showBarChartDialog(score: TestScore) {
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
    private fun drawHorizontalBarChart(chart: HorizontalBarChart, score: TestScore) {
        val corrects = listOf(
            score.angryCorrect,
            score.happyCorrect,
            score.surprisedCorrect,
            score.sadCorrect
        )
        val wrongs = listOf(
            score.angryWrong,
            score.happyWrong,
            score.surprisedWrong,
            score.sadWrong
        )

        // 스택형 바 데이터 (맞음 + 틀림)
        val entries = corrects.indices.map { i ->
            BarEntry(i.toFloat(), floatArrayOf(corrects[i].toFloat(), wrongs[i].toFloat()))
        }

        val dataSet = BarDataSet(entries, "정답 개수").apply {
            setColors(intArrayOf(R.color.blue, R.color.red), requireContext())
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
            valueFormatter = IndexAxisValueFormatter(listOf("화남", "기쁨", "놀람", "슬픔"))
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
