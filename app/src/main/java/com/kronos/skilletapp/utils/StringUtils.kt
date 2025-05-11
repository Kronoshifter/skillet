package com.kronos.skilletapp.utils

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
  if ("sx".contains(last().lowercaseChar())) {
    append("e")
  } else if ("y".contains(last().lowercaseChar())) {
    replace(lastIndex, lastIndex + 1, "ie")
  }

  append("s")
}

fun CharSequence?.isNotNullOrBlank() = !isNullOrBlank()