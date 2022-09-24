package com.autocrop.ui.elements.menu

import android.content.Context
import android.graphics.PorterDuff
import android.text.SpannableStringBuilder
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.IdRes
import androidx.annotation.MenuRes
import androidx.appcompat.widget.PopupMenu
import androidx.core.text.color
import androidx.core.text.italic
import androidx.core.view.children
import com.autocrop.utils.kotlin.BlankFun
import com.autocrop.utils.android.getThemedColor
import com.w2sv.autocrop.R

abstract class ExtendedPopupMenu(context: Context, anchor: View, @MenuRes menuResourceId: Int)
    : PopupMenu(context, anchor){

    init {
        menuInflater.inflate(menuResourceId, menu)
    }

    protected fun setItemOnClickListeners(@IdRes itemId2OnCLickListener: Map<Int, BlankFun>) =
        itemId2OnCLickListener.forEach { (id, onClickListener) ->
            menu.findItem(id)
                .setOnMenuItemClickListener {
                    onClickListener()
                    true
                }
        }

    protected fun setIcons(context: Context, @ColorRes colorRes: Int){
        setIconsColor(context, colorRes)
        menu.makeIconsVisible()
    }

    private fun setIconsColor(context: Context, @ColorRes colorRes: Int) =
        menu.children.forEach {
            @Suppress("DEPRECATION")
            it.icon?.setColorFilter(
                context.getThemedColor(colorRes),
                PorterDuff.Mode.SRC_ATOP
            )
        }

    protected fun styleGroupDividers(context: Context, @IdRes dividerItemResources: Array<Int>) =
        dividerItemResources
            .forEach { id ->
                with(menu.findItem(id)){
                    title = SpannableStringBuilder().italic { color(context.getThemedColor(R.color.dark_gray)) {append(title)} }
                }
            }
}