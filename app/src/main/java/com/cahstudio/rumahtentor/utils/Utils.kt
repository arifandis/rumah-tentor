package com.cahstudio.rumahtentor.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import com.cahstudio.rumahtentor.R
import java.util.*

class Utils {

    companion object{
        const val KEY_CLOUD_MESSAGE = "AAAAzhiI7w8:APA91bGAPuawvc7ZkodaenOk63-5QYF5JBsLMey72h8-3J_Zlqu1MBA_7cas32tFMms9WJP3Ak2TeHIqZ7wJ1-Fqv89jZl5ophR-9l3mBpsqFYyVmj1eq_B5HXotbQzRQ2XuVw9oqJ19"

        fun setupProgressDialog(context: Context): Dialog? {
            val dialog = Dialog(context)
            dialog.setContentView(R.layout.dialog_loading)
            dialog.setCancelable(false)

            Objects.requireNonNull(dialog.window)!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            return dialog
        }
    }
}