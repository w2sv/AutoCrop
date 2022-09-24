package com.autocrop.activities.main.fragments.flowfield.views

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import com.autocrop.activities.main.MainActivityViewModel
import com.autocrop.retriever.viewmodel.ViewModelRetriever
import com.autocrop.utils.android.MimeTypes

class ImageSelectionButton(context: Context, attributeSet: AttributeSet):
    AppCompatButton(context, attributeSet),
    ViewModelRetriever<MainActivityViewModel> by MainActivityViewModelRetriever(context) {

        init {
            setOnClickListener {
                with(sharedViewModel){
                    permissionsHandler.requestPermissionsOrRun {
                        selectImages.launch(
                            Intent(Intent.ACTION_PICK).apply {
                                type = MimeTypes.IMAGE
                                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                            }
                        )
                    }
                }
            }
        }
    }