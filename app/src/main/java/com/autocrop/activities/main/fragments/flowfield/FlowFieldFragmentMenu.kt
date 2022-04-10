package com.autocrop.activities.main.fragments.flowfield

import android.content.Context
import android.text.SpannableStringBuilder
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.core.text.color
import androidx.core.text.italic
import com.autocrop.global.BooleanUserPreferences
import com.autocrop.utils.android.getColorInt
import com.autocrop.utils.android.makeIconsVisible
import com.autocrop.utils.android.persistMenuAfterClick
import com.w2sv.autocrop.R

class FlowFieldFragmentMenu(itemId2OnCLickListener: Map<Int, () -> Unit>, context: Context, anchor: View)
    : PopupMenu(context, anchor){

        init{
            menuInflater.inflate(R.menu.activity_main, menu)

            setCheckableItems(context)
            setOnClickListeners(itemId2OnCLickListener)
            styleGroupDividers(context)
            menu.makeIconsVisible()
        }

    /**
     * Sets check and [setOnMenuItemClickListener]
     */
    private fun setCheckableItems(context: Context) = mapOf(
        R.id.main_menu_item_conduct_auto_scrolling to BooleanUserPreferences.Keys.conductAutoScrolling
    )
        .forEach { (id, userPreferencesKey) ->
            with(menu.findItem(id)){
                isChecked = BooleanUserPreferences.getValue(userPreferencesKey)

                setOnMenuItemClickListener { item ->
                    BooleanUserPreferences.toggle(userPreferencesKey)
                    isChecked = !isChecked
                    item.persistMenuAfterClick(context)
                }
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
                        R.color.saturated_magenta,
                        context
                    )
                ) {append(title)} }
            }
        }
    }