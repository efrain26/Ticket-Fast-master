package mx.odelant.printorders.activity.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import mx.odelant.printorders.R

class MainDialog {

    companion object {
        @SuppressLint("InflateParams")
        fun makeEditUsernameDialog(
            context: Context,
            sharedPreferences: SharedPreferences,
            onSuccess: () -> Unit
        ): AlertDialog {
            val builder = AlertDialog.Builder(context)
            val dialogLayout =
                LayoutInflater.from(context).inflate(R.layout.main__dialog__username, null, false)
            val editTextName = dialogLayout.findViewById<EditText>(R.id.main_dialog_edittext_username)

            builder.setTitle("Editar empresa")

            val username = sharedPreferences.getString(context.getString(R.string.username), "")
            editTextName.setText(username)

            builder.setView(dialogLayout)
            builder.setPositiveButton("OK", null)
            builder.setNegativeButton("cancelar", null)
            builder.setCancelable(true)

            val dialog = builder.show()

            val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            okButton.setOnClickListener {

                val usernameFromEditText = editTextName.text.toString()

                val sharedPreferencesEditor = sharedPreferences.edit()

                if(!usernameFromEditText.isBlank()) {
                    sharedPreferencesEditor.putString(
                        context.getString(R.string.username),
                        usernameFromEditText
                    )
                    sharedPreferencesEditor.apply()
                } else {
                    sharedPreferencesEditor.remove(context.getString(R.string.username))
                    sharedPreferencesEditor.apply()
                }
                onSuccess()
                dialog.dismiss()
            }

            return dialog
        }

    }
}