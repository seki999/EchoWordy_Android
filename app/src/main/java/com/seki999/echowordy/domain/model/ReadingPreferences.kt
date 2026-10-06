package com.seki999.echowordy.domain.model

enum class ReadingTheme(val label: String) {
    SOFT("柔和浅色"), WARM("暖色护眼"), DARK("深色");
}
enum class ReadingFontSize(val label: String, val scale: Float) {
    SMALL("小", 0.90f), STANDARD("标准", 1f), LARGE("大", 1.15f), EXTRA_LARGE("特大", 1.30f);
}
data class ReadingPreferences(
    val theme: ReadingTheme = ReadingTheme.SOFT,
    val fontSize: ReadingFontSize = ReadingFontSize.LARGE,
)
