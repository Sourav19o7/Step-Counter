package com.level.stepcounter

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatDialogFragment

class StepDialog : AppCompatDialogFragment() {

    lateinit var editText: EditText
    lateinit var listener : StepDialogListener
    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            listener = context as StepDialogListener
        }catch (e: ClassCastException){
            throw ClassCastException(context.toString() +
            "must implement StepDialogListener")
        }
    }

    @Override
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val inflater : LayoutInflater = requireActivity().layoutInflater

        val view : View = inflater.inflate(R.layout.target_dialog, null)

        builder.setView(view)
            .setTitle("TARGET")
            .setNegativeButton("cancel") { dialogInterface, i ->

            }
            .setPositiveButton("Set") { dialogInterface, i ->
                val target = editText.text.toString()
                listener.applyTarget("/$target")
            }

        editText = view.findViewById(R.id.new_steps)
        return builder.create()
    }

    interface StepDialogListener{
        fun applyTarget(target : String)
    }
}