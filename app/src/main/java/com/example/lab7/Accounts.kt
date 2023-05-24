package com.example.lab7


data class Accounts(val login: String, val password: String)

var accountList = listOf(
    Accounts("admin@mail.ru","1234"),
    Accounts("test1@mail.ru","111"))