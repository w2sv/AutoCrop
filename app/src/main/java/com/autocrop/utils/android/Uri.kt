package com.autocrop.utils.android

import android.net.Uri
import android.provider.DocumentsContract

fun buildDocumentUriFromTreeUri(treeUri: Uri): Uri =
    DocumentsContract.buildDocumentUriUsingTree(
        treeUri,
        DocumentsContract.getTreeDocumentId(treeUri)
    )

fun documentUriPathIdentifier(documentUri: Uri): String =
    documentUri.pathSegments[1]  // TODO