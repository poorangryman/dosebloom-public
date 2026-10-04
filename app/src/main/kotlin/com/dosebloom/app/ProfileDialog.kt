package com.dosebloom.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ProfileDialog(
    activity: RefactoredMainActivity,
    profiles: List<String>,
    current: String,
    onSelect: (String) -> Unit,
    onAdd: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = { onSelect(current) },
        shape = RoundedCornerShape(24.dp),
        title = { Text(stringResource(R.string.profile), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                profiles.forEach { p ->
                    val displayName = Localization.profileDisplayName(activity, p)
                    Surface(
                        onClick = { onSelect(p) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (p == current) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_profile),
                                contentDescription = null,
                                tint = if (p == current) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp).padding(end = 8.dp)
                            )
                            Text(
                                displayName,
                                Modifier.weight(1f),
                                fontWeight = if (p == current) FontWeight.Bold else FontWeight.Normal
                            )
                            if (p != "Я") {
                                IconButton(onClick = { onDelete(p) }) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_delete),
                                        contentDescription = stringResource(R.string.delete_profile),
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                HorizontalDivider(Modifier.padding(vertical = 6.dp))
                OutlinedTextField(
                    name,
                    { name = it },
                    label = { Text(stringResource(R.string.new_profile)) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    enabled = name.isNotBlank(),
                    onClick = { onAdd(name); name = "" },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                ) {
                    Text(stringResource(R.string.add_and_select))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSelect(current) }) {
                Text(stringResource(R.string.done))
            }
        }
    )
}
