package com.example.ui.theme

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.data.model.AppLanguageOption
import java.util.Locale

object LocaleHelper {
    fun buildLocalizedContext(base: Context, languageCode: String): Context {
        val lang = AppLanguageOption.fromCode(languageCode)
        val locale = Locale.forLanguageTag(lang.code)
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }
        config.setLayoutDirection(locale)
        return base.createConfigurationContext(config)
    }

    fun applyRuntimeLocale(context: Context, languageCode: String) {
        val lang = AppLanguageOption.fromCode(languageCode)
        val locale = Locale.forLanguageTag(lang.code)
        Locale.setDefault(locale)
        val res = context.resources
        val config = Configuration(res.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }
        config.setLayoutDirection(locale)
        @Suppress("DEPRECATION")
        res.updateConfiguration(config, res.displayMetrics)
    }
}

@Composable
fun ProvideLocalizedAppContext(
    languageCode: String,
    content: @Composable () -> Unit
) {
    val baseContext = LocalContext.current
    val langOption = remember(languageCode) { AppLanguageOption.fromCode(languageCode) }
    val localizedContext = remember(baseContext, langOption.code) {
        LocaleHelper.applyRuntimeLocale(baseContext, langOption.code)
        LocaleHelper.buildLocalizedContext(baseContext, langOption.code)
    }
    val direction = if (langOption.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalLayoutDirection provides direction
    ) {
        content()
    }
}
