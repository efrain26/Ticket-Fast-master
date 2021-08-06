package mx.odelant.printorders.activity.orderHistory

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mx.odelant.printorders.R
import mx.odelant.printorders.dataLayer.AppDatabase
import mx.odelant.printorders.dataLayer.ClientDL
import mx.odelant.printorders.entities.Client

class OrderHistoryDialog {
    companion object {
        fun makeSelectClientDialog(
            context: Context,
            db: AppDatabase,
            onSelect: (Client?) -> Unit
        ): AlertDialog {
            val builder = AlertDialog.Builder(context)
            val dialogLayout =
                LayoutInflater.from(context)
                    .inflate(R.layout.order_history__dialog__select_client, null, false)
            val selectClientACT =
                dialogLayout.findViewById<AutoCompleteTextView>(R.id.order_history_act_select_client)

            builder.setTitle("Selecciona cliente")
            builder.setView(dialogLayout)

            builder.setPositiveButton("OK", null)
            builder.setNegativeButton("sin cliente", null)
            builder.setNeutralButton("cancelar", null)

            builder.setCancelable(true)
            val dialog = builder.show()

            val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val cancelButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
            val clearClientButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)

            cancelButton.setOnClickListener {
                dialog.dismiss()
            }

            clearClientButton.setOnClickListener {
                onSelect(null)
                dialog.dismiss()
            }

            val adapter = ArrayAdapter<Client>(context, android.R.layout.simple_dropdown_item_1line)
            selectClientACT.setAdapter(adapter)
            selectClientACT.threshold = 1

            GlobalScope.launch {
                withContext(Dispatchers.IO) {
                    val clients = ClientDL.getAll(db)
                    adapter.clear()
                    adapter.addAll(clients)
                }
            }

            selectClientACT.setOnItemClickListener { _, _, pos, _ ->
                val client = adapter.getItem(pos)
                if (client != null) {
                    okButton.isEnabled = true
                    okButton.setOnClickListener {
                        onSelect(client)
                        dialog.dismiss()
                    }
                }
            }

            return dialog
        }
    }
}