package com.example.androidlab.ActivityD

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.androidlab.R
import com.example.androidlab.database.MyFaceScore
import java.time.format.DateTimeFormatter

class MyFaceScoreAdapter(private val onItemClick: (MyFaceScore) -> Unit) : ListAdapter<MyFaceScore, MyFaceScoreAdapter.FaceScoreViewHolder>(DiffCallback()) {

    inner class FaceScoreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateText = itemView.findViewById<TextView>(R.id.textDate)
        private val totalScoreText = itemView.findViewById<TextView>(R.id.textTotalScore)

        fun bind(item: MyFaceScore) {
            val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
            dateText.text = item.date.format(formatter)
            val totalScore = item.emotion1Correct + item.emotion2Correct + item.emotion3Correct + item.emotion4Correct + item.emotion5Correct + item.emotion6Correct + item.emotion7Correct
            val wrongScore = item.emotion1Wrong + item.emotion2Wrong + item.emotion3Wrong + item.emotion4Wrong + item.emotion5Wrong + item.emotion6Wrong + item.emotion7Wrong

            val final_score=(totalScore*100/(totalScore+wrongScore)).toInt()

            totalScoreText.text = "총합: $final_score 점"

            itemView.setOnClickListener { onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaceScoreViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_face_score, parent, false)
        return FaceScoreViewHolder(view)
    }

    override fun onBindViewHolder(holder: FaceScoreViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<MyFaceScore>() {
        override fun areItemsTheSame(oldItem: MyFaceScore, newItem: MyFaceScore): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: MyFaceScore, newItem: MyFaceScore): Boolean = oldItem == newItem
    }
}
