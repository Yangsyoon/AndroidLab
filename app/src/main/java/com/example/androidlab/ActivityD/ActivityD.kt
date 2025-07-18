package com.example.androidlab.ActivityD

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.androidlab.R
import com.example.androidlab.database.MyFaceScore
import com.example.androidlab.database.MyFaceScoreDatabase
import com.example.androidlab.database.TestScore
import com.example.androidlab.database.TestScoreDatabase
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class ActivityD : AppCompatActivity() {

    lateinit var testScoreDatabase: TestScoreDatabase
    lateinit var myFaceScoreDatabase: MyFaceScoreDatabase

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_d)

        /*val testScoreChart = findViewById<LineChart>(R.id.testScoreChart)
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
            // 더미 데이터 삽입 (비어있으면)
            if (testScoreDatabase.TestScoreDAO().getCount() == 0) {
                testScoreDatabase.TestScoreDAO().insertAll(
                    listOf(
                        TestScore(
                            date = LocalDateTime.now().minusDays(3),
                            emotion1Correct = 5,
                            emotion1Wrong = 1,
                            emotion2Correct = 4,
                            emotion2Wrong = 2,
                            emotion3Correct = 6,
                            emotion3Wrong = 0,
                            emotion4Correct = 3,
                            emotion4Wrong = 3
                        ),
                        TestScore(
                            date = LocalDateTime.now().minusDays(2),
                            emotion1Correct = 3,
                            emotion1Wrong = 3,
                            emotion2Correct = 7,
                            emotion2Wrong = 1,
                            emotion3Correct = 5,
                            emotion3Wrong = 1,
                            emotion4Correct = 4,
                            emotion4Wrong = 2
                        ),
                        TestScore(
                            date = LocalDateTime.now().minusDays(1),
                            emotion1Correct = 6,
                            emotion1Wrong = 0,
                            emotion2Correct = 5,
                            emotion2Wrong = 2,
                            emotion3Correct = 4,
                            emotion3Wrong = 2,
                            emotion4Correct = 5,
                            emotion4Wrong = 1
                        )
                    )
                )
            }
            if (myFaceScoreDatabase.MyFaceScoreDAO().getCount() == 0) {
                myFaceScoreDatabase.MyFaceScoreDAO().insertAll(
                    listOf(
                        MyFaceScore(
                            date = LocalDateTime.now().minusDays(3),
                            emotion1Score = 70,
                            emotion2Score = 65,
                            emotion3Score = 80,
                            emotion4Score = 60
                        ),
                        MyFaceScore(
                            date = LocalDateTime.now().minusDays(2),
                            emotion1Score = 75,
                            emotion2Score = 60,
                            emotion3Score = 78,
                            emotion4Score = 63
                        ),
                        MyFaceScore(
                            date = LocalDateTime.now().minusDays(1),
                            emotion1Score = 80,
                            emotion2Score = 70,
                            emotion3Score = 75,
                            emotion4Score = 65
                        )
                    )
                )
            }

            val testScoreData = testScoreDatabase.TestScoreDAO().getAllSortedByDate()
            val faceScoreData = myFaceScoreDatabase.MyFaceScoreDAO().getAllSortedByDate()

            //drawTestScoreLineChart(testScoreChart, testScoreData)
            //drawFaceScoreLineChart(myFaceScoreChart, faceScoreData)
        }*/
    }

    /*@RequiresApi(Build.VERSION_CODES.O)
    private fun drawTestScoreLineChart(chart: LineChart, data: List<TestScore>) {
        val totalVisiblePoints = 7
        val dataSize = data.size
        val startX = if (dataSize < totalVisiblePoints) (totalVisiblePoints - dataSize) else 0

        val entries = data.mapIndexed { index, score ->
            val totalCorrect = score.emotion1Correct + score.emotion2Correct + score.emotion3Correct + score.emotion4Correct
            Entry((startX + index).toFloat(), totalCorrect.toFloat())
        }

        val lineDataSet = LineDataSet(entries, "").apply {
            color = getColor(R.color.purple_500)
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
                    // startX 보정해서 실제 데이터 인덱스 계산
                    val dataIndex = index - startX
                    if (dataIndex in data.indices) {
                        showBarChartDialog(data[dataIndex])
                    }
                }
            }
            override fun onNothingSelected() {}
        })
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
            color = getColor(R.color.teal_700)
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

        val entries = corrects.indices.map { i ->
            BarEntry(i.toFloat(), floatArrayOf(corrects[i].toFloat(), wrongs[i].toFloat()))
        }

        val dataSet = BarDataSet(entries, "감정 탐정 점수").apply {
            setColors(intArrayOf(R.color.blue, R.color.red), this@ActivityD)
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

    private fun showFaceScoreDialog(score: MyFaceScore) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("표정 짓기 점수")

        val message = """
            감정1: ${score.emotion1Score}점
            감정2: ${score.emotion2Score}점
            감정3: ${score.emotion3Score}점
            감정4: ${score.emotion4Score}점
        """.trimIndent()

        builder.setMessage(message)
        builder.setPositiveButton("닫기") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }*/
}