package com.example.myeduapp.core.datastore

import platform.Foundation.NSUserDefaults

class IosSecureStorage : SecureStorage {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun saveString(key: String, value: String) {
        defaults.setObject(value, forKey = key)
    }

    override fun getString(key: String): String? {
        return defaults.stringForKey(key)
    }

    override fun remove(key: String) {
        defaults.removeObjectForKey(key)
    }

    override fun clear() {
        val dictionary = defaults.dictionaryRepresentation()
        for (key in dictionary.keys) {
            val keyStr = key as? String ?: continue
            defaults.removeObjectForKey(keyStr)
        }
    }
}

actual fun getSecureStorage(): SecureStorage = IosSecureStorage()
