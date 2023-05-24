<p align = "center">МИНИСТЕРСТВО НАУКИ И ВЫСШЕГО ОБРАЗОВАНИЯ
РОССИЙСКОЙ ФЕДЕРАЦИИ
ФЕДЕРАЛЬНОЕ ГОСУДАРСТВЕННОЕ БЮДЖЕТНОЕ
ОБРАЗОВАТЕЛЬНОЕ УЧРЕЖДЕНИЕ ВЫСШЕГО ОБРАЗОВАНИЯ
«САХАЛИНСКИЙ ГОСУДАРСТВЕННЫЙ УНИВЕРСИТЕТ»</p>
<br>
<p align = "center">Институт естественных наук и техносферной безопасности</p>
<p align = "center">Кафедра информатики</p>
<p align = "center">Пак Никита Витальевич</p>
<br>
<p align = "center">Лабораторная работа</p>
<p align = "center">«Проверка адреса email»</p>
<p align = "center">01.03.02 Прикладная математика и информатика</p>




<br>
<p align = "right" >Научный руководитель</p>
<p align = "right" >Соболев Евгений Игоревич</p>
<p align = "center" >Южно-Сахалинск</p>
<p align = "center" >2023 г.</p>
<p align = "center" ><b>ВВЕДЕНИЕ</b></p>
<p>Kotlin (Ко́тлин) — статически типизированный, объектно-ориентированный язык программирования, работающий поверх Java Virtual Machine и разрабатываемый компанией JetBrains. Также компилируется в JavaScript и в исполняемый код ряда платформ через инфраструктуру LLVM. Язык назван в честь острова Котлин в Финском заливе, на котором расположен город Кронштадт</p>
<p>Авторы ставили целью создать язык более лаконичный и типобезопасный, чем Java, и более простой, чем Scala. Следствием упрощения по сравнению со Scala стали также более быстрая компиляция и лучшая поддержка языка в IDE. Язык полностью совместим с Java, что позволяет Java-разработчикам постепенно перейти к его использованию; в частности, язык также встраивается Android, что позволяет для существующего Android-приложения внедрять новые функции на Kotlin без переписывания приложения целиком.</p>
<p align = "center" >РЕШЕНИЕ ЗАДАЧ</p>

<p align = "center" >Упражнение. Создать хороший UX для пользователей, вводящих адрес электронной почты и пароль при регистрации в приложении.

Требования:

Проверка формата электронной почты. Пример: user@gmail не является действительным адресом электронной почты
Пользовательский интерфейс должен показывать, действителен или нет адрес электронной почты. При необходимости интерфейс должен указать, что не так с адресом
Автозаполнение и проверка доступности домена. Пользователи часто опечатываются при вводе адреса. Например, указывают неправильно доменное имя (gmail.con вместо gmail.com)
Проверка пароля. Нет ограничения на вводимые символы. Есть ограничение минимальной и максимальной длины
При необходимости, интерфейс должен указать, что неправильно
Проверить, что заполнены все поля, и указать, какое именно не заполнено
Для автозаполнения необходимо:

Проверить существование введённого домена
Указать, что неправильно в введённом имени
Предложить Автозаполнение доменного имени самыми вероятными и популярными доменными именами. Пример: если пользователь вводит «user@», то продолжениями могут быть «user@gmail.com», «user@yahoo.com» и т.д. Если пользователь уточняет «user@g», то продолжениями могут быть популярные домены, начинающиеся с «g». Например: «user@gmail.com», «user@gmail.co.uk»</p>

<p align = "center" >MainActivity</p>

```kotlin
    
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

```

<p align = "Accounts"</p>

```kotlin

package com.example.lab7


data class Accounts(val login: String, val password: String)

var accountList = listOf(
    Accounts("admin@mail.ru","1234"),
    Accounts("test1@mail.ru","111"))

```

***

![Screenshot](https://github.com/Pupkapus/Android_StudioEmail/blob/main/Screenshot_1.png)

***
<p align = "center" >ВЫВОД</p>
<p>Подводя итог всему сказанному, могу сделать вывод, что, поработав c kotlin, я узнал многое и применил это на практике. Все задачи были выполнены.</p>
