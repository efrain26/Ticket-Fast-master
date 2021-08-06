package mx.odelant.printorders.activity.start

import android.app.Activity
import android.widget.Button
import android.widget.EditText
import mx.odelant.printorders.R

class StartView(activity: Activity) {
    val emailEditText: EditText = activity.findViewById(R.id.start_et_email)
    val codeEditText: EditText = activity.findViewById(R.id.start_et_registration_code)
    val registrationButton: Button = activity.findViewById(R.id.start_btn_login)
    val whatsappButton: Button = activity.findViewById(R.id.send_btn_whatsapp)
}