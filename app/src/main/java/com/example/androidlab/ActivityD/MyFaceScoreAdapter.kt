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
import java.time.format.DateTimeFormatter
import kotlin.math.abs

class MyFaceScoreAdapter(private val onItemClick: (MyFaceScore) -> Unit) :
    ListAdapter<MyFaceScore, MyFaceScoreAdapter.MyFaceScoreViewHolder>(DiffCallback()) {

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

    inner class MyFaceScoreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateText = itemView.findViewById<TextView>(R.id.textDate)
        private val totalScoreText = itemView.findViewById<TextView>(R.id.textTotalScore)

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(item: MyFaceScore) {
            val formatterOutput = DateTimeFormatter.ofPattern("yyyy/MM/dd")
            val dateStr = item.date.format(formatterOutput)
            dateText.text = dateStr

            val totalScore = item.angryCorrect + item.happyCorrect +
                    item.neutralCorrect + item.surprisedCorrect + item.sadCorrect
            val wrongScore = item.angryWrong + item.happyWrong +
                    item.neutralWrong + item.surprisedWrong + item.sadWrong

            val finalScore = if (totalScore + wrongScore > 0) {
                (totalScore * 100 / (totalScore + wrongScore)).toInt()
            } else {
                0
            }

            totalScoreText.text = "총합: $finalScore 점"

            itemView.setOnClickListener { onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyFaceScoreViewHolder {
        val layoutRes = layouts[viewType]
        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return MyFaceScoreViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: MyFaceScoreViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<MyFaceScore>() {
        override fun areItemsTheSame(oldItem: MyFaceScore, newItem: MyFaceScore): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: MyFaceScore, newItem: MyFaceScore): Boolean =
            oldItem == newItem
    }
}

