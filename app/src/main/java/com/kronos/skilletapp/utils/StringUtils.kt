package com.kronos.skilletapp.utils

import android.webkit.URLUtil.isValidUrl

fun String.removePunctuation(): String {
  val punctuationPattern = """[,.()/]""".toPattern()
  return punctuationPattern.matcher(this).replaceAll("")
}

fun String.normalizeWhitespace(): String {
  val whitespacePattern = """\s+""".toPattern()
  return whitespacePattern.matcher(this).replaceAll(" ")
}

fun String.pluralize(count: Int, pluralizer: StringBuilder.() -> Unit) = if (count == 1) this else buildString {
  append(this@pluralize)
  pluralizer()
}

fun String.pluralize(count: Int) = pluralize(count) {
  val esPlurals = arrayOf("x", "s", "sh", "ch", "z", "o")
  if (endsWith(suffixes = esPlurals, ignoreCase = true)) {
    append("e")
  } else if ("y".contains(last().lowercaseChar())) {
    replace(lastIndex, lastIndex + 1, "ie")
  }

  append("s")
}

fun String.endsWith(vararg suffixes: String, ignoreCase: Boolean = false) = suffixes.any { endsWith(it, ignoreCase) }

fun CharSequence?.isNotNullOrBlank() = !isNullOrBlank()
fun CharSequence.isValidUrl() = isValidUrl(this.toString())