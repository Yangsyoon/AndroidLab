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
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MyFaceScoreListFragment : Fragment(R.layout.fragment_my_face_score_list) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MyFaceScoreAdapter
    private val viewModel: MyFaceScoreViewModel by activityViewModels()  // 공유 ViewModel


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView)
        adapter = MyFaceScoreAdapter { score ->
            showBarChartDialog(score)
        }

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.faceScores.collectLatest { list ->
                adapter.submitList(list)
            }
        }

    }

    private fun showBarChartDialog(score: MyFaceScore) {
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
    private fun drawHorizontalBarChart(chart: BarChart, score: MyFaceScore) {
        val corrects = listOf(score.angryCorrect,score.happyCorrect,score.neutralCorrect,score.surprisedCorrect,score.sadCorrect)
        val wrongs = listOf(score.angryWrong,score.happyWrong,score.neutralWrong,score.surprisedWrong,score.sadWrong)

        val entries = corrects.indices.map { i ->
            BarEntry(i.toFloat(), floatArrayOf(corrects[i].toFloat(), wrongs[i].toFloat()))
        }

        val dataSet = BarDataSet(entries, "내 표정 짓기 점수").apply {
            setColors(intArrayOf(R.color.blue, R.color.red), requireContext())
            stackLabels = arrayOf("맞음", "틀림")
        }

        chart.data = BarData(dataSet).apply { barWidth = 0.5f }

        chart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(listOf("화남", "기쁨", "무표정", "놀람","슬픔"))
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
        }

        chart.axisLeft.axisMinimum = 0f
        chart.axisRight.isEnabled = false
        chart.description = Description().apply { text = "" }
        chart.invalidate()
    }
}
