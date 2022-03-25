package com.autocrop.activities

import com.autocrop.utils.android.intentExtraIdentifier

object IntentIdentifiers {
    val SELECTED_IMAGE_URI_STRINGS: String = intentExtraIdentifier("selected_image_uri_strings")
    val N_DISMISSED_IMAGES: String = intentExtraIdentifier("n_dismissed_images")
    val N_SAVED_CROPS: String = intentExtraIdentifier("n_saved_crops")
}