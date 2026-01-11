package com.tapme.app.ui.memories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.MaterialToolbar
import com.tapme.app.R
import com.tapme.app.ui.memories.MemoriesViewModel
import com.tapme.app.utils.PreferencesManager

class MemoriesFragment : Fragment() {

    private lateinit var viewModel: MemoriesViewModel
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var memoriesRecyclerView: androidx.recyclerview.widget.RecyclerView
    private lateinit var loadingIndicator: android.widget.ProgressBar
    private lateinit var emptyStateView: View
    private lateinit var adapter: MemoriesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_memories, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferencesManager = PreferencesManager(requireContext())
        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application))[MemoriesViewModel::class.java]

        // Setup toolbar with back button
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        // Set back arrow icon
        val backIcon = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_arrow_back)
        backIcon?.setTint(ContextCompat.getColor(requireContext(), R.color.text_primary))
        toolbar.navigationIcon = backIcon
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        memoriesRecyclerView = view.findViewById(R.id.memoriesRecyclerView)
        loadingIndicator = view.findViewById(R.id.loadingIndicator)
        emptyStateView = view.findViewById(R.id.emptyStateView)

        // Setup RecyclerView with pagination
        adapter = MemoriesAdapter { tap ->
            // Optional: Show tap details in a dialog or navigate to detail view
        }
        val layoutManager = LinearLayoutManager(requireContext())
        memoriesRecyclerView.layoutManager = layoutManager
        memoriesRecyclerView.adapter = adapter

        // Add scroll listener for infinite scroll pagination
        memoriesRecyclerView.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                
                // Load more when user scrolls to within 3 items of the bottom
                if (dy > 0 && !viewModel.isLoading() && viewModel.hasMore()) {
                    if (lastVisibleItemPosition >= totalItemCount - 3) {
                        viewModel.loadMore()
                    }
                }
            }
        })

        // Observe ViewModel
        viewModel.memoriesState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is MemoriesState.Loading -> {
                    loadingIndicator.visibility = View.VISIBLE
                    emptyStateView.visibility = View.GONE
                    memoriesRecyclerView.visibility = View.GONE
                }
                is MemoriesState.LoadingMore -> {
                    // Show loading indicator at bottom (handled by adapter footer)
                    loadingIndicator.visibility = View.GONE
                }
                is MemoriesState.Success -> {
                    loadingIndicator.visibility = View.GONE
                    if (state.taps.isEmpty() && !state.isLoadingMore) {
                        emptyStateView.visibility = View.VISIBLE
                        memoriesRecyclerView.visibility = View.GONE
                    } else {
                        emptyStateView.visibility = View.GONE
                        memoriesRecyclerView.visibility = View.VISIBLE
                        adapter.submitList(state.taps)
                        adapter.setLoadingMore(state.isLoadingMore)
                        adapter.setHasMore(state.hasMore)
                    }
                }
                is MemoriesState.Error -> {
                    loadingIndicator.visibility = View.GONE
                    // Suppress errors during initial authentication
                    if (!com.tapme.app.utils.AuthenticationManager.isInInitialAuthentication()) {
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Load memories
        viewModel.loadMemories()
    }

    override fun onResume() {
        super.onResume()
        // Refresh when fragment becomes visible
        viewModel.loadMemories()
    }
}
