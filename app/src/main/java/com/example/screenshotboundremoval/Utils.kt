package com.example.screenshotboundremoval

fun String.replaceMultiple(toBeReplaced: List<String>, replaceWith: String): String = this.run { var copy = this; toBeReplaced.forEach { copy = copy.replace(it, replaceWith) }; copy }
