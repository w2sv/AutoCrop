package com.autocrop.utils.android

import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import java.io.File
import java.nio.file.Paths
import kotlin.io.path.Path

val Uri.fileName: String
    get() = File(path!!).name

// TODO
val Uri.parentDirPath: String
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && DocumentsContract.isTreeUri(this))
            pathSegments[1]
        else
            pathSegments.run { subList(0, lastIndex).joinToString(separator = "/") }

fun buildDocumentUriFromTreeUri(treeUri: Uri): Uri =
    DocumentsContract.buildDocumentUriUsingTree(treeUri, DocumentsContract.getTreeDocumentId(treeUri))