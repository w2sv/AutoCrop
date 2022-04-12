package com.autocrop.utils.android

import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import java.io.File
import java.nio.file.Paths
import kotlin.io.path.Path

val Uri.fileName: String
    get() = File(path!!).name

fun buildDocumentUriFromTreeUri(treeUri: Uri): Uri =
    DocumentsContract.buildDocumentUriUsingTree(treeUri, DocumentsContract.getTreeDocumentId(treeUri))