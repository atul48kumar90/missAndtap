package com.tapme.app.ui.whitelist

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.tapme.app.R
import com.tapme.app.data.remote.RetrofitClient
import com.tapme.app.ui.whitelist.WhitelistViewModel
import com.tapme.app.utils.PreferencesManager

class WhitelistFragment : Fragment() {

    private lateinit var viewModel: WhitelistViewModel
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var adapter: AllowedTappersAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_whitelist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferencesManager = PreferencesManager(requireContext())
        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application))[WhitelistViewModel::class.java]

        setupUI(view)
        observeViewModel()
        loadData()
    }

    private fun setupUI(view: View) {
        val myUserCodeText = view.findViewById<android.widget.TextView>(R.id.myUserCodeText)
        val btnCopyCode = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCopyCode)
        val btnShareCode = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnShareCode)
        val btnRegenerateCode = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnRegenerateCode)
        val userCodeInput = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.userCodeInput)
        val btnAddTapper = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnAddTapper)
        val allowedTappersList = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.allowedTappersList)

        // Setup RecyclerView
        adapter = AllowedTappersAdapter { tapperId ->
            viewModel.removeTapper(tapperId)
        }
        allowedTappersList.layoutManager = LinearLayoutManager(requireContext())
        allowedTappersList.adapter = adapter

        // Copy code
        btnCopyCode.setOnClickListener {
            val code = myUserCodeText.text.toString()
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("User Code", code)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(requireContext(), "Code copied!", Toast.LENGTH_SHORT).show()
        }

        // Share code
        btnShareCode.setOnClickListener {
            val code = myUserCodeText.text.toString()
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "Add me on TapMe! My user code is: $code")
            }
            startActivity(Intent.createChooser(shareIntent, "Share User Code"))
        }

        // Regenerate code
        btnRegenerateCode.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Regenerate User Code?")
                .setMessage("This will create a new code. People who have your old code won't be able to tap you anymore. You'll need to share your new code.")
                .setPositiveButton("Regenerate") { _, _ ->
                    viewModel.regenerateUserCode()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // Add tapper
        btnAddTapper.setOnClickListener {
            val userCode = userCodeInput?.text?.toString()?.trim()?.uppercase() ?: ""
            if (userCode.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a user code", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!userCode.startsWith("@")) {
                Toast.makeText(requireContext(), "User code must start with @", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.addTapper(userCode)
            userCodeInput?.text?.clear()
        }
    }

    private fun observeViewModel() {
        viewModel.userCode.observe(viewLifecycleOwner) { code ->
            code?.let {
                view?.findViewById<android.widget.TextView>(R.id.myUserCodeText)?.text = it
            }
        }

        viewModel.allowedTappers.observe(viewLifecycleOwner) { tappers ->
            adapter.submitList(tappers)
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.success.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadData() {
        viewModel.loadUserCode()
        viewModel.loadAllowedTappers()
    }
}
