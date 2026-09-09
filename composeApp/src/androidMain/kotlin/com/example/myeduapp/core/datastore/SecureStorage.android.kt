package com.example.myeduapp.core.datastore

import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.myeduapp.MyEduApp

class AndroidSecureStorage : SecureStorage {
    private val masterKey = MasterKey.Builder(MyEduApp.appContext)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        MyEduApp.appContext,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override fun saveString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    override fun getString(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    override fun remove(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }

    override fun clear() {
        sharedPreferences.edit().clear().apply()
    }
}

actual fun getSecureStorage(): SecureStorage = AndroidSecureStorage()
