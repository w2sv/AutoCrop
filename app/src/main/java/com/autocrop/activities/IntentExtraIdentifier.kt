package com.autocrop.activities

object IntentExtraIdentifier {
    val SELECTED_IMAGE_URIS = intentExtraIdentifier("selected_image_uri_strings")
    val N_DISMISSED_IMAGES = intentExtraIdentifier("n_dismissed_images")

    val EXAMINATION_ACTIVITY_RESULTS = intentExtraIdentifier("examination_activity_results")
    val CROP_SAVING_URIS = intentExtraIdentifier("crop_saving_uris")

    private fun intentExtraIdentifier(name: String): String = "com.autocrop.$name"
}