package com.autocrop.activities

object IntentExtraIdentifier {
    val SELECTED_IMAGE_URIS = intentExtraIdentifier("selected_image_uri_strings")
    val N_DISMISSED_IMAGES = intentExtraIdentifier("n_dismissed_images")

    val N_SAVED_CROPS_WITH_N_DELETED_SCREENSHOTS = intentExtraIdentifier("n_saved_crops")
    val CROP_WRITE_DIR_PATH = intentExtraIdentifier("crop_write_dir_path")
    val CROP_SAVING_URIS = intentExtraIdentifier("crop_saving_uris")

    private fun intentExtraIdentifier(name: String): String = "com.autocrop.$name"
}