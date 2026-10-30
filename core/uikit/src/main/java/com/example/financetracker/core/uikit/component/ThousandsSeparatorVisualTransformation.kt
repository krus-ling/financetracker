package com.example.financetracker.core.uikit.component

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Визуальное разделение тысяч пробелами (например: 10000 -> 10 000, 1250000.50 -> 1 250 000.50)
 * с поддержкой двустороннего маппинга курсора.
 */
class ThousandsSeparatorVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        if (raw.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val hasComma = raw.contains(',')
        val hasDot = raw.contains('.')
        val separator = if (hasComma) ',' else if (hasDot) '.' else null
        val parts = if (separator != null) raw.split(separator, limit = 2) else listOf(raw)
        val intPart = parts[0]
        val intLen = intPart.length

        val sb = StringBuilder()
        val origToTrans = IntArray(raw.length + 1)
        val transToOrigList = ArrayList<Int>(raw.length + 10)

        for (i in raw.indices) {
            origToTrans[i] = sb.length
            sb.append(raw[i])
            transToOrigList.add(i)

            if (i < intLen) {
                val rem = intLen - 1 - i
                if (rem > 0 && rem % 3 == 0) {
                    sb.append(' ')
                    transToOrigList.add(i + 1)
                }
            }
        }
        origToTrans[raw.length] = sb.length
        transToOrigList.add(raw.length)

        val transformed = sb.toString()
        val transToOrig = transToOrigList.toIntArray()

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return origToTrans[offset.coerceIn(0, raw.length)]
            }

            override fun transformedToOriginal(offset: Int): Int {
                return transToOrig[offset.coerceIn(0, transformed.length)]
            }
        }

        return TransformedText(AnnotatedString(transformed), offsetMapping)
    }
}