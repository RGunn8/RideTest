package com.ryangunn.ridetest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.ryangunn.ridetest.database.MoveDatabase
import com.ryangunn.ridetest.database.model.Moves
import com.ryangunn.ridetest.databinding.DialogFinishBinding
import kotlinx.coroutines.launch


class FinishDialog : DialogFragment() {
    private var _binding: DialogFinishBinding? = null
    private val binding get() = _binding!!
    private val args: FinishDialogArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFinishBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                val move =
                    MoveDatabase.getDatabase(requireContext())?.moveDAO()?.getMove(args.moveId)
                move?.let {
                    displayMove(move)
                }
            }
        }
    }

    private fun displayMove(move: Moves) {
        binding.apply {
            moveImageView.setImageBitmap(move.img)
            distanceTextView.text =
                getString(R.string.move_total_distance, move.getDistanceInString())
            timeTextView.text = getString(R.string.move_total_time, move.time)
            okayTextView.setOnClickListener { dismiss() }
        }
    }
}