package com.example.androidlab.ActivityD

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.androidlab.R
import com.example.androidlab.database.TestScore
import java.time.format.DateTimeFormatter

class TestScoreAdapter(private val onItemClick: (TestScore) -> Unit) : ListAdapter<TestScore, TestScoreAdapter.TestScoreViewHolder>(DiffCallback()) {

    inner class TestScoreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateText = itemView.findViewById<TextView>(R.id.textDate)
        private val totalScoreText = itemView.findViewById<TextView>(R.id.textTotalScore)

        fun bind(item: TestScore) {
            val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
            dateText.text = item.date.format(formatter)
            val totalScore = item.emotion1Correct + item.emotion1Correct + item.emotion1Correct + item.emotion1Correct
            totalScoreText.text = "총합: $totalScore 점"

            itemView.setOnClickListener { onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestScoreViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_score, parent, false)
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
