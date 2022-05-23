package com.autocrop.activities.main.fragments.flowfield

import android.content.Context
import android.graphics.PorterDuff
import android.text.SpannableStringBuilder
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.core.text.color
import androidx.core.text.italic
import androidx.core.view.children
import com.autocrop.global.BooleanPreferences
import com.autocrop.uielements.makeIconsVisible
import com.autocrop.uielements.persistMenuAfterClick
import com.autocrop.utils.BlankFun
import com.autocrop.utilsandroid.getColorInt
import com.w2sv.autocrop.R

class FlowFieldFragmentMenu(itemId2OnCLickListener: Map<Int, BlankFun>, context: Context, anchor: View)
    : PopupMenu(context, anchor){

    init{
        menuInflater.inflate(R.menu.activity_main, menu)

        setIconColor(context)
        setCheckableItems(context)
        setOnClickListeners(itemId2OnCLickListener)
        styleGroupDividers(context)
        menu.makeIconsVisible()
    }

    /**
     * Sets check and [setOnMenuItemClickListener]
     */
    private fun setCheckableItems(context: Context) = mapOf(
        R.id.main_menu_item_auto_scroll to "autoScroll"
    )
        .forEach { (id, userPreferencesKey) ->
            with(menu.findItem(id)){
                val value = BooleanPreferences.getValue(userPreferencesKey)
                isChecked = value

                setOnMenuItemClickListener { item ->
                    BooleanPreferences[userPreferencesKey] = !value
                    isChecked = !isChecked
                    item.persistMenuAfterClick(context)
                }
            }
    }

    @Suppress("DEPRECATION")
    private fun setIconColor(context: Context){
        menu.children.forEach {
            it.icon?.setColorFilter(getColorInt(R.color.magenta_bright, context), PorterDuff.Mode.SRC_ATOP)
        }
    }

    private fun setOnClickListeners(itemId2OnCLickListener: Map<Int, () -> Unit>) = itemId2OnCLickListener.forEach { (id, onClickListener) ->
            with(menu.findItem(id)){
                setOnMenuItemClickListener {
                    onClickListener()
                    true
                }
            }
        }

    private fun styleGroupDividers(context: Context) = arrayOf(
        R.id.main_menu_group_divider_examination,
        R.id.main_menu_group_divider_crop_saving,
        R.id.main_menu_group_divider_other
    )
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