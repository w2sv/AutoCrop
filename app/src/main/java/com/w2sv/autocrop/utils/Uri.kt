package com.w2sv.autocrop.utils

import android.net.Uri

fun documentUriPathIdentifier(documentUri: Uri): String =
    documentUri.pathSegments[1]