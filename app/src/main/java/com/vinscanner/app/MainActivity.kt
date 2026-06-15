package com.vinscanner.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vinscanner.app.data.VinRecord
import com.vinscanner.app.data.VinRepository
import com.vinscanner.app.databinding.ActivityMainBinding
import com.vinscanner.app.ui.input.InputDialogFragment
import com.vinscanner.app.ui.list.VinListAdapter
import com.vinscanner.app.ui.scan.ScanActivity
import com.vinscanner.app.ui.settings.SettingsActivity
import com.vinscanner.app.util.EmailSender
import com.vinscanner.app.viewmodel.VinViewModel
import com.vinscanner.app.viewmodel.VinViewModelFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: VinViewModel
    private lateinit var adapter: VinListAdapter
    private lateinit var repository: VinRepository

    private val scanLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val vin = result.data?.getStringExtra(ScanActivity.EXTRA_SCAN_RESULT)
                ?: return@registerForActivityResult
            addVin(vin, getString(R.string.source_scan))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        repository = VinRepository(this)
        viewModel = ViewModelProvider(
            this,
            VinViewModelFactory(repository)
        )[VinViewModel::class.java]

        adapter = VinListAdapter(
            onItemClick = { record, _ -> copyToClipboard(record.vin) },
            onItemLongClick = { record, pos -> confirmDelete(record, pos) }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        viewModel.records.observe(this) { list ->
            adapter.submitList(list)
            binding.emptyView.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            binding.tvCount.text = getString(R.string.item_count, list.size)
        }

        binding.fabScan.setOnClickListener {
            scanLauncher.launch(Intent(this, ScanActivity::class.java))
        }
        binding.btnInput.setOnClickListener {
            val dialog = InputDialogFragment().apply {
                listener = object : InputDialogFragment.OnVinInputListener {
                    override fun onVinInput(vin: String) {
                        addVin(vin, getString(R.string.source_input))
                    }
                }
                setDuplicateCheck { viewModel.contains(it) }
            }
            dialog.show(supportFragmentManager, "input_dialog")
        }
        binding.btnEmail.setOnClickListener { sendEmail() }
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        binding.btnClear.setOnClickListener { confirmClear() }
    }

    private fun addVin(vin: String, source: String) {
        val record = VinRecord(vin, System.currentTimeMillis(), source)
        if (viewModel.add(record)) {
            Toast.makeText(this, getString(R.string.toast_scan_success, vin), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, R.string.dialog_duplicate_vin, Toast.LENGTH_SHORT).show()
        }
    }

    private fun copyToClipboard(text: String) {
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("VIN", text))
        Toast.makeText(this, R.string.toast_copied, Toast.LENGTH_SHORT).show()
    }

    private fun confirmDelete(record: VinRecord, pos: Int) {
        AlertDialog.Builder(this)
            .setTitle(R.string.app_name)
            .setMessage(getString(R.string.dialog_delete_confirm, record.vin))
            .setPositiveButton(android.R.string.ok) { _, _ ->
                viewModel.removeAt(pos)
                Toast.makeText(this, R.string.toast_deleted, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun confirmClear() {
        val records = viewModel.records.value.orEmpty()
        if (records.isEmpty()) {
            Toast.makeText(this, R.string.dialog_email_no_data, Toast.LENGTH_SHORT).show()
            return
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.app_name)
            .setMessage(R.string.dialog_clear_confirm)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                viewModel.clearAll()
                Toast.makeText(this, R.string.toast_clear_success, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun sendEmail() {
        val records = viewModel.records.value.orEmpty()
        if (records.isEmpty()) {
            Toast.makeText(this, R.string.dialog_email_no_data, Toast.LENGTH_SHORT).show()
            return
        }
        val prefs = getSharedPreferences("vin_scanner_settings", MODE_PRIVATE)
        val email = prefs.getString(getString(R.string.settings_email_key), "") ?: ""
        val subject = prefs.getString(
            getString(R.string.settings_subject_key),
            getString(R.string.settings_default_subject)
        ) ?: getString(R.string.settings_default_subject)
        if (email.isBlank()) {
            Toast.makeText(this, R.string.dialog_email_no_address, Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, SettingsActivity::class.java))
            return
        }
        val intent = EmailSender.buildEmailIntent(this, email, subject, records)
        if (intent == null) {
            Toast.makeText(this, R.string.dialog_email_no_app, Toast.LENGTH_LONG).show()
            return
        }
        startActivity(intent)
    }
}
