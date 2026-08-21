package com.phoenix.fitpro.presentation.util

import android.app.Activity
import android.app.LocaleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object LanguageHelper {

    fun setAppLanguage(context: Context, languageCode: String, restartActivity: Boolean = true) {
        val targetLang = if (languageCode.equals("en", ignoreCase = true)) "en" else "fr"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val localeManager = context.getSystemService(LocaleManager::class.java)
            localeManager?.applicationLocales = LocaleList.forLanguageTags(targetLang)
        } else {
            val appLocale = LocaleListCompat.forLanguageTags(targetLang)
            AppCompatDelegate.setApplicationLocales(appLocale)
        }

        // Set default JVM locale for date formatters and domain logic
        val locale = Locale(targetLang)
        Locale.setDefault(locale)

        val config = context.resources.configuration
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)

        context.applicationContext.resources.let { appRes ->
            val appConfig = appRes.configuration
            appConfig.setLocale(locale)
            appRes.updateConfiguration(appConfig, appRes.displayMetrics)
        }

        if (restartActivity && context is Activity) {
            context.recreate()
        }
    }

    fun getCurrentLanguage(context: Context): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val localeManager = context.getSystemService(LocaleManager::class.java)
            val locales = localeManager?.applicationLocales
            if (locales != null && !locales.isEmpty) {
                locales.get(0).language
            } else {
                Locale.getDefault().language
            }
        } else {
            val currentLocales = AppCompatDelegate.getApplicationLocales()
            if (!currentLocales.isEmpty) {
                currentLocales.get(0)?.language ?: Locale.getDefault().language
            } else {
                Locale.getDefault().language
            }
        }
    }
}
