package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DisableProtectionDialog(
    hasActiveLockouts: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFF87171),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Disable Protection?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Turning protection off will disable automatic content monitoring.",
                    fontSize = 14.sp,
                    color = Color(0xFFE2E8F0),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "FocusGuard will stop scanning active applications for adult content until protection is manually re-enabled.",
                    fontSize = 13.sp,
                    color = Color(0xFF94A3B8),
                    lineHeight = 18.sp
                )
                if (hasActiveLockouts) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF450A0A),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Note: Any existing active lockouts will remain enforced until their countdown timer expires.",
                            fontSize = 12.sp,
                            color = Color(0xFFFCA5A5),
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFDC2626),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Confirm Turn OFF", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Keep Protection ON", color = Color.White)
            }
        },
        containerColor = Color(0xFF1E293B),
        shape = RoundedCornerShape(20.dp)
    )
}
