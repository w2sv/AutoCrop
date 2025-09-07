package com.w2sv.autocrop.ui.screen.pager.dialogs

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.w2sv.autocrop.R
import com.w2sv.autocrop.ui.theme.AppTheme

@Composable
fun ProcessCropBundleDialog(
    deleteScreenshots: () -> Boolean,
    toggleDeleteScreenshots: () -> Unit,
    onConfirmation: () -> Unit,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = { Icon(painterResource(R.drawable.ic_save_24), contentDescription = null, modifier = Modifier.size(36.dp)) },
        title = { Text(stringResource(com.w2sv.core.common.R.string.save_crop)) },
        text = {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = deleteScreenshots(), onCheckedChange = { toggleDeleteScreenshots() })
                Text(
                    pluralStringResource(com.w2sv.core.common.R.plurals.delete_corresponding_screenshots, count = 1),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        },
        confirmButton = {
            Button(onConfirmation) { Text(stringResource(com.w2sv.core.common.R.string.yes)) }
        },
        dismissButton = {
            Button(onDismissRequest) { Text(stringResource(com.w2sv.core.common.R.string.no)) }
        }
    )
}

@Preview
@Composable
private fun Prev() {
    AppTheme {
        ProcessCropBundleDialog(
            { true },
            {},
            {},
            {}
        )
    }
}
