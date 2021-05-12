package com.autocrop.utils.android

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.autocrop.activities.examination.N_SAVED_CROPS
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.bunsenbrenner.screenshotboundremoval.R
import com.google.android.material.snackbar.Snackbar


fun Activity.displayToast(message: String) {
    Toast.makeText(
        this,
        message,
        Toast.LENGTH_LONG
    ).apply {
        with(view!!) {
            setBackgroundColor(Color.parseColor("darkgray"))

            findViewById<TextView>(android.R.id.message).apply {
                setTextColor(Color.parseColor("white"))
                gravity = Gravity.CENTER
                with(Pair(14, 0)) {
                    setPadding(first, second, first, second)
                }
            }
        }
        show()
    }
}


fun Activity.displaySnackbar(message: String, textColorId: Int = R.color.light_green){
    Snackbar
        .make(
            findViewById(android.R.id.content),
            message,
            Snackbar.LENGTH_LONG
        )
        .apply{
            with(view){
                findViewById<TextView>(com.google.android.material.R.id.snackbar_text).apply {
                    setTextColor(resources.getColor(textColorId, theme))
                    textAlignment = View.TEXT_ALIGNMENT_CENTER
                    maxLines = 2
                }
            }
        }
        .show()
}


fun Activity.persistMenuAfterItemClick(item: MenuItem): Boolean = item.run {
    setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW)
    actionView = View(this@persistMenuAfterItemClick)
    setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
        override fun onMenuItemActionExpand(item: MenuItem): Boolean = false
        override fun onMenuItemActionCollapse(item: MenuItem): Boolean = false
    })
    false
}


fun Activity.permissionGranted(permission: String): Boolean =
    checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED


fun Activity.proceedTransitionAnimation(){
    Animatoo.animateSwipeLeft(this)
}


fun Activity.returnTransitionAnimation(){
    Animatoo.animateSwipeRight(this)
}


fun Activity.restartTransitionAnimation(){
    Animatoo.animateFade(this)
}


//
// TODO
fun Activity.snackbarArgument(alreadyDisplayed: Boolean, extraName: String, defaultValue: Int): Int? =
    intent.getIntExtra(extraName, defaultValue).run {
        if (!alreadyDisplayed && !equals(defaultValue))
            this
        else
            null
    }