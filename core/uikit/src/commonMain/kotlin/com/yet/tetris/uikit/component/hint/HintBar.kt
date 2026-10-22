package com.yet.tetris.uikit.component.hint

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Grey = Color(0xFFE6E6E6)

/**
 * The button hints along the bottom of a screen, in the andr36oid look: 24 dp tall, black,
 * optional name of the screen on the left, then pairs of button and what it does on the right.
 *
 * @param leading text on the left, such as the name of the screen; null for none
 * @param hints pairs of button and what it does: "A" to "Select"
 */
@Composable
fun HintBar(
    modifier: Modifier = Modifier,
    leading: String? = null,
    hints: List<Pair<String, String>>,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(HEIGHT)
                .background(Color.Black)
                .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = leading.orEmpty(),
            modifier = Modifier.weight(1f),
            color = Grey,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
        ) {
            hints.forEach { (key, label) ->
                Box(
                    modifier = Modifier.padding(start = 12.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        KeyBadge(key)
                        Text(
                            text = label,
                            modifier = Modifier.padding(start = 4.dp),
                            color = Grey,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Start,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

/** A button as the bar shows it: a light pill with its name in dark bold letters */
@Composable
private fun KeyBadge(key: String) {
    Box(
        modifier =
            Modifier
                .height(18.dp)
                .widthIn(min = 18.dp)
                .background(Grey, RoundedCornerShape(9.dp))
                .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = key,
            color = Color(0xFF2E2E2E),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
    }
}

/** The bar's height in the andr36oid look */
val HEIGHT = 24.dp
