package com.autocrop.activities

object IntentIdentifier {
    val SELECTED_IMAGE_URI_STRINGS: String = intentExtraIdentifier("selected_image_uri_strings")
    val N_DISMISSED_IMAGES: String = intentExtraIdentifier("n_dismissed_images")
    val N_SAVED_CROPS_WITH_N_DELETED_SCREENSHOTS: String = intentExtraIdentifier("n_saved_crops")
    val CROP_WRITE_DIR_PATH: String = intentExtraIdentifier("crop_write_dir_path")

    private fun intentExtraIdentifier(name: String): String = "com.autocrop.$name"
}