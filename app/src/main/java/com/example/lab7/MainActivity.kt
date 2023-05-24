package com.example.lab7

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.concurrent.Executor
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity(), PingCallback {

    private lateinit var emailText: AutoCompleteTextView
    private lateinit var passwordText: EditText
    private lateinit var loginButton: Button
    private lateinit var error_text1: TextView
    private lateinit var error_text2: TextView

    private var emailSuggestions = arrayOf("gmail.com", "gmail.uk.com", "yahoo.com", "hotmail.com", "mail.ru","bk.ru","yandex.ru")
    override fun onCreate(savedInstanceState: Bundle?)  {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)




        emailText = findViewById(R.id.editText_email)
        passwordText = findViewById(R.id.editText_password)
        loginButton = findViewById(R.id.button_login)
        error_text1 = findViewById(R.id.error_text1)
        error_text2 = findViewById(R.id.error_text2)

        //создаем подсказку
        var adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, emailSuggestions)
        emailText.setAdapter(adapter)

        emailText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val input = s.toString()
                //подбор подсказки по введеному тексту
                if (input.contains("@")) {
                    val index = input.indexOf("@")
                    val domain = input.substring(index + 1)
                    val temparray = emailSuggestions.filter { it.startsWith(domain) }
                    val combinedEmails = mutableListOf<String>()
                    for (suggestion in temparray) {
                        val email = input.replaceAfter("@", "") + suggestion
                        combinedEmails.add(email)

                    }
                    val resultArray = combinedEmails.toTypedArray()
                    adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_dropdown_item_1line, resultArray)
                    emailText.setAdapter(adapter)
                }

            }

            override fun afterTextChanged(s: Editable?) {

            }

        })


        loginButton.setOnClickListener{
            val email = emailText.text.toString()
            val password = passwordText.text.toString()
            var textFail = false



            //проверка на пустое поле password
            if (TextUtils.isEmpty(password)) {
                error_text2.setText(R.string.empty)
                textFail = true
            }
            else{
                //ограничение пароля по вводим символам
                val minLengthChar = 3
                val maxLengthChar = 8
                error_text2.setText("")
                if(password.length<minLengthChar){
                    error_text2.setText(R.string.low_password)
                    textFail = true
                }
                if(password.length>maxLengthChar){
                    error_text2.setText(R.string.high_password)
                    textFail = true
                }
            }


            //проверка на пустое поле email
            if (TextUtils.isEmpty(email)) {
                error_text1.setText(R.string.empty)
                textFail = true
            }else {
                error_text1.setText("")
                //проверка на доступность домена
                val domain = email.substring(email.indexOf("@") + 1)
                if(textFail == false){
                    startPingAndContinueWork(domain)
                }
            }

        }


    }
    override fun onPingResult(success: Boolean) {
        runOnUiThread { if (success) {
            error_text1.setText("")
            val email = emailText.text.toString()
            val password = passwordText.text.toString()
            val foundAccount = accountList.find{ it.login == email}
            if(foundAccount == null) Toast.makeText(this,R.string.fail_login,Toast.LENGTH_SHORT).show()
            else {
                if(foundAccount.password != password) Toast.makeText(this,R.string.fail_login,Toast.LENGTH_SHORT).show()
                else Toast.makeText(this,R.string.success_login,Toast.LENGTH_SHORT).show()
            }
        } else {
            error_text1.setText("Domain isn't correct")
        }}
    }
    private val executor: Executor = Executors.newSingleThreadExecutor()
    fun startPingAndContinueWork(domain: String) {
        executor.execute(PingTask(domain, this))
    }
}

interface PingCallback {
    fun onPingResult(success: Boolean)
}

class PingTask(private val domain: String, private val callback: PingCallback) : Runnable {
    override fun run() {
        try {
            val address = InetAddress.getByName(domain)
            val success = address.isReachable(3000)
            callback.onPingResult(success)
        } catch (e: UnknownHostException) {
            callback.onPingResult(false)
        }
    }
}

