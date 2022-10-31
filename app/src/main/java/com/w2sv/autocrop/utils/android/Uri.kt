package com.w2sv.autocrop.utils.android

import android.net.Uri

fun documentUriPathIdentifier(documentUri: Uri): String =
    documentUri.pathSegments[1]