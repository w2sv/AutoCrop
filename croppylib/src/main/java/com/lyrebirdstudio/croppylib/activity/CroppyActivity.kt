package com.lyrebirdstudio.croppylib.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.lyrebirdstudio.croppylib.R
import com.lyrebirdstudio.croppylib.databinding.ActivityCroppyBinding
import com.lyrebirdstudio.croppylib.CropRequest
import com.lyrebirdstudio.croppylib.fragment.ImageCropFragment

class CroppyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCroppyBinding
    private lateinit var viewModel: CroppyActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val cropRequest: CropRequest = intent.getParcelableExtra(KEY_CROP_REQUEST)!!

        binding = ActivityCroppyBinding.inflate(layoutInflater)
            .apply {
                setContentView(root)
            }

        viewModel = ViewModelProvider(this)[CroppyActivityViewModel::class.java]
            .apply {
                exitActivityAnimation = cropRequest.exitActivityAnimation
            }

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .add(
                    R.id.containerCroppy,
                    ImageCropFragment.newInstance(cropRequest)
                        .apply {
                            onApplyClicked = { cropRect ->
                                setResult(
                                    Activity.RESULT_OK,
                                    Intent()
                                        .apply {
                                            data = cropRequest.sourceUri
                                            putExtra(KEY_CROP_RECT_STRING_EXTRA, cropRect.flattenToString())
                                        }
                                )
                                finish()
                            }

                            onCancelClicked = {
                                setResult(Activity.RESULT_CANCELED)
                                finish()
                            }
                        }
                )
                .commitAllowingStateLoss()
        }
    }

    override fun finish() {
        super.finish()

        viewModel.exitActivityAnimation?.invoke(this)
    }

    companion object {

        private const val KEY_CROP_REQUEST = "KEY_CROP_REQUEST"
        private const val KEY_CROP_RECT_STRING_EXTRA = "KEY_CROP_RECT_STRING_EXTRA"

        fun newIntent(context: Context, cropRequest: CropRequest): Intent {
            return Intent(context, CroppyActivity::class.java)
                .putExtras(
                    Bundle()
                        .apply {
                            putParcelable(KEY_CROP_REQUEST, cropRequest)
                        }
                )
        }

        fun getCropRect(intent: Intent): Rect =
            Rect.unflattenFromString(intent.getStringExtra(KEY_CROP_RECT_STRING_EXTRA))!!
    }
}