package mx.odelant.printorders.activity.analytics

import android.app.DatePickerDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mx.odelant.printorders.R
import mx.odelant.printorders.dataLayer.AnalyticsDL
import mx.odelant.printorders.dataLayer.AppDatabase
import mx.odelant.printorders.dataLayer.ClientDL
import mx.odelant.printorders.dataLayer.ProductDL
import mx.odelant.printorders.entities.Client
import mx.odelant.printorders.entities.Product
import mx.odelant.printorders.utils.Formatter
import java.util.*

class AnalyticsDialog {
    companion object {
        fun makeSelectFiltersDialog(
            date: Calendar,
            dateScope: AnalyticsDL.DateScope,
            dataType: AnalyticsDL.DataType,
            client: Client?,
            product: Product?,
            context: Context,
            db: AppDatabase,
            onSelect: (Calendar, AnalyticsDL.DateScope, AnalyticsDL.DataType, Client?, Product?) -> Unit
        ): AlertDialog {
            val builder = AlertDialog.Builder(context)
            val dialogLayout =
                LayoutInflater.from(context)
                    .inflate(R.layout.analytics__dialog__select_filters, null, false)

            val selectedDate: Calendar = date
            var selectedDateScope: AnalyticsDL.DateScope = dateScope
            var selectedDataType: AnalyticsDL.DataType = dataType
            var selectedClient: Client? = client
            var selectedProduct: Product? = product

            val selectDateButton =
                dialogLayout.findViewById<Button>(R.id.analytics_dialog_btn_select_date)
            val dateTextView =
                dialogLayout.findViewById<TextView>(R.id.analytics_dialog_tv_date_label)

            val granularityRadioGroup =
                dialogLayout.findViewById<RadioGroup>(R.id.analytics_dialog_rg_granularity)
            val granularityWeekRadioButton =
                dialogLayout.findViewById<RadioButton>(R.id.analytics_dialog_rb_granularity_week)
            val granularityMonthRadioButton =
                dialogLayout.findViewById<RadioButton>(R.id.analytics_dialog_rb_granularity_month)
            val granularityYearRadioButton =
                dialogLayout.findViewById<RadioButton>(R.id.analytics_dialog_rb_granularity_year)

            val chartTypeRadioGroup =
                dialogLayout.findViewById<RadioGroup>(R.id.analytics_dialog_rg_chart)
            val chartRevenueRadioButton =
                dialogLayout.findViewById<RadioButton>(R.id.analytics_dialog_rb_chart_revenue)
            val chartQuantityRadioButton =
                dialogLayout.findViewById<RadioButton>(R.id.analytics_dialog_rb_chart_quantity)

            val selectClientACT =
                dialogLayout.findViewById<AutoCompleteTextView>(R.id.analytics_dialog_act_select_client)
            val clearClientButton =
                dialogLayout.findViewById<Button>(R.id.analytics_dialog_btn_clear_client)

            val selectProductACT =
                dialogLayout.findViewById<AutoCompleteTextView>(R.id.analytics_dialog_act_select_product)
            val clearProductButton =
                dialogLayout.findViewById<Button>(R.id.analytics_dialog_btn_clear_product)

            when (dateScope) {
                AnalyticsDL.DateScope.Week ->
                    granularityWeekRadioButton.isChecked = true
                AnalyticsDL.DateScope.Month ->
                    granularityMonthRadioButton.isChecked = true
                AnalyticsDL.DateScope.Year ->
                    granularityYearRadioButton.isChecked = true
            }

            when (dataType) {
                AnalyticsDL.DataType.Revenue ->
                    chartRevenueRadioButton.isChecked = true
                AnalyticsDL.DataType.Quantity ->
                    chartQuantityRadioButton.isChecked = true
            }

            if (client != null) {
                selectClientACT.setText(client.name)
            }

            if (product != null) {
                selectProductACT.setText(product.name)
            }

            builder.setTitle("Selecciona filtros")
            builder.setView(dialogLayout)

            builder.setPositiveButton("OK", null)
            builder.setNegativeButton("cancelar", null)

            builder.setCancelable(true)
            val dialog = builder.show()

            dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

            val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val cancelButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)

            cancelButton.setOnClickListener {
                dialog.dismiss()
            }

            okButton.setOnClickListener {
                onSelect(
                    selectedDate,
                    selectedDateScope,
                    selectedDataType,
                    selectedClient,
                    selectedProduct
                )
                dialog.dismiss()
            }

            dateTextView.text = Formatter.toScopedDate(selectedDateScope, selectedDate)

            val datePickerListener =
                DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                    selectedDate.set(Calendar.YEAR, year)
                    selectedDate.set(Calendar.MONTH, monthOfYear)
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    dateTextView.text = Formatter.toScopedDate(selectedDateScope, selectedDate)
                }

            selectDateButton.setOnClickListener {
                val dpd = DatePickerDialog(
                    context,
                    datePickerListener,
                    selectedDate.get(Calendar.YEAR),
                    selectedDate.get(Calendar.MONTH),
                    selectedDate.get(Calendar.DAY_OF_MONTH)
                )
                dpd.show()
            }

            val clientAdapter =
                ArrayAdapter<Client>(context, android.R.layout.simple_dropdown_item_1line)
            selectClientACT.setAdapter(clientAdapter)
            selectClientACT.threshold = 1

            GlobalScope.launch {
                withContext(Dispatchers.IO) {
                    val clients = ClientDL.getAll(db)
                    clientAdapter.clear()
                    clientAdapter.addAll(clients)
                }
            }

            selectClientACT.setOnItemClickListener { _, view, pos, _ ->
                selectedClient = clientAdapter.getItem(pos)
                val imm =
                    context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.applicationWindowToken, 0)
            }

            clearClientButton.setOnClickListener {
                selectedClient = null
                selectClientACT.setText("")
                selectClientACT.clearListSelection()
            }

            val productAdapter =
                ArrayAdapter<Product>(context, android.R.layout.simple_dropdown_item_1line)
            selectProductACT.setAdapter(productAdapter)
            selectProductACT.threshold = 1

            GlobalScope.launch {
                withContext(Dispatchers.IO) {
                    val products = ProductDL.getAll(db)
                    productAdapter.clear()
                    productAdapter.addAll(products)
                }
            }

            selectProductACT.setOnItemClickListener { _, view, pos, _ ->
                selectedProduct = productAdapter.getItem(pos)
                val imm =
                    context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.applicationWindowToken, 0)
            }

            clearProductButton.setOnClickListener {
                selectedProduct = null
                selectProductACT.setText("")
                selectProductACT.clearListSelection()
            }

            granularityRadioGroup.setOnCheckedChangeListener { radioGroup, checkedId ->
                val changedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
                if (changedRadioButton.isChecked) {
                    when (checkedId) {
                        R.id.analytics_dialog_rb_granularity_week ->
                            selectedDateScope = AnalyticsDL.DateScope.Week
                        R.id.analytics_dialog_rb_granularity_month ->
                            selectedDateScope = AnalyticsDL.DateScope.Month
                        R.id.analytics_dialog_rb_granularity_year ->
                            selectedDateScope = AnalyticsDL.DateScope.Year
                    }
                }
                dateTextView.text = Formatter.toScopedDate(selectedDateScope, selectedDate)
            }

            chartTypeRadioGroup.setOnCheckedChangeListener { radioGroup, checkedId ->
                val changedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
                if (changedRadioButton.isChecked) {
                    when (checkedId) {
                        R.id.analytics_dialog_rb_chart_quantity ->
                            selectedDataType = AnalyticsDL.DataType.Quantity
                        R.id.analytics_dialog_rb_chart_revenue ->
                            selectedDataType = AnalyticsDL.DataType.Revenue
                    }
                }
            }

            return dialog
        }
    }
}