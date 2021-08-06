package mx.odelant.printorders.activity.utils.dialog

import android.content.Context
import androidx.appcompat.app.AlertDialog

class DialogHelpers {
    companion object {
        fun makeDeleteConfirmationAlert(
            context: Context,
            onConfirmDelete: () -> Unit
        ): AlertDialog {
            return AlertDialog.Builder(context).setMessage("Seguro?")
                .setCancelable(true)
                .setPositiveButton("Eliminar") { _, _ ->
                    onConfirmDelete()
                }
                .setNegativeButton("Cancelar", null)
                .create()
        }
    }
}