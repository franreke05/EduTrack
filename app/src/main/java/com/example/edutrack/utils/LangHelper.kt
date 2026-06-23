package com.example.edutrack.utils

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

private const val PREFS = "lang_prefs"
private const val KEY_LANG = "lang"

fun Context.getSavedLang(): String =
    getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_LANG, "es") ?: "es"

fun Context.saveLang(lang: String) =
    getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_LANG, lang).apply()

fun Context.wrapWithLocale(): Context {
    val lang = getSavedLang()
    val locale = Locale(lang)
    Locale.setDefault(locale)
    val config = Configuration(resources.configuration)
    config.setLocale(locale)
    return createConfigurationContext(config)
}
