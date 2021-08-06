package mx.odelant.printorders.activity.inventory

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mx.odelant.printorders.R
import mx.odelant.printorders.dataLayer.AppDatabase
import mx.odelant.printorders.dataLayer.ProductDL
import mx.odelant.printorders.entities.Product
import mx.odelant.printorders.utils.Formatter

class ProductDetailDialog {

    companion object {
        @SuppressLint("InflateParams")
        fun makeEditProductDialog(
            context: Context,
            db: AppDatabase,
            product: Product,
            onSuccess: () -> Unit
        ): AlertDialog {
            val builder = AlertDialog.Builder(context)
            val dialogLayout =
                LayoutInflater.from(context)
                    .inflate(R.layout.inventory__dialog__product, null, false)
            val editTextName =
                dialogLayout.findViewById<EditText>(R.id.product_dialog_edittext_product_name)
            val editTextBasePrice =
                dialogLayout.findViewById<EditText>(R.id.product_dialog_edittext_base_price)
            val editTextStock =
                dialogLayout.findViewById<EditText>(R.id.product_dialog_edittext_stock)

            builder.setTitle("Editar producto")


            editTextBasePrice.filters = arrayOf(Formatter.DecimalDigitsInputFilter(7, 2))
            editTextStock.filters = arrayOf(Formatter.DecimalDigitsInputFilter(7, 2))

            editTextName.setText(product.name)
            editTextStock.setText(Formatter.intInHundredthsToString(product.stockInHundredths))

            val basePriceFormatted = Formatter.intInHundredthsToString(product.basePriceInCents)
            editTextBasePrice.setText(basePriceFormatted)

            builder.setView(dialogLayout)
            builder.setPositiveButton("OK", null)
            builder.setNegativeButton("cancelar", null)
            builder.setCancelable(true)
            builder.setNeutralButton("Eliminar", null)

            val dialog = builder.show()

            val neutralButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)

            neutralButton.setOnClickListener {
                AlertDialog.Builder(context).setMessage("Seguro?")
                    .setCancelable(true)
                    .setPositiveButton("Eliminar") { _, _ ->
                        GlobalScope.launch {
                            ProductDL.deleteProductById(db, product)
                            onSuccess()
                            dialog.dismiss()
                        }
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }

            val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            okButton.setOnClickListener {
                val updateProduct = Product(
                    id = product.id,
                    name = editTextName.text.toString(),
                    basePriceInCents = Formatter.stringToIntInHundredths(editTextBasePrice.text.toString()),
                    stockInHundredths = Formatter.stringToIntInHundredths(editTextStock.text.toString()),
                    isDeleted = false
                )

                GlobalScope.launch {
                    val error = ProductDL.tryUpdateProduct(db, updateProduct)
                    if (error == ProductDL.ProductDLError.NONE) {
                        onSuccess()
                        dialog.dismiss()
                    } else {
                        Snackbar.make(
                            it,
                            "Un producto con ese nombre ya existe. Seleccionalo para editarlo.",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            }

            return dialog
        }

        @SuppressLint("InflateParams")
        fun makeCreateProductDialog(
            context: Context,
            db: AppDatabase,
            onSuccess: () -> Unit
        ): AlertDialog {
            val builder = AlertDialog.Builder(context)
            val dialogLayout =
                LayoutInflater.from(context)
                    .inflate(R.layout.inventory__dialog__product, null, false)
            val editTextName =
                dialogLayout.findViewById<EditText>(R.id.product_dialog_edittext_product_name)
            val editTextBasePrice =
                dialogLayout.findViewById<EditText>(R.id.product_dialog_edittext_base_price)
            val editTextStock =
                dialogLayout.findViewById<EditText>(R.id.product_dialog_edittext_stock)

            builder.setTitle("Crear producto")
            editTextBasePrice.filters = arrayOf(Formatter.DecimalDigitsInputFilter(7, 2))
            editTextStock.filters = arrayOf(Formatter.DecimalDigitsInputFilter(7, 2))
            builder.setView(dialogLayout)

            builder.setPositiveButton("OK", null)
            builder.setNegativeButton("cancelar", null)
            builder.setCancelable(true)
            val dialog = builder.show()

            val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

            okButton.setOnClickListener {
                val basePriceInCents =
                    Formatter.stringToIntInHundredths(editTextBasePrice.text.toString())
                val stockInHundredths =
                    Formatter.stringToIntInHundredths(editTextStock.text.toString())

                val product = Product(
                    0,
                    editTextName.text.toString(),
                    basePriceInCents,
                    stockInHundredths,
                    false
                )

                GlobalScope.launch {
                    val error = ProductDL.tryInsertProduct(db, product)
                    if (error == ProductDL.ProductDLError.NONE) {
                        onSuccess()
                        dialog.dismiss()
                    } else {
                        Snackbar.make(
                            it,
                            "Un producto con ese nombre ya existe. Seleccionalo para editarlo.",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            }

            return dialog
        }
    }
}