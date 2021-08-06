package mx.odelant.printorders.utils

import android.text.InputFilter
import android.text.Spanned
import mx.odelant.printorders.dataLayer.AnalyticsDL
import java.util.*
import java.util.regex.Pattern

class Formatter {
    companion object {
        fun intInHundredthsToString(priceInCents: Int): String {
            val basePricePadded = priceInCents.toString().padStart(3, '0')
            val basePriceWhole = basePricePadded.substring(IntRange(0, basePricePadded.length - 3))
            val basePriceDecimal =
                basePricePadded.substring(
                    IntRange(
                        basePricePadded.length - 2,
                        basePricePadded.length - 1
                    )
                )
            return "$basePriceWhole.$basePriceDecimal"
        }

        fun stringToIntInHundredths(formattedString: String): Int {
            val basePriceStringWhole =
                formattedString.split('.').getOrNull(0)?.padStart(1, '0') ?: "0"
            val basePriceStringCents = formattedString.split('.').getOrNull(1) ?: "0"
            return (basePriceStringWhole.toInt() * 100) + basePriceStringCents.padEnd(
                2,
                '0'
            ).toInt()
        }

        fun toScopedDate(dateScope: AnalyticsDL.DateScope, date: Calendar): String {
            val months = arrayOf(
                "Enero",
                "Febrero",
                "Marzo",
                "Abril",
                "Mayo",
                "Junio",
                "Julio",
                "Agosto",
                "Septiembre",
                "Octubre",
                "Noviembre",
                "Diciembre"
            )
            return when (dateScope) {
                AnalyticsDL.DateScope.Week ->
                    "Semana #${date.get(Calendar.WEEK_OF_MONTH)} de ${months[date.get(Calendar.MONTH)]} ${date.get(
                        Calendar.YEAR
                    )}"
                AnalyticsDL.DateScope.Month ->
                    "${months[date.get(Calendar.MONTH)]} ${date.get(Calendar.YEAR)}"
                AnalyticsDL.DateScope.Year ->
                    "${date.get(Calendar.YEAR)}"
            }
        }
    }

    class DecimalDigitsInputFilter(
        private val digitsBeforeZero: Int,
        private val digitsAfterZero: Int
    ) :
        InputFilter {
        private var mPattern: Pattern =
            Pattern.compile("[0-9]{0,$digitsBeforeZero}(\\.[0-9]{0,$digitsAfterZero})?")

        override fun filter(
            source: CharSequence,
            start: Int,
            end: Int,
            dest: Spanned,
            dstart: Int,
            dend: Int
        ): CharSequence? {
            val endString = dest.replaceRange(dstart, dend, source)
            return if (!mPattern.matcher(endString).matches()) "" else null
        }
    }
}

