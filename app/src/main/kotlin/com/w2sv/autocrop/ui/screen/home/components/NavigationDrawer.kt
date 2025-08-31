package com.w2sv.autocrop.ui.screen.home.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.w2sv.autocrop.BuildConfig
import com.w2sv.autocrop.ui.util.compose.SystemBarsIgnoringVisibilityPaddedColumn
import com.w2sv.autocrop.ui.util.compose.emptyInsets
import kotlinx.coroutines.launch
import com.w2sv.core.common.R.string as Strings

@Composable
fun NavigationDrawer(
    state: DrawerState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        modifier = modifier,
        drawerContent = { NavigationDrawerSheet() },
        drawerState = state,
        content = content
    )

    BackHandler(enabled = state.isOpen) {
        scope.launch { state.close() }
    }
}

@Composable
private fun NavigationDrawerSheet(modifier: Modifier = Modifier) {
    ModalDrawerSheet(
        modifier = modifier,
        windowInsets = emptyInsets,
        drawerContainerColor = Color.Transparent,
        drawerContentColor = MaterialTheme.colorScheme.onSurface
    ) {
        SystemBarsIgnoringVisibilityPaddedColumn(horizontalAlignment = Alignment.CenterHorizontally) {
            Header(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding)
            )
            HorizontalDivider(
                modifier = Modifier.padding(top = 16.dp),
                color = MaterialTheme.colorScheme.onSurface,
                thickness = Dp.Hairline
            )
            NavigationDrawerSheetItemColumn(
                modifier = Modifier
                    .padding(horizontal = horizontalPadding)
                    .verticalScroll(rememberScrollState())
            )
        }
    }
}

private val horizontalPadding = 24.dp

@Composable
private fun Header(modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(54.dp))
        Image(
            painterResource(id = com.w2sv.core.common.R.drawable.logo_nobackground),
            null,
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .padding(6.dp)
        )
        Spacer(modifier = Modifier.height(22.dp))
        Text(
            text = stringResource(id = Strings.version).format(BuildConfig.VERSION_NAME)
        )
    }
}
