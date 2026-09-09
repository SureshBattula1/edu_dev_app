package com.example.myeduapp.core.datastore

interface SecureStorage {
    fun saveString(key: String, value: String)
    fun getString(key: String): String?
    fun remove(key: String)
    fun clear()
}

expect fun getSecureStorage(): SecureStorage
