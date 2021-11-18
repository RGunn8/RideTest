package com.ryangunn.ridetest.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.ryangunn.ridetest.database.model.Moves
import com.ryangunn.ridetest.databinding.FragmentHistoryBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    lateinit var historyViewModel: HistoryViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        historyViewModel = ViewModelProvider(this)[HistoryViewModel::class.java]
        historyViewModel.getMoves(requireContext())
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                historyViewModel.moves.collect {
                    binding.apply {
                        if (it.isEmpty()) {
                            displayNoMoveTextView()
                        } else {
                            showRecyclerView(it)
                        }
                    }
                }
            }
        }
    }

    private fun FragmentHistoryBinding.showRecyclerView(it: List<Moves>) {
        historyRecyclerView.visibility = View.VISIBLE
        noMovesTextView.visibility = View.GONE
        val adapter = HistoryAdapter(it)
        historyRecyclerView.adapter = adapter
        historyRecyclerView.layoutManager =
            LinearLayoutManager(requireContext())
    }

    private fun FragmentHistoryBinding.displayNoMoveTextView() {
        historyRecyclerView.visibility = View.GONE
        noMovesTextView.visibility = View.VISIBLE
    }


}