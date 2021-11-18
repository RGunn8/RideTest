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
import com.ryangunn.ridetest.database.model.Route
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
        historyViewModel.getRoute(requireContext())
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                historyViewModel.route.collect {
                    binding.apply {
                        if (it.isEmpty()) {
                            displayNoRouteTextView()
                        } else {
                            showRecyclerView(it)
                        }
                    }
                }
            }
        }
    }

    private fun FragmentHistoryBinding.showRecyclerView(it: List<Route>) {
        historyRecyclerView.visibility = View.VISIBLE
        noRouteTextView.visibility = View.GONE
        val adapter = HistoryAdapter(it)
        historyRecyclerView.adapter = adapter
        historyRecyclerView.layoutManager =
            LinearLayoutManager(requireContext())
    }

    private fun FragmentHistoryBinding.displayNoRouteTextView() {
        historyRecyclerView.visibility = View.GONE
        noRouteTextView.visibility = View.VISIBLE
    }


}