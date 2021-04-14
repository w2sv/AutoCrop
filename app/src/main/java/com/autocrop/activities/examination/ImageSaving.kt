package com.autocrop.activities.examination

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.autocrop.GlobalParameters
import com.autocrop.utils.deleteUnderlyingMediaFile
import com.autocrop.utils.nameMediaFile
import com.autocrop.utils.pathMediaFile
import java.io.FileOutputStream


fun saveImageAndDeleteScreenshotIfApplicable(
    uri: Uri,
    image: Bitmap,
    context: Context,
    contentResolver: ContentResolver
) {

    // delete screenshot if applicable
    if (GlobalParameters.deleteInputScreenshots!!)
        uri.deleteUnderlyingMediaFile(context)

    // save image under original file path
    if (GlobalParameters.saveDirectoryPath != null){
        val destinationFilePath = "${GlobalParameters.saveDirectoryPath}/${uri.nameMediaFile(context)}"
        Log.i("ImageSaving", "Saving to $destinationFilePath")
        saveImage(image, destinationFilePath)
    }
    else
        insertImageIntoMediaStore(
            contentResolver,
            image,
            uri.pathMediaFile(context)
        )
}


fun insertImageIntoMediaStore(contentResolver: ContentResolver, image: Bitmap, title: String?){
    MediaStore.Images.Media.insertImage(
        contentResolver,
        image,
        title,
        ""
    )
}


fun saveImage(image: Bitmap, destinationFilePath: String){
    with(FileOutputStream(destinationFilePath)){
        image.compress(Bitmap.CompressFormat.PNG, -1, this)
        this.flush()
        this.close()
    }
}