package com.autocrop.utils.android

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Binder
import android.provider.DocumentsContract
import java.io.File

val Uri.fileName: String
    get() = File(path!!).name

fun Context.uriPermissionGranted(uri: Uri, permissionCode: Int): Boolean =
    checkUriPermission(
        uri,
        null,
        null,
        Binder.getCallingPid(),
        Binder.getCallingUid(),
        permissionCode
    ) == PackageManager.PERMISSION_GRANTED

fun buildDocumentUriFromTreeUri(treeUri: Uri): Uri =
    DocumentsContract.buildDocumentUriUsingTree(treeUri, DocumentsContract.getTreeDocumentId(treeUri))