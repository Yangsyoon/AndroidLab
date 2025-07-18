package com.example.androidlab.ActivityD

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androidlab.R
import com.example.androidlab.database.MyFaceScore
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
            showFaceScoreDialog(score)
        }

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.faceScores.collectLatest { list ->
                adapter.submitList(list)
            }
        }

    }

    private fun showFaceScoreDialog(score: MyFaceScore) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("표정 점수")

        val message = """
            감정1: ${score.emotion1Score}점
            감정2: ${score.emotion2Score}점
            감정3: ${score.emotion3Score}점
            감정4: ${score.emotion4Score}점
        """.trimIndent()

        builder.setMessage(message)
        builder.setPositiveButton("닫기") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }
}
