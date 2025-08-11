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
import kotlin.math.abs

class TestScoreAdapter(private val onItemClick: (TestScore) -> Unit) : ListAdapter<TestScore, TestScoreAdapter.TestScoreViewHolder>(DiffCallback()) {

    // 4개 레이아웃 리소스
    private val layouts = intArrayOf(
        R.layout.item_score1,
        R.layout.item_score2,
        R.layout.item_score3,
        R.layout.item_score4
    )

    // position 기반 순환 (0,1,2,3 반복). 날짜/ID 기반으로 고정하려면 아래 주석 해제해서 사용.
    override fun getItemViewType(position: Int): Int {
        // return position % 4
        // 날짜/ID 안정 해시 기반: 같은 항목은 늘 같은 레이아웃
        val item = getItem(position)
        val key = item.id ?: item.date.hashCode() // id가 nullable이면 date로 대체
        return abs(key) % 4
    }

    inner class TestScoreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateText = itemView.findViewById<TextView>(R.id.textDate)
        private val totalScoreText = itemView.findViewById<TextView>(R.id.textTotalScore)

        fun bind(item: TestScore) {
            val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
            dateText.text = item.date.format(formatter)
            val totalScore = item.emotion1Correct + item.emotion2Correct + item.emotion3Correct + item.emotion4Correct
            totalScoreText.text = "총합: $totalScore 점"

            itemView.setOnClickListener { onItemClick(item) }
        }
    }
    /*
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestScoreViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_score1, parent, false)
        return TestScoreViewHolder(view)
    }
    */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestScoreViewHolder {
        val layoutRes = layouts[viewType]                   // ✅ viewType으로 선택
        val view = LayoutInflater.from(parent.context)
            .inflate(layoutRes, parent, false)
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
