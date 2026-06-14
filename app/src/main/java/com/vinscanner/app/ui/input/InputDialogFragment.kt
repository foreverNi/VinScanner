package com.vinscanner.app.ui.input

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.vinscanner.app.R
import com.vinscanner.app.util.VinValidator

/**
 * 手动输入VIN码对话框。
 * 当用户点击确认后，通过回调接口返回规范化后的VIN码。
 */
class InputDialogFragment : DialogFragment() {

    interface OnVinInputListener {
        fun onVinInput(vin: String)
    }

    var listener: OnVinInputListener? = null
    private var duplicateCheck: ((String) -> Boolean)? = null

    fun setDuplicateCheck(check: (String) -> Boolean) {
        this.duplicateCheck = check
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_input_vin, null)
        val editText = view.findViewById<EditText>(R.id.et_vin)
        val errorView = view.findViewById<TextView>(R.id.tv_error)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.dialog_input_title)
            .setView(view)
            .setPositiveButton(android.R.string.ok, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create()

        dialog.setOnShowListener {
            val okBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val cancelBtn = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            okBtn.setOnClickListener {
                val raw = editText.text?.toString() ?: ""
                val vin = VinValidator.normalize(raw)
                when {
                    !VinValidator.isValid(vin) -> errorView.setText(R.string.dialog_invalid_vin)
                    duplicateCheck?.invoke(vin) == true -> errorView.setText(R.string.dialog_duplicate_vin)
                    else -> {
                        listener?.onVinInput(vin)
                        dialog.dismiss()
                    }
                }
            }
        }
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { errorView.text = "" }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        return dialog
    }
}
