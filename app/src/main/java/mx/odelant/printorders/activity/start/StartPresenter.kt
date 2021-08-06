package mx.odelant.printorders.activity.start

import android.app.Activity
import android.content.Context
import android.util.Base64
import mx.odelant.printorders.R
import java.math.BigInteger
import java.security.MessageDigest
import java.util.*

class StartPresenter(private val activity: Activity, private val startView: StartView) {

    fun checkRegistration(): Boolean {
        val sharedPref = activity.getPreferences(Context.MODE_PRIVATE)

        if (
            !sharedPref.contains(activity.getString(R.string.registration_expiration_month)) ||
            !sharedPref.contains(activity.getString(R.string.registration_expiration_year)) ||
            !sharedPref.contains(activity.getString(R.string.registration_expiration_day))
        ) {
            return false
        }

        val expirationDay =
            sharedPref.getInt(activity.getString(R.string.registration_expiration_day), -1)
        val expirationMonth =
            sharedPref.getInt(activity.getString(R.string.registration_expiration_month), -1)
        val expirationYear =
            sharedPref.getInt(activity.getString(R.string.registration_expiration_year), -1)

        if (expirationDay < 0 || expirationMonth < 0 || expirationYear < 0) {
            return false
        }

        val today = Calendar.getInstance()
        if (
            expirationYear < today.get(Calendar.YEAR) ||
            (expirationYear == today.get(Calendar.YEAR) && expirationMonth < today.get(Calendar.MONTH)) ||
            (expirationYear == today.get(Calendar.YEAR) && expirationMonth == today.get(Calendar.MONTH) && expirationDay < today.get(
                Calendar.DAY_OF_MONTH
            ))
        ) {
            with(sharedPref.edit()) {
                remove(activity.getString(R.string.registration_expiration_year))
                remove(activity.getString(R.string.registration_expiration_month))
                remove(activity.getString(R.string.registration_expiration_day))
                apply()
            }
            return false
        }

        return true
    }

    fun attemptRegistration(): Boolean {
        val stringFromCodeEditText = startView.codeEditText.text.toString()
        val stringFromEmailEditText = startView.emailEditText.text.toString()

        val decodedCode =
            tryDecodeCode(stringFromEmailEditText, stringFromCodeEditText) ?: return false

        val today = Calendar.getInstance()
        val calendar =  today.get(Calendar.MONTH)
        val decodeCalendar = decodedCode.dateStart.get(Calendar.MONTH)
        if (
            decodedCode.dateStart.get(Calendar.YEAR) != today.get(Calendar.YEAR) ||
            decodedCode.dateStart.get(Calendar.MONTH) != today.get(Calendar.MONTH) ||
            decodedCode.dateStart.get(Calendar.DAY_OF_MONTH) != today.get(Calendar.DAY_OF_MONTH)
        ) {
            return false
        }

        if (
            decodedCode.dateEnd.get(Calendar.YEAR) < today.get(Calendar.YEAR) ||
            (decodedCode.dateEnd.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    decodedCode.dateEnd.get(Calendar.MONTH) < today.get(Calendar.MONTH)) ||
            (decodedCode.dateEnd.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    decodedCode.dateEnd.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                    decodedCode.dateEnd.get(Calendar.DAY_OF_MONTH) < today.get(Calendar.DAY_OF_MONTH))
        ) {
            return false
        }

        val sharedPref = activity.getPreferences(Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putInt(
                activity.getString(R.string.registration_expiration_year),
                decodedCode.dateEnd.get(Calendar.YEAR)
            )
            putInt(
                activity.getString(R.string.registration_expiration_month),
                decodedCode.dateEnd.get(Calendar.MONTH)
            )
            putInt(
                activity.getString(R.string.registration_expiration_day),
                decodedCode.dateEnd.get(Calendar.DAY_OF_MONTH)
            )
            apply()
        }

        return true
    }

    private fun tryDecodeCode(email: String, code: String): RegistrationData? {

        val salt = "8TSK08nQWtJO5tRvHHwQ"

        val deBasedCode = String(Base64.decode(code, Base64.DEFAULT))

        val payload = deBasedCode.substringBefore('|')
        val md5Hash = deBasedCode.substringAfterLast('|')

        if (payload.length + md5Hash.length + 1 != deBasedCode.length) {
            return null
        }

        val datesString = payload.substring(0, 16)

        fun String.md5(): String {
            val md = MessageDigest.getInstance("MD5")
            return BigInteger(1, md.digest(toByteArray())).toString(16).padStart(32, '0')
        }

        val calculatedMd5 = "$datesString$email$salt".md5()

        if (md5Hash != calculatedMd5) {
            return null
        }

        val dateStartDay = datesString.substring(0, 2).toInt()
        val dateStartMonth = datesString.substring(2, 4).toInt()
        val dateStartYear = datesString.substring(4, 8).toInt()

        val dateEndDay = datesString.substring(8, 10).toInt()
        val dateEndMonth = datesString.substring(10, 12).toInt()
        val dateEndYear = datesString.substring(12, 16).toInt()

        val calStart = Calendar.getInstance()
        calStart.set(dateStartYear, dateStartMonth - 1, dateStartDay)

        val calEnd = Calendar.getInstance()
        calEnd.set(dateEndYear, dateEndMonth - 1, dateEndDay)

        return RegistrationData(calStart, calEnd)
    }
}
