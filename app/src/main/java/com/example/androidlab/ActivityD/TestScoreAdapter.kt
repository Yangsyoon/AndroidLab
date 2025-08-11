package com.example.androidlab.ActivityD

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.androidlab.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs

class TestScoreAdapter(private val onItemClick: (TestScore) -> Unit) : ListAdapter<TestScore, TestScoreAdapter.TestScoreViewHolder>(DiffCallback()) {

    private val layouts = intArrayOf(
        R.layout.item_score1,
        R.layout.item_score2,
        R.layout.item_score3,
        R.layout.item_score4
    )

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        val key = item.id.hashCode()
        return abs(key) % 4
    }

    inner class TestScoreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateText = itemView.findViewById<TextView>(R.id.textDate)
        private val totalScoreText = itemView.findViewById<TextView>(R.id.textTotalScore)

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(item: TestScore) {
            val formatterInput = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val formatterOutput = DateTimeFormatter.ofPattern("yyyy/MM/dd")

            val dateStr = item.date.format(formatterOutput)
            dateText.text = dateStr

            val totalScore = item.emotion1Correct + item.emotion2Correct + item.emotion3Correct + item.emotion4Correct + item.emotion5Correct
            totalScoreText.text = "총합: $totalScore 점"

            itemView.setOnClickListener { onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestScoreViewHolder {
        val layoutRes = layouts[viewType]
        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return TestScoreViewHolder(view)
    }

    override fun onBindViewHolder(holder: TestScoreViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<TestScore>() {
        override fun areItemsTheSame(oldItem: TestScore, newItem: TestScore): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: TestScore, newItem: TestScore): Boolean = oldItem == newItem
    }
}
