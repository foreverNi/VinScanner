package com.vinscanner.app.ui.edit

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.vinscanner.app.R
import com.vinscanner.app.data.VinRepository
import com.vinscanner.app.databinding.ActivityVinEditBinding
import com.vinscanner.app.util.VinValidator
import com.vinscanner.app.viewmodel.VinViewModel
import com.vinscanner.app.viewmodel.VinViewModelFactory

class VinEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVinEditBinding
    private lateinit var viewModel: VinViewModel
    private var vinIndex = -1
    private var originalVin = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVinEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.edit_title)

        vinIndex = intent.getIntExtra(EXTRA_VIN_INDEX, -1)
        originalVin = intent.getStringExtra(EXTRA_VIN_VALUE).orEmpty()
        if (vinIndex < 0 || originalVin.isBlank()) {
            Toast.makeText(this, R.string.edit_invalid_record, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        viewModel = ViewModelProvider(
            this,
            VinViewModelFactory(VinRepository(this))
        )[VinViewModel::class.java]

        binding.tvCurrentVin.text = originalVin
        binding.etVin.setText(originalVin)
        binding.etVin.setSelection(binding.etVin.text?.length ?: 0)
        binding.etVin.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.tvError.text = ""
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        binding.btnSave.setOnClickListener { saveVin() }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun saveVin() {
        val vin = VinValidator.normalize(binding.etVin.text?.toString().orEmpty())
        when {
            !VinValidator.isValid(vin) -> binding.tvError.setText(R.string.dialog_invalid_vin)
            vin == originalVin -> finish()
            viewModel.containsExceptIndex(vin, vinIndex) -> binding.tvError.setText(R.string.dialog_duplicate_vin)
            viewModel.updateVinAt(vinIndex, vin) -> {
                Toast.makeText(this, R.string.toast_edit_success, Toast.LENGTH_SHORT).show()
                finish()
            }
            else -> binding.tvError.setText(R.string.edit_save_failed)
        }
    }

    companion object {
        const val EXTRA_VIN_INDEX = "extra_vin_index"
        const val EXTRA_VIN_VALUE = "extra_vin_value"
    }
}
