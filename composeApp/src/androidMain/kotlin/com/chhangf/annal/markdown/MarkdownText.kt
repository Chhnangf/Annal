package com.chhangf.annal.markdown

import android.content.Context
import android.os.Build
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.widget.TextView
import androidx.annotation.FontRes
import androidx.annotation.IdRes
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import coil.ImageLoader
import io.noties.markwon.Markwon


@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    linkColor: Color = Color.Unspecified,
    truncateOnTextOverflow: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    isTextSelectable: Boolean = false,
    autoSizeConfig: AutoSizeConfig? = null,
    @FontRes fontResource: Int? = null,
    style: TextStyle = LocalTextStyle.current,
    @IdRes viewId: Int? = null,
    onClick: (() -> Unit)? = null,
    // this option will disable all clicks on links, inside the markdown text
    // it also enable the parent view to receive the click event
    disableLinkMovementMethod: Boolean = false,
    imageLoader: ImageLoader? = null,
    linkifyMask: Int = Linkify.EMAIL_ADDRESSES or Linkify.PHONE_NUMBERS or Linkify.WEB_URLS,
    onLinkClicked: ((String) -> Unit)? = null,
    onTextLayout: ((numLines: Int) -> Unit)? = null
) {
    val defaultColor: Color = LocalContentColor.current.copy(alpha = LocalContentAlpha.current)
    val context: Context = LocalContext.current
    val markdownRender: Markwon =
        remember { MarkdownRender.create(context, imageLoader, linkifyMask, onLinkClicked) }

    AndroidView(
        modifier = modifier,
        factory = { factoryContext ->

            val linkTextColor = linkColor.takeOrElse { style.color.takeOrElse { defaultColor } }

            TextView(factoryContext).apply {
                viewId?.let { id = viewId }
                onClick?.let { setOnClickListener { onClick() } }
                fontResource?.let { fontResourceId ->
                    typeface = ResourcesCompat.getFont(context, fontResourceId)
                }

                setMaxLines(maxLines)
                setLinkTextColor(linkTextColor.toArgb())

                setTextIsSelectable(isTextSelectable)

                movementMethod = LinkMovementMethod.getInstance()

                if (truncateOnTextOverflow) enableTextOverflows()

                autoSizeConfig?.let { config ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        setAutoSizeTextTypeUniformWithConfiguration(
                            config.autoSizeMinTextSize,
                            config.autoSizeMaxTextSize,
                            config.autoSizeStepGranularity,
                            config.unit
                        )
                    }
                }
            }
        },
        update = { textView ->
            with(textView) {
                applyTextColor(style.color.takeOrElse { defaultColor }.toArgb())
                applyFontSize(style)
                applyLineHeight(style)
                applyLineSpacing(style)
                applyTextDecoration(style)

                with(style) {
                    textAlign?.let { applyTextAlign(it) }
                    fontStyle?.let { applyFontStyle(it) }
                    fontWeight?.let { applyFontWeight(it) }
                }
            }
            markdownRender.setMarkdown(textView, markdown)
            if (disableLinkMovementMethod) {
                textView.movementMethod = null
            }
            if (onTextLayout != null) {
                textView.post {
                    onTextLayout(textView.lineCount)
                }
            }
            textView.maxLines = maxLines
        }
    )
}
