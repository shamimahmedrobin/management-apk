package com.example.core.security

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SecurityManager {

    private const val PREFS_NAME = "stylesphere_security_prefs"
    private const val KEY_PIN_ENABLED = "key_pin_enabled"
    private const val KEY_PIN_CODE = "key_pin_code"

    private var prefs: SharedPreferences? = null

    private val _isPinEnabled = MutableStateFlow(false)
    val isPinEnabled: StateFlow<Boolean> = _isPinEnabled.asStateFlow()

    private val _isAppLocked = MutableStateFlow(false)
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    private var currentPin: String = "1234"

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val enabled = prefs?.getBoolean(KEY_PIN_ENABLED, false) ?: false
            val savedPin = prefs?.getString(KEY_PIN_CODE, "1234") ?: "1234"
            currentPin = savedPin
            _isPinEnabled.value = enabled
            if (enabled) {
                _isAppLocked.value = true
            }
        }
    }

    fun setPin(pin: String, context: Context? = null) {
        currentPin = pin
        _isPinEnabled.value = true
        _isAppLocked.value = false
        val p = prefs ?: context?.applicationContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        p?.edit {
            putBoolean(KEY_PIN_ENABLED, true)
            putString(KEY_PIN_CODE, pin)
        }
    }

    fun disablePin(context: Context? = null) {
        _isPinEnabled.value = false
        _isAppLocked.value = false
        val p = prefs ?: context?.applicationContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        p?.edit {
            putBoolean(KEY_PIN_ENABLED, false)
        }
    }

    fun verifyPin(pin: String): Boolean {
        val isValid = pin == currentPin
        if (isValid) {
            _isAppLocked.value = false
        }
        return isValid
    }

    fun lockApp() {
        if (_isPinEnabled.value) {
            _isAppLocked.value = true
        }
    }

    fun unlockWithBiometric() {
        _isAppLocked.value = false
    }
}
