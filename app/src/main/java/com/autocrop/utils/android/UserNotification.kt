package com.autocrop.utils.android

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.w2sv.autocrop.R


object NotificationColor {
    const val NEUTRAL: Int = R.color.light_gray
    const val SUCCESS: Int = R.color.light_green
}

//$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
// from String and textColorId $
//$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$

fun Activity.displaySnackbar(
    message: String,
    @ColorRes textColorId: Int,
    displayDuration: Int = Snackbar.LENGTH_LONG,
    @DrawableRes icon: Int? = null) =
        Snackbar
            .make(findViewById(android.R.id.content), message, displayDuration)
            .apply {
                view.
                configuredTextView(icon)
                    .apply { setTextColor(getColorInt(textColorId, context)) }
            }
            .show()

//$$$$$$$$$$$$$$$$$
// from Spannable $
//$$$$$$$$$$$$$$$$$

fun Activity.displaySnackbar(
    message: Spannable,
    displayDuration: Int = Snackbar.LENGTH_LONG,
    @DrawableRes icon: Int? = null) =
        Snackbar
            .make(findViewById(android.R.id.content), message, displayDuration)
            .apply {
                view.configuredTextView(icon)
            }
            .show()

private fun View.configuredTextView(@DrawableRes icon: Int?): TextView = @Suppress("UNRESOLVED_REFERENCE")
    findViewById<TextView>(com.google.android.material.R.id.snackbar_text).apply {
        textAlignment = View.TEXT_ALIGNMENT_CENTER
        maxLines = 2
        icon?.let {
//            setCompoundDrawablesWithIntrinsicBounds(context.scaledDrawable(it, 42, 42), null, null, null)
            setCompoundDrawablesWithIntrinsicBounds(it, 0,0, 0)
        }
    }

fun Context.scaledDrawable(@DrawableRes id: Int, width: Int, height: Int): Drawable =
    BitmapDrawable(
        resources,
        Bitmap.createScaledBitmap(
            ContextCompat.getDrawable(this, id)!!.toBitmap(),
            width,
            height,
        false
        )
    )

fun Drawable.toBitmap(): Bitmap =
    if (this is BitmapDrawable)
        bitmap
    else
        Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888).apply {
            val canvas = Canvas(this)
            setBounds(0, 0, canvas.width, canvas.height)
            draw(canvas)
        }
