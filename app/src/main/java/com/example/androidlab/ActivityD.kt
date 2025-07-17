package com.example.androidlab

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class ActivityD : AppCompatActivity() {

    lateinit var testScoreDatabase: TestScoreDatabase
    lateinit var myFaceScoreDatabase: MyFaceScoreDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_d)

        val testScoreChart = findViewById<LineChart>(R.id.testScoreChart)
        val myFaceScoreChart = findViewById<LineChart>(R.id.myFaceScoreChart)

        testScoreDatabase = Room.databaseBuilder(
            applicationContext,
            TestScoreDatabase::class.java,
            "test_score_db"
        ).build()

        myFaceScoreDatabase = Room.databaseBuilder(
            applicationContext,
            MyFaceScoreDatabase::class.java,
            "my_face_score_db"
        ).build()

        lifecycleScope.launch {
            // 더미 데이터 삽입 (비어있을 때만)
            if (testScoreDatabase.TestScoreDAO().getCount() == 0) {
                testScoreDatabase.TestScoreDAO().insertAll(
                    listOf(
                        TestScore(date = LocalDateTime.now().minusDays(3), emotion1Correct = 5, emotion1Wrong = 1, emotion2Correct = 4, emotion2Wrong = 2, emotion3Correct = 6, emotion3Wrong = 0, emotion4Correct = 3, emotion4Wrong = 3),
                        TestScore(date = LocalDateTime.now().minusDays(2), emotion1Correct = 3, emotion1Wrong = 3, emotion2Correct = 7, emotion2Wrong = 1, emotion3Correct = 5, emotion3Wrong = 1, emotion4Correct = 4, emotion4Wrong = 2),
                        TestScore(date = LocalDateTime.now().minusDays(1), emotion1Correct = 6, emotion1Wrong = 0, emotion2Correct = 5, emotion2Wrong = 2, emotion3Correct = 4, emotion3Wrong = 2, emotion4Correct = 5, emotion4Wrong = 1)
                    )
                )
            }
            if (myFaceScoreDatabase.MyFaceScoreDAO().getCount() == 0) {
                myFaceScoreDatabase.MyFaceScoreDAO().insertAll(
                    listOf(
                        MyFaceScore(date = LocalDateTime.now().minusDays(3), emotion1Score = 70, emotion2Score = 65, emotion3Score = 80, emotion4Score = 60),
                        MyFaceScore(date = LocalDateTime.now().minusDays(2), emotion1Score = 75, emotion2Score = 60, emotion3Score = 78, emotion4Score = 63),
                        MyFaceScore(date = LocalDateTime.now().minusDays(1), emotion1Score = 80, emotion2Score = 70, emotion3Score = 75, emotion4Score = 65)
                    )
                )
            }

            // 데이터 다시 불러와서 차트 그리기
            val testScoreData = testScoreDatabase.TestScoreDAO().getAllSortedByDate()
            val faceScoreData = myFaceScoreDatabase.MyFaceScoreDAO().getAllSortedByDate()

            drawTestScoreLineChart(testScoreChart, testScoreData)
            drawFaceScoreLineChart(myFaceScoreChart, faceScoreData)
        }
    }


    private fun drawTestScoreLineChart(chart: LineChart, data: List<TestScore>) {
        val entries = data.mapIndexed { index, score ->
            Entry(index.toFloat(), (score.emotion1Correct + score.emotion2Correct + score.emotion3Correct + score.emotion4Correct).toFloat())
        }

        val lineDataSet = LineDataSet(entries, "Test Score").apply {
            color = getColor(R.color.purple_500)
            valueTextSize = 10f
            setDrawCircles(true)
            setDrawValues(true)
        }

        chart.data = LineData(lineDataSet)
        chart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            valueFormatter = IndexAxisValueFormatter(data.map { it.date.toLocalDate().toString() })
            granularity = 1f
            labelRotationAngle = -45f
        }
        chart.axisRight.isEnabled = false
        chart.description = Description().apply { text = "" }
        chart.invalidate()

        chart.setOnChartValueSelectedListener(object : com.github.mikephil.charting.listener.OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                e?.x?.toInt()?.let { index ->
                    showBarChartDialog(data[index])
                }
            }

            override fun onNothingSelected() {}
        })
    }

    private fun drawFaceScoreLineChart(chart: LineChart, data: List<MyFaceScore>) {
        val entries = data.mapIndexed { index, item ->
            val totalScore = item.emotion1Score + item.emotion2Score + item.emotion3Score + item.emotion4Score
            Entry(index.toFloat(), totalScore.toFloat())
        }


        val lineDataSet = LineDataSet(entries, "Face Score").apply {
            color = getColor(R.color.teal_700)
            valueTextSize = 10f
            setDrawCircles(true)
            setDrawValues(true)
        }

        chart.data = LineData(lineDataSet)
        chart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            valueFormatter = IndexAxisValueFormatter(data.map { it.date.toLocalDate().toString() })
            granularity = 1f
            labelRotationAngle = -45f
        }
        chart.axisRight.isEnabled = false
        chart.description = Description().apply { text = "" }
        chart.invalidate()

        chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                e?.x?.toInt()?.let { index ->
                    showFaceScoreDialog(data[index])
                }
            }
            override fun onNothingSelected() {}
        })

    }

    private fun showBarChartDialog(score: TestScore) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_bar_chart, null)
        val chart = dialogView.findViewById<BarChart>(R.id.dialogBarChart)
        val closeButton = dialogView.findViewById<Button>(R.id.closeButton)

        drawHorizontalBarChart(chart, score)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        closeButton.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun drawHorizontalBarChart(chart: BarChart, score: TestScore) {
        val corrects = listOf(score.emotion1Correct, score.emotion2Correct, score.emotion3Correct, score.emotion4Correct)
        val wrongs = listOf(score.emotion1Wrong, score.emotion2Wrong, score.emotion3Wrong, score.emotion4Wrong)

        val entries = corrects.indices.map { index ->
            BarEntry(index.toFloat(), floatArrayOf(corrects[index].toFloat(), wrongs[index].toFloat()))
        }

        val dataSet = BarDataSet(entries, "감정 정오답").apply {
            setColors(intArrayOf(R.color.blue, R.color.red), this@ActivityD)
            stackLabels = arrayOf("맞음", "틀림")
        }

        chart.data = BarData(dataSet).apply {
            barWidth = 0.5f
        }

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

    private fun showFaceScoreDialog(score: MyFaceScore) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("감정별 점수 상세")

        val message = """
        감정1 점수: ${score.emotion1Score}
        감정2 점수: ${score.emotion2Score}
        감정3 점수: ${score.emotion3Score}
        감정4 점수: ${score.emotion4Score}
    """.trimIndent()

        builder.setMessage(message)
        builder.setPositiveButton("닫기") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

}
