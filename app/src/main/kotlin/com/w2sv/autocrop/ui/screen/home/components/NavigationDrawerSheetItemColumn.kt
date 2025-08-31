package com.w2sv.autocrop.ui.screen.home.components

import android.content.Context
import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ShareCompat
import androidx.core.net.toUri
import com.w2sv.androidutils.openUrl
import com.w2sv.androidutils.packagePlayStoreUrl
import com.w2sv.androidutils.startActivity
import com.w2sv.androidutils.widget.showToast
import com.w2sv.autocrop.R
import com.w2sv.autocrop.ui.theme.onSurfaceVariantLowAlpha
import com.w2sv.common.AppUrl
import com.w2sv.core.common.R.string as Strings

@Composable
internal fun NavigationDrawerSheetItemColumn(
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current
) {
    Column(modifier = modifier) {
        remember {
            listOf(
                NavigationDrawerSheetElement.Header(
                    titleRes = Strings.legal
                ),
                NavigationDrawerSheetElement.Item(
                    iconRes = R.drawable.ic_policy_24,
                    labelRes = Strings.privacy_policy,
                    onClick = { context.openUrl(AppUrl.PRIVACY_POLICY) }
                ),
                NavigationDrawerSheetElement.Item(
                    iconRes = R.drawable.ic_copyright_24,
                    labelRes = Strings.license,
                    onClick = { context.openUrl(AppUrl.LICENSE) }
                ),
                NavigationDrawerSheetElement.Header(
                    titleRes = Strings.support_the_app
                ),
                NavigationDrawerSheetElement.Item(
                    iconRes = R.drawable.ic_star_rate_24,
                    labelRes = Strings.rate,
                    explanationRes = Strings.rate_the_app_in_the_playstore,
                    onClick = {
                        context.startActivity(
                            intent = Intent(
                                Intent.ACTION_VIEW,
                                context.packagePlayStoreUrl.toUri()
                            )
                                .setPackage("com.android.vending"),
                            onActivityNotFoundException = {
                                it.showToast(context.getString(Strings.you_re_not_signed_into_the_play_store))
                            }
                        )
                    }
                ),
                NavigationDrawerSheetElement.Item(
                    iconRes = R.drawable.ic_share_24,
                    labelRes = Strings.share,
                    explanationRes = Strings.share_explanation,
                    onClick = {
                        ShareCompat.IntentBuilder(context)
                            .setType("text/plain")
                            .setText(context.getString(Strings.share_action_text, AppUrl.PLAY_STORE_ENTRY))
                            .startChooser()
                    }
                ),
                NavigationDrawerSheetElement.Item(
                    iconRes = R.drawable.ic_bug_report_24,
                    labelRes = Strings.report_a_bug_request_a_feature,
                    explanationRes = Strings.report_a_bug_explanation,
                    onClick = { context.openUrl(AppUrl.CREATE_ISSUE) }
                ),
                NavigationDrawerSheetElement.Item(
                    iconRes = R.drawable.ic_donate_24,
                    labelRes = Strings.support_development,
                    explanationRes = Strings.buy_me_a_coffee_as_a_sign_of_gratitude,
                    onClick = { context.openUrl(AppUrl.DONATE) }
                ),
                NavigationDrawerSheetElement.Header(
                    titleRes = Strings.more
                ),
                NavigationDrawerSheetElement.Item(
                    iconRes = R.drawable.ic_developer_24,
                    labelRes = Strings.developer,
                    explanationRes = Strings.check_out_my_other_apps,
                    onClick = { context.openUrl(AppUrl.GOOGLE_PLAY_DEVELOPER_PAGE) }
                ),
                NavigationDrawerSheetElement.Item(
                    iconRes = R.drawable.ic_github_24,
                    labelRes = Strings.source,
                    explanationRes = Strings.examine_the_app_s_source_code_on_github,
                    onClick = { context.openUrl(AppUrl.GITHUB_REPOSITORY) }
                )
            )
        }
            .forEach { element ->
                when (element) {
                    is NavigationDrawerSheetElement.Item -> {
                        Item(
                            item = element,
                            modifier = element.modifier
                        )
                    }

                    is NavigationDrawerSheetElement.Header -> {
                        SubHeader(
                            titleRes = element.titleRes,
                            modifier = element.modifier
                        )
                    }
                }
            }
    }
}

@Immutable
private sealed interface NavigationDrawerSheetElement {
    val modifier: Modifier

    @Immutable
    data class Header(
        @param:StringRes val titleRes: Int,
        override val modifier: Modifier = Modifier
            .padding(top = 20.dp, bottom = 4.dp)
    ) : NavigationDrawerSheetElement

    @Immutable
    data class Item(
        @param:DrawableRes val iconRes: Int,
        @param:StringRes val labelRes: Int,
        @param:StringRes val explanationRes: Int? = null,
        override val modifier: Modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        val onClick: () -> Unit
    ) : NavigationDrawerSheetElement
}

@Composable
private fun SubHeader(@StringRes titleRes: Int, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(id = titleRes),
        modifier = modifier,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun Item(item: NavigationDrawerSheetElement.Item, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.clickable(onClick = item.onClick)
    ) {
        MainItemRow(item = item, modifier = Modifier.fillMaxWidth())
        item.explanationRes?.let {
            Text(
                text = stringResource(id = it),
                color = MaterialTheme.colorScheme.onSurfaceVariantLowAlpha,
                modifier = Modifier.padding(start = iconSize + labelStartPadding),
                fontSize = 14.sp
            )
        }
    }
}

private val iconSize = 28.dp
private val labelStartPadding = 16.dp

@Composable
private fun MainItemRow(item: NavigationDrawerSheetElement.Item, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.size(size = iconSize),
            painter = painterResource(id = item.iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = stringResource(id = item.labelRes),
            modifier = Modifier.padding(start = labelStartPadding),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}
