package com.kronos.skilletapp.utils

import android.net.Uri
import androidx.navigation.NavDeepLinkRequest

@DslMarker public annotation class NavDeepLinkRequestDsl

fun navDeepLinkRequest(uri: Uri, requestBuilder: NavDeepLinkRequestBuilder.() -> Unit): NavDeepLinkRequest =
  NavDeepLinkRequestBuilder().apply(requestBuilder).fromUri(uri)

fun navDeepLinkRequestFromAction(action: String, requestBuilder: NavDeepLinkRequestBuilder.() -> Unit): NavDeepLinkRequest =
  NavDeepLinkRequestBuilder().apply(requestBuilder).fromAction(action)

fun navDeepLinkRequestFromMimeType(mimeType: String, requestBuilder: NavDeepLinkRequestBuilder.() -> Unit): NavDeepLinkRequest =
  NavDeepLinkRequestBuilder().apply(requestBuilder).fromMimeType(mimeType)

@NavDeepLinkRequestDsl
class NavDeepLinkRequestBuilder {
  var action: String? = null
  var mimeType: String? = null
  var uri: Uri? = null

  internal fun fromUri(uri: Uri) = NavDeepLinkRequest.Builder.fromUri(uri).apply {
    action?.let { setAction(it) }
    mimeType?.let { setMimeType(it) }
  }.build()

  internal fun fromAction(action: String) = NavDeepLinkRequest.Builder.fromAction(action).apply {
    mimeType?.let { setMimeType(it) }
    uri?.let { setUri(it) }
  }.build()

  internal fun fromMimeType(mimeType: String) = NavDeepLinkRequest.Builder.fromMimeType(mimeType).apply {
    action?.let { setAction(it) }
    uri?.let { setUri(it) }
  }.build()
}