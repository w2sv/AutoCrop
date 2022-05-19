package com.autocrop.activities.main.fragments.flowfield.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import com.autocrop.activities.main.MainActivityViewModel
import com.autocrop.uicontroller.ViewModelRetriever
import com.autocrop.utilsandroid.MimeTypes

class ImageSelectionButton(context: Context, attributeSet: AttributeSet):
    AppCompatButton(context, attributeSet),
    ViewModelRetriever<MainActivityViewModel> by MainActivityViewModelRetriever(context) {

        init {
            setOnClickListener {
                with(sharedViewModel){
                    permissionsHandler.requestPermissionsOrRun {
                        selectImages.launch(MimeTypes.IMAGE)
                    }
                }
            }
        }
    }