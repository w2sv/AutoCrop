package com.autocrop.uielements

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
import com.autocrop.utils.BlankFun
import com.autocrop.utilsandroid.getColorInt
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

    @Suppress("DEPRECATION")
    protected fun setIconColor(context: Context, @ColorRes colorRes: Int) =
        menu.children.forEach {
            it.icon?.setColorFilter(
                getColorInt(colorRes, context),
                PorterDuff.Mode.SRC_ATOP
            )
        }

    protected fun styleGroupDividers(context: Context, @IdRes dividerItemResources: Array<Int>) =
        dividerItemResources
            .forEach { id ->
                with(menu.findItem(id)){
                    title = SpannableStringBuilder().italic { color(
                        getColorInt(
                            R.color.dark_gray,
                            context
                        )
                    ) {append(title)} }
                }
            }
}