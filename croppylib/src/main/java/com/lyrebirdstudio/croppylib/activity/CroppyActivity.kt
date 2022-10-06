package com.lyrebirdstudio.croppylib.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.lyrebirdstudio.croppylib.CroppyRequest
import com.lyrebirdstudio.croppylib.R
import com.lyrebirdstudio.croppylib.databinding.ActivityCroppyBinding
import com.lyrebirdstudio.croppylib.fragment.CroppyFragment
import com.lyrebirdstudio.croppylib.CropEdges

class CroppyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCroppyBinding
    private lateinit var viewModel: CroppyActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCroppyBinding.inflate(layoutInflater)
            .apply {
                setContentView(root)
            }

        val cropRequest: CroppyRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(KEY_CROP_REQUEST, CroppyRequest::class.java)!!
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(KEY_CROP_REQUEST)!!
        }

        viewModel = ViewModelProvider(this)[CroppyActivityViewModel::class.java]
            .apply {
                exitAnimation = cropRequest.exitActivityAnimation
            }

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .add(
                    R.id.croppy_container,
                    CroppyFragment.instance(cropRequest)
                        .apply {
                            onApplyClicked = { edges ->
                                setResult(
                                    Activity.RESULT_OK,
                                    Intent()
                                        .apply {
                                            data = cropRequest.uri
                                            putExtra(
                                                ADJUSTED_CROP_EDGES_EXTRA,
                                                edges
                                            )
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

        viewModel.exitAnimation?.invoke(this)
    }

    companion object {
        private const val KEY_CROP_REQUEST = "KEY_CROP_REQUEST"
        private const val ADJUSTED_CROP_EDGES_EXTRA = "KEY_CROP_RECT_STRING_EXTRA"

        fun intent(context: Context, croppyRequest: CroppyRequest): Intent =
            Intent(context, CroppyActivity::class.java)
                .putExtras(
                    Bundle()
                        .apply {
                            putParcelable(KEY_CROP_REQUEST, croppyRequest)
                        }
                )

        fun getCropEdges(croppyResultIntent: Intent): CropEdges =
            @Suppress("DEPRECATION")
            croppyResultIntent.getParcelableExtra(ADJUSTED_CROP_EDGES_EXTRA)!!
    }
}