package com.kronos.skilletapp.utils

fun String.removePunctuation(): String {
  val punctuationPattern = """[,.()/]""".toPattern()
  return punctuationPattern.matcher(this).replaceAll("")
}

fun String.normalizeWhitespace(): String {
  val whitespacePattern = """\s+""".toPattern()
  return whitespacePattern.matcher(this).replaceAll(" ")
}

fun String.pluralize(count: Int, pluralizer: (String) -> String) = let {
  if (count == 1) this else pluralizer(this)
}

fun CharSequence?.isNotNullOrBlank() = !isNullOrBlank()