package io.github.mishkun.compose.flexboxlayout

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MultiMeasureLayout
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.facebook.soloader.SoLoader
import io.github.mishkun.compose.flexboxlayout.ui.theme.ComposeFlexboxLayoutTheme
import io.github.mishkun.compose.flexboxlayout.yg.YogaLayout
import io.github.orioncraftmc.meditate.YogaConstants
import io.github.orioncraftmc.meditate.YogaMeasureOutput
import io.github.orioncraftmc.meditate.YogaNode
import io.github.orioncraftmc.meditate.YogaNodeFactory
import io.github.orioncraftmc.meditate.enums.YogaAlign
import io.github.orioncraftmc.meditate.enums.YogaDirection
import io.github.orioncraftmc.meditate.enums.YogaEdge
import io.github.orioncraftmc.meditate.enums.YogaFlexDirection
import io.github.orioncraftmc.meditate.enums.YogaJustify
import io.github.orioncraftmc.meditate.enums.YogaMeasureMode
import io.github.orioncraftmc.meditate.enums.YogaOverflow
import io.github.orioncraftmc.meditate.enums.YogaPositionType
import io.github.orioncraftmc.meditate.enums.YogaWrap
import kotlin.math.roundToInt


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        SoLoader.init(this, false)
        super.onCreate(savedInstanceState)
        setContent {
            ComposeFlexboxLayoutTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    Column {
                        YogaFlexBox()
                        GoogleFlexBox()
                        YogaDefaultPreview()
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Composable
fun FlexBox(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Layout(content) { measurables, constraints ->
        // 1. The measuring phase.
        val placeables = measurables.map { measurable ->
            measurable.measure(constraints)
        }

        // 2. The sizing phase.
        layout(constraints.maxWidth, constraints.maxHeight) {
            // 3. The placement phase.
            var yPosition = 0
            var xPosition = 0
            var rowHeight = 0

            placeables.forEach { placeable ->
                if (placeable.width < (constraints.maxWidth - xPosition)) {
                    placeable.placeRelative(xPosition, yPosition)
                    xPosition += (placeable.width)
                    rowHeight = maxOf(rowHeight, placeable.height)
                } else {
                    yPosition += (rowHeight)
                    xPosition = 0
                    rowHeight = 0
                    placeable.placeRelative(xPosition, yPosition)
                    xPosition += placeable.width
                }
            }
        }
    }
}


@Preview
@Composable
fun YogaFlexBox() {
    Box(modifier = Modifier.width(200.dp)) {
        AndroidView(factory = { ctx ->
            YogaLayout(ctx).apply {
                addView(TextView(ctx).apply {
                    setText("Hello Android!")
                    setBackgroundColor(ctx.getColor(R.color.teal_200))
                    layoutParams = ViewGroup.LayoutParams(WRAP_CONTENT, ctx.resources.displayMetrics.density.toInt() * 48)
                })
                addView(TextView(ctx).apply {
                    setText("Hello Android2!")
                    setBackgroundColor(ctx.getColor(R.color.teal_700))
                })
                addView(YogaLayout(ctx).apply {
                    yogaNode.flexDirection = YogaFlexDirection.COLUMN
                    yogaNode.wrap = YogaWrap.WRAP
                    addView(TextView(ctx).apply {
                        setText("Hello Android3!")
                        setBackgroundColor(ctx.getColor(R.color.purple_200))
                    })
                    addView(TextView(ctx).apply {
                        setText("Hello Android4!")
                        setBackgroundColor(ctx.getColor(R.color.purple_700))
                    })
                })
                yogaNode.flexDirection = YogaFlexDirection.ROW
                yogaNode.wrap = YogaWrap.WRAP

                val view: View = getChildAt(0)
                getYogaNodeForView(view).flexGrow = 1f
            }
        })
    }
}

@Preview
@Composable
fun GoogleFlexBox() {
//    AndroidView(modifier = Modifier.width(200.dp), factory = { ctx ->
//        FlexboxLayout(ctx).apply {
//            addView(TextView(ctx).apply {
//                setText("Hello Android!")
//                setBackgroundColor(ctx.getColor(R.color.teal_200))
//                layoutParams = FlexboxLayout.LayoutParams(WRAP_CONTENT, ctx.resources.displayMetrics.density.toInt() * 48)
//            })
//            addView(TextView(ctx).apply {
//                setText("Hello Android2!")
//                setBackgroundColor(ctx.getColor(R.color.teal_700))
//            })
//            addView(TextView(ctx).apply {
//                setText("Hello Android3!")
//                setBackgroundColor(ctx.getColor(R.color.purple_200))
//            })
//            addView(TextView(ctx).apply {
//                setText("Hello Android4!")
//                setBackgroundColor(ctx.getColor(R.color.purple_700))
//            })
//            setFlexDirection(FlexDirection.ROW)
//            flexWrap = FlexWrap.WRAP
//
//            val view: View = getChildAt(0)
//            val lp = view.layoutParams as FlexboxLayout.LayoutParams
//            lp.flexGrow = 1f
//            view.layoutParams = lp
//        }
//    })
}

val YogaNodeLocal: androidx.compose.runtime.ProvidableCompositionLocal<YogaNode?> = compositionLocalOf { null }

@Stable
data class FlexNodeContainer(
    val node: YogaNode
)

@Composable
fun Testlayout(num: Int, content: @Composable () -> Unit) {
    Layout(content = content) { measurables, constraints ->
        Log.d("TestLayout", "$num Before measure")
        val placeables = measurables.map { it.measure(constraints) }
        Log.d("TestLayout", "$num After measure")
        layout(constraints.maxWidth, constraints.maxHeight) {
            Log.d("TestLayout", "$num Before place")
            placeables.forEach { it.place(0, 0) }
            Log.d("TestLayout", "$num After place")
        }
    }
}

@Composable
//@Preview
fun TestLayoutPreview() {
    Testlayout(num = 0) {

        Text(text = "Hello0")
        Testlayout(num = 1) {

            Text(text = "Hello1")
            Testlayout(num = 2) {

                Text(text = "Hello2")
            }
        }
        Testlayout(num = 3) {
            Text(text = "Hello3")
        }
    }
}


enum class EdgeKind {
    POSITION, PADDING, MARGIN, BORDER
}

fun Edges.applyTo(node: YogaNode, kind: EdgeKind, density: Float) {
    when (kind) {
        EdgeKind.POSITION -> {
            when (leading.kind) {
                FlexSizeKind.PERCENT -> {
                    node.setPositionPercent(YogaEdge.START, leading.dpAmount(density)!!)
                }

                FlexSizeKind.CONSTANT -> {
                    node.setPosition(YogaEdge.START, leading.dpAmount(density)!!)
                }

                FlexSizeKind.AUTO -> {
                }

                FlexSizeKind.UNDEFINED -> {
                    node.setPosition(YogaEdge.START, YogaConstants.UNDEFINED)
                }
            }

            when (trailing.kind) {
                FlexSizeKind.PERCENT -> {
                    node.setPositionPercent(YogaEdge.END, trailing.dpAmount(density)!!)
                }

                FlexSizeKind.CONSTANT -> {
                    node.setPosition(YogaEdge.END, trailing.dpAmount(density)!!)
                }

                FlexSizeKind.AUTO -> {
                }

                FlexSizeKind.UNDEFINED -> {
                    node.setPosition(YogaEdge.END, YogaConstants.UNDEFINED)
                }
            }

            when (top.kind) {
                FlexSizeKind.PERCENT -> {
                    node.setPositionPercent(YogaEdge.TOP, top.dpAmount(density)!!)
                }

                FlexSizeKind.CONSTANT -> {
                    node.setPosition(YogaEdge.TOP, top.dpAmount(density)!!)
                }

                FlexSizeKind.AUTO -> {
                }

                FlexSizeKind.UNDEFINED -> {
                    node.setPosition(YogaEdge.TOP, YogaConstants.UNDEFINED)
                }
            }

            when (bottom.kind) {
                FlexSizeKind.PERCENT -> {
                    node.setPositionPercent(YogaEdge.BOTTOM, bottom.dpAmount(density)!!)
                }

                FlexSizeKind.CONSTANT -> {
                    node.setPosition(YogaEdge.BOTTOM, bottom.dpAmount(density)!!)
                }

                FlexSizeKind.AUTO -> {
                }

                FlexSizeKind.UNDEFINED -> {
                    node.setPosition(YogaEdge.BOTTOM, YogaConstants.UNDEFINED)
                }
            }
        }

        EdgeKind.PADDING -> {
            when (leading.kind) {
                FlexSizeKind.PERCENT -> {
                    node.setPaddingPercent(YogaEdge.START, leading.dpAmount(density)!!)
                }

                FlexSizeKind.CONSTANT -> {
                    node.setPadding(YogaEdge.START, leading.dpAmount(density)!!)
                }

                FlexSizeKind.AUTO -> {
                }

                FlexSizeKind.UNDEFINED -> {
                    node.setPadding(YogaEdge.START, YogaConstants.UNDEFINED)
                }
            }

            when (trailing.kind) {
                FlexSizeKind.PERCENT -> {
                    node.setPaddingPercent(YogaEdge.END, trailing.dpAmount(density)!!)
                }

                FlexSizeKind.CONSTANT -> {
                    node.setPadding(YogaEdge.END, trailing.dpAmount(density)!!)
                }

                FlexSizeKind.AUTO -> {
                }

                FlexSizeKind.UNDEFINED -> {
                    node.setPadding(YogaEdge.END, YogaConstants.UNDEFINED)
                }
            }

            when (top.kind) {
                FlexSizeKind.PERCENT -> {
                    node.setPaddingPercent(YogaEdge.TOP, top.dpAmount(density)!!)
                }

                FlexSizeKind.CONSTANT -> {
                    node.setPadding(YogaEdge.TOP, top.dpAmount(density)!!)
                }

                FlexSizeKind.AUTO -> {
                }

                FlexSizeKind.UNDEFINED -> {
                    node.setPadding(YogaEdge.TOP, YogaConstants.UNDEFINED)
                }
            }

            when (bottom.kind) {
                FlexSizeKind.PERCENT -> {
                    node.setPaddingPercent(YogaEdge.BOTTOM, bottom.dpAmount(density)!!)
                }

                FlexSizeKind.CONSTANT -> {
                    node.setPadding(YogaEdge.BOTTOM, bottom.dpAmount(density)!!)
                }

                FlexSizeKind.AUTO -> {
                }

                FlexSizeKind.UNDEFINED -> {
                    node.setPadding(YogaEdge.BOTTOM, YogaConstants.UNDEFINED)
                }
            }
        }

        EdgeKind.MARGIN -> {
            when (leading.kind) {
                FlexSizeKind.PERCENT -> {
                    node.setMarginPercent(YogaEdge.START, leading.dpAmount(density)!!)
                }

                FlexSizeKind.CONSTANT -> {
                    node.setMargin(YogaEdge.START, leading.dpAmount(density)!!)
                }

                FlexSizeKind.AUTO -> {
                    node.setMarginAuto(YogaEdge.START)
                }

                FlexSizeKind.UNDEFINED -> {
                    node.setMargin(YogaEdge.START, YogaConstants.UNDEFINED)
                }
            }

            when (trailing.kind) {
                FlexSizeKind.PERCENT -> {
                    node.setMarginPercent(YogaEdge.END, trailing.dpAmount(density)!!)
                }

                FlexSizeKind.CONSTANT -> {
                    node.setMargin(YogaEdge.END, trailing.dpAmount(density)!!)
                }

                FlexSizeKind.AUTO -> {
                    node.setMarginAuto(YogaEdge.END)
                }

                FlexSizeKind.UNDEFINED -> {
                    node.setMargin(YogaEdge.END, YogaConstants.UNDEFINED)
                }
            }

            when (top.kind) {
                FlexSizeKind.PERCENT -> {
                    node.setMarginPercent(YogaEdge.TOP, top.dpAmount(density)!!)
                }

                FlexSizeKind.CONSTANT -> {
                    node.setMargin(YogaEdge.TOP, top.dpAmount(density)!!)
                }

                FlexSizeKind.AUTO -> {
                    node.setMarginAuto(YogaEdge.TOP)
                }

                FlexSizeKind.UNDEFINED -> {
                    node.setMargin(YogaEdge.TOP, YogaConstants.UNDEFINED)
                }
            }

            when (bottom.kind) {
                FlexSizeKind.PERCENT -> {
                    node.setMarginPercent(YogaEdge.BOTTOM, bottom.dpAmount(density)!!)
                }

                FlexSizeKind.CONSTANT -> {
                    node.setMargin(YogaEdge.BOTTOM, bottom.dpAmount(density)!!)
                }

                FlexSizeKind.AUTO -> {
                    node.setMarginAuto(YogaEdge.BOTTOM)
                }

                FlexSizeKind.UNDEFINED -> {
                    node.setMargin(YogaEdge.BOTTOM, YogaConstants.UNDEFINED)
                }
            }
        }

        EdgeKind.BORDER -> {
            when (leading.kind) {
                FlexSizeKind.PERCENT -> {
                }

                FlexSizeKind.CONSTANT -> {
                    node.setBorder(YogaEdge.START, leading.dpAmount(density)!!)
                }

                FlexSizeKind.AUTO -> {
                }

                FlexSizeKind.UNDEFINED -> {
                }
            }

            when (trailing.kind) {
                FlexSizeKind.PERCENT -> {
                    node.setMarginPercent(YogaEdge.END, trailing.dpAmount(density)!!)
                }

                FlexSizeKind.CONSTANT -> {
                    node.setBorder(YogaEdge.END, trailing.dpAmount(density)!!)
                }

                FlexSizeKind.AUTO -> {
                    node.setMarginAuto(YogaEdge.END)
                }

                FlexSizeKind.UNDEFINED -> {
                    node.setMargin(YogaEdge.END, YogaConstants.UNDEFINED)
                }
            }

            when (top.kind) {
                FlexSizeKind.PERCENT -> {
                    node.setMarginPercent(YogaEdge.TOP, top.dpAmount(density)!!)
                }

                FlexSizeKind.CONSTANT -> {
                    node.setBorder(YogaEdge.TOP, top.dpAmount(density)!!)
                }

                FlexSizeKind.AUTO -> {
                    node.setMarginAuto(YogaEdge.TOP)
                }

                FlexSizeKind.UNDEFINED -> {
                    node.setMargin(YogaEdge.TOP, YogaConstants.UNDEFINED)
                }
            }

            when (bottom.kind) {
                FlexSizeKind.PERCENT -> {
                }

                FlexSizeKind.CONSTANT -> {
                    node.setBorder(YogaEdge.BOTTOM, bottom.dpAmount(density)!!)
                }

                FlexSizeKind.AUTO -> {
                }

                FlexSizeKind.UNDEFINED -> {
                }
            }
        }
    }
}

fun Modifier.conditional(condition: Boolean, modifier: Modifier.() -> Modifier): Modifier {
    return if (condition) {
        then(modifier(Modifier))
    } else {
        this
    }
}

enum class FlexSizeKind {
    PERCENT,
    CONSTANT,
    AUTO,
    UNDEFINED
}

data class Edges(
    val leading: FlexSizeDimension = undefined(),
    val trailing: FlexSizeDimension = undefined(),
    val top: FlexSizeDimension = undefined(),
    val bottom: FlexSizeDimension = undefined()
)

fun all(dimension: FlexSizeDimension): Edges =
    Edges(
        leading = dimension,
        trailing = dimension,
        top = dimension,
        bottom = dimension
    )

class FlexSizeDimension internal constructor(
    val kind: FlexSizeKind,
    val amount: Float?
) {
    internal fun dpAmount(density: Float): Float? =
        amount?.let {
            it * density
        }
}

fun percent(amount: Float) = FlexSizeDimension(kind = FlexSizeKind.PERCENT, amount = amount)
fun constant(amount: Float) = FlexSizeDimension(kind = FlexSizeKind.CONSTANT, amount = amount)

fun auto() = FlexSizeDimension(kind = FlexSizeKind.AUTO, amount = null)
fun undefined() = FlexSizeDimension(kind = FlexSizeKind.UNDEFINED, amount = null)

data class FlexSize(
    val width: FlexSizeDimension,
    val height: FlexSizeDimension
)

enum class AlignItems {
    STRETCH,
    CENTER,
    START,
    END
}

enum class AlignSelf {
    AUTO,
    STRETCH,
    CENTER,
    START,
    END
}

enum class AlignContent {
    AUTO,
    CENTER,
    START,
    END,
    STRETCH
}

enum class FlexDirection {
    COLUMN,
    ROW,
    COLUMN_REVERSE,
    ROW_REVERSE
}

enum class FlexWrap {
    WRAP,
    NO_WRAP
}

enum class JustifyContent {
    CENTER,
    START,
    END,
    SPACE_AROUND,
    SPACE_BETWEEN
}

enum class Direction {
    LTR,
    RTL,
    INHERIT
}

enum class Overflow {
    VISIBLE,
    HIDDEN,
    SCROLL
}

enum class PositionType {
    ABSOLUTE,
    RELATIVE
}

data class FlexStyle(
    val size: FlexSize = FlexSize(width = undefined(), height = undefined()),
    val minSize: FlexSize = FlexSize(width = undefined(), height = undefined()),
    val maxSize: FlexSize = FlexSize(width = undefined(), height = undefined()),


    // Specifies how flex-items are placed in the flex-container (defining the main-axis).
    // - Note: Applies to flex-container.
    val flexDirection: FlexDirection = FlexDirection.ROW,


    // Specifies whether flex items are forced into a single line
    // or can be wrapped onto multiple lines.
    // - Note: Applies to flex-container.
    val flexWrap: FlexWrap = FlexWrap.NO_WRAP,


    // Distributes space between and around flex-items along the main-axis.
    // - Note: Applies to flex-container.
    val justifyContent: JustifyContent = JustifyContent.START,

    // Distributes space between and around flex-items along the cross-axis.
    // This works like `justifyContent` but in the perpendicular direction.
    // - Note: Applies to flex-container.
    val alignItems: AlignItems = AlignItems.START,

    // Aligns a flex-container's lines when there is extra space on the cross-axis.
    // - Warning: This property has no effect on single line.
    // - Note: Applies to multi-line flex-container (no `FlexWrap.nowrap`).
    val alignContent: AlignContent = AlignContent.START,


    // Aligns self (flex-item) by overriding it's parent's (flex-container) `alignItems`.
    // - Note: Applies to flex-item.
    val alignSelf: AlignSelf = AlignSelf.AUTO,


    // Shorthand property specifying the ability of a flex-item
    // to alter its dimensions to fill available space.
    // - Note: Applies to flex-item.
    val flex: Float = Float.NaN,


    // Grow factor of a flex-item.
    // - Note: Applies to flex-item.
    val flexGrow: Float = Float.NaN,

    // Shrink factor of a flex-item.
    // - Note: Applies to flex-item.
    val flexShrink: Float = Float.NaN,

    // Initial main size of a flex item.
    // - Note: Applies to flex-item.
    val flexBasis: Float = Float.NaN,

    val direction: Direction = Direction.INHERIT,
    val overflow: Overflow = Overflow.VISIBLE,
    val positionType: PositionType = PositionType.RELATIVE,

    // CSS's (top, right, bottom, left) that works with `positionType = .absolute`.
    val position: Edges = Edges(),

    val margin: Edges = Edges(),
    val padding: Edges = Edges(),

    // facebook/yoga implementation that mostly works as same as `padding`.
    val border: Edges = Edges(),
)

fun FlexStyle.applyTo(node: YogaNode, density: Float) {
    when (size.width.kind) {
        FlexSizeKind.PERCENT -> size.width.amount?.let { node.setWidthPercent(it) }
        FlexSizeKind.CONSTANT -> size.width.dpAmount(density)?.let { node.setWidth(it) }
        FlexSizeKind.AUTO -> node.setWidthAuto()
        FlexSizeKind.UNDEFINED -> {}
    }

    when (size.height.kind) {
        FlexSizeKind.PERCENT -> size.height.amount?.let { node.setHeightPercent(it) }
        FlexSizeKind.CONSTANT -> size.height.dpAmount(density)?.let { node.setHeight(it) }
        FlexSizeKind.AUTO -> node.setHeightAuto()
        FlexSizeKind.UNDEFINED -> {}
    }

    when (minSize.width.kind) {
        FlexSizeKind.PERCENT -> minSize.width.amount?.let { node.setMinWidthPercent(it) }
        FlexSizeKind.CONSTANT -> minSize.width.dpAmount(density)?.let { node.setMinWidth(it) }
        FlexSizeKind.AUTO -> {}
        FlexSizeKind.UNDEFINED -> {}
    }

    when (minSize.height.kind) {
        FlexSizeKind.PERCENT -> minSize.height.amount?.let { node.setMinHeightPercent(it) }
        FlexSizeKind.CONSTANT -> minSize.height.dpAmount(density)?.let { node.setMinHeight(it) }
        FlexSizeKind.AUTO -> {}
        FlexSizeKind.UNDEFINED -> {}
    }

    when (maxSize.width.kind) {
        FlexSizeKind.PERCENT -> maxSize.width.amount?.let { node.setMaxWidthPercent(it) }
        FlexSizeKind.CONSTANT -> maxSize.width.dpAmount(density)?.let { node.setMaxWidth(it) }
        FlexSizeKind.AUTO -> {}
        FlexSizeKind.UNDEFINED -> {}
    }

    when (maxSize.height.kind) {
        FlexSizeKind.PERCENT -> maxSize.height.amount?.let { node.setMaxHeightPercent(it) }
        FlexSizeKind.CONSTANT -> maxSize.height.dpAmount(density)?.let { node.setMaxHeight(it) }
        FlexSizeKind.AUTO -> {}
        FlexSizeKind.UNDEFINED -> {}
    }

    when (flexDirection) {
        FlexDirection.COLUMN -> {
            node.flexDirection = YogaFlexDirection.COLUMN
        }

        FlexDirection.ROW -> {
            node.flexDirection = YogaFlexDirection.ROW
        }

        FlexDirection.COLUMN_REVERSE -> {
            node.flexDirection = YogaFlexDirection.COLUMN_REVERSE
        }

        FlexDirection.ROW_REVERSE -> {
            node.flexDirection = YogaFlexDirection.ROW_REVERSE
        }
    }

    when (flexWrap) {
        FlexWrap.WRAP -> {
            node.wrap = YogaWrap.WRAP
        }

        FlexWrap.NO_WRAP -> {
            node.wrap = YogaWrap.NO_WRAP
        }
    }

    when (justifyContent) {
        JustifyContent.CENTER -> {
            node.justifyContent = YogaJustify.CENTER
        }

        JustifyContent.START -> {
            node.justifyContent = YogaJustify.FLEX_START
        }

        JustifyContent.END -> {
            node.justifyContent = YogaJustify.FLEX_END
        }

        JustifyContent.SPACE_AROUND -> {
            node.justifyContent = YogaJustify.SPACE_AROUND
        }

        JustifyContent.SPACE_BETWEEN -> {
            node.justifyContent = YogaJustify.SPACE_BETWEEN
        }
    }

    when (alignItems) {
        AlignItems.STRETCH -> {
            node.alignItems = YogaAlign.STRETCH
        }

        AlignItems.CENTER -> {
            node.alignItems = YogaAlign.CENTER
        }

        AlignItems.START -> {
            node.alignItems = YogaAlign.FLEX_START
        }

        AlignItems.END -> {
            node.alignItems = YogaAlign.FLEX_END
        }
    }

    when (alignContent) {
        AlignContent.AUTO -> {
            node.alignContent = YogaAlign.AUTO
        }

        AlignContent.CENTER -> {
            node.alignContent = YogaAlign.CENTER
        }

        AlignContent.START -> {
            node.alignContent = YogaAlign.FLEX_START
        }

        AlignContent.END -> {
            node.alignContent = YogaAlign.FLEX_END
        }

        AlignContent.STRETCH -> {
            node.alignContent = YogaAlign.STRETCH
        }
    }

    when (alignSelf) {
        AlignSelf.AUTO -> {
            node.alignSelf = YogaAlign.AUTO
        }

        AlignSelf.STRETCH -> {
            node.alignSelf = YogaAlign.STRETCH
        }

        AlignSelf.CENTER -> {
            node.alignSelf = YogaAlign.CENTER
        }

        AlignSelf.START -> {
            node.alignSelf = YogaAlign.FLEX_START
        }

        AlignSelf.END -> {
            node.alignSelf = YogaAlign.FLEX_END
        }
    }

    node.flex = flex
    node.flexGrow = flexGrow
    node.flexShrink = flexShrink
    node.setFlexBasis(flexBasis)

    when (direction) {
        Direction.LTR -> node.setDirection(YogaDirection.LTR)
        Direction.RTL -> node.setDirection(YogaDirection.RTL)
        Direction.INHERIT -> node.setDirection(YogaDirection.INHERIT)
    }

    when (overflow) {
        Overflow.VISIBLE -> {
            node.overflow = YogaOverflow.VISIBLE
        }

        Overflow.HIDDEN -> {
            node.overflow = YogaOverflow.HIDDEN
        }

        Overflow.SCROLL -> {
            node.overflow = YogaOverflow.SCROLL
        }
    }

    when (positionType) {
        PositionType.ABSOLUTE -> {
            node.positionType = YogaPositionType.ABSOLUTE
        }

        PositionType.RELATIVE -> {
            node.positionType = YogaPositionType.RELATIVE
        }
    }

    position.applyTo(node, kind = EdgeKind.POSITION, density)
    padding.applyTo(node, kind = EdgeKind.PADDING, density)
    margin.applyTo(node, kind = EdgeKind.MARGIN, density)
    border.applyTo(node, kind = EdgeKind.BORDER, density)
}

enum class Axis {
    VERTICAL, HORIZONTAL
}

@Composable
fun Modifier.yoga(style: FlexStyle = FlexStyle()): Modifier {
    val parentNode = checkNotNull(YogaNodeLocal.current) { "Only use yoga modifier inside yoga layout blocks!" }
    val thisNode = remember {
        FlexNodeContainer(YogaNodeFactory.create())
    }
    val density = LocalDensity.current.density
    style.applyTo(node = thisNode.node, density)
    DisposableEffect(parentNode) {
        parentNode.addChildAt(thisNode.node, parentNode.childCount)
        onDispose {
            thisNode.node.removeSelf()
        }
    }
    return then(YogaModifier3(thisNode.node))
        .layout { measurable, constraints ->
            thisNode.node.dirty()
            val placeable = measurable.measure(constraints)
            layout(placeable.width, placeable.height) {
                placeable.place(0, 0)
            }
        }
}

fun YogaNode.removeSelf() {
    val owner = owner
    owner?.removeChildAt(owner.indexOf(this))
}


@Composable
fun YogaCompose(
    modifier: Modifier = Modifier,
    flexibleAxes: Set<Axis> = setOf(),
    style: FlexStyle = FlexStyle(),
    content: @Composable () -> Unit
) {
    val nodeContainer = remember {
        FlexNodeContainer(YogaNodeFactory.create())
    }
    val parentNode = YogaNodeLocal.current
    val density = LocalDensity.current.density
    style.applyTo(node = nodeContainer.node, density)
    if (parentNode != null) {
        DisposableEffect(parentNode, nodeContainer.node) {
            parentNode.addChildAt(nodeContainer.node, parentNode.childCount)
            onDispose {
                nodeContainer.node.removeSelf()
            }
        }
        CompositionLocalProvider(YogaNodeLocal provides nodeContainer.node) {
            content()
        }
    } else {
        CompositionLocalProvider(YogaNodeLocal provides nodeContainer.node) {
            MultiMeasureLayout(
                content = content,
                modifier = modifier.then(YogaModifier2(nodeContainer.node))
            ) { measurables: List<Measurable>, constraints: Constraints ->
                val nodes = measurables.mapIndexedNotNull { index, measurable ->
                    val nodeModifier = measurable.parentData as? YogaModifier3
                    val node = nodeModifier?.node ?: return@mapIndexedNotNull null
                    node.setMeasureFunction { _, suggestedWidth, widthMode, suggestedHeight, heightMode ->
                        val placeable = measurable.measure(
                            Constraints(
                                maxWidth = if (suggestedWidth.isNaN()) Constraints.Infinity
                                else suggestedWidth.roundToInt(),
                                maxHeight = if (suggestedHeight.isNaN()) Constraints.Infinity
                                else suggestedHeight.roundToInt()
                            )
                        )

                        fun sanitize(
                            constrainedSize: Float,
                            measuredSize: Float,
                            mode: YogaMeasureMode
                        ): Float {
                            return when (mode) {
                                YogaMeasureMode.UNDEFINED -> measuredSize
                                YogaMeasureMode.EXACTLY -> constrainedSize
                                YogaMeasureMode.AT_MOST -> measuredSize.coerceAtMost(constrainedSize)
                            }
                        }
                        node.data = placeable
                        Log.d(
                            "YogaCompose",
                            "Node: $node, $index, Heightmd: $heightMode, WidthMd: $widthMode, Constraints: $constraints, w: ${placeable.measuredWidth}, h: ${placeable.measuredHeight}"
                        )
                        return@setMeasureFunction YogaMeasureOutput.make(
                            sanitize(suggestedWidth, placeable.width.toFloat(), widthMode),
                            sanitize(suggestedHeight, placeable.height.toFloat(), heightMode)
                        )
                    }
                    node
                }

                Log.d("YogaCompose", "Constraints: $constraints")
                nodeContainer.node.calculateLayout(
                    flexibleAxes.contains(Axis.HORIZONTAL).let {
                        if (it) {
                            YogaConstants.UNDEFINED
                        } else {
                            if (constraints.hasBoundedWidth) {
                                constraints.maxWidth.toFloat()
                            } else {
                                YogaConstants.UNDEFINED
                            }
                        }
                    },
                    flexibleAxes.contains(Axis.VERTICAL).let {
                        if (it) {
                            YogaConstants.UNDEFINED
                        } else {
                            if (constraints.hasBoundedHeight) {
                                constraints.maxHeight.toFloat()
                            } else {
                                YogaConstants.UNDEFINED
                            }
                        }
                    }
                )

                nodeContainer.node.printLayout()

                nodes.forEach { node ->
                    val paddingStart = node.getLayoutPadding(YogaEdge.START)
                    val paddingEnd = node.getLayoutPadding(YogaEdge.END)
                    val paddingTop = node.getLayoutPadding(YogaEdge.TOP)
                    val paddingBottom = node.getLayoutPadding(YogaEdge.BOTTOM)

                    val measurable = node.data as? Measurable
                    if (measurable != null) Log.d("YogaCompose", "Measurable for node $node")
                    node.data = measurable?.measure(
                        Constraints(
                            maxWidth = node.layoutWidth.roundToInt() - paddingStart.toInt() - paddingEnd.toInt(),
                            minWidth = node.layoutWidth.roundToInt() - paddingStart.toInt() - paddingEnd.toInt(),
                            minHeight = node.layoutHeight.roundToInt() - paddingTop.toInt() - paddingBottom.toInt(),
                            maxHeight = node.layoutHeight.roundToInt() - paddingTop.toInt() - paddingBottom.toInt()
                        )
                    ) ?: node.data
                }

                layout(
                    nodeContainer.node.layoutWidth.roundToInt(),
                    nodeContainer.node.layoutHeight.roundToInt()
                ) {
                    nodes.forEachIndexed { index, node ->
                        val paddingStart = node.getLayoutPadding(YogaEdge.START)
                        val paddingTop = node.getLayoutPadding(YogaEdge.TOP)

                        val placeable = node.data as Placeable
                        Log.d("YogaCompose", "Node: $node, $index, Placeable: ${node.layoutX}, ${node.layoutY}")
                        placeable.place(
                            x = node.layoutXInAncestor(nodeContainer.node).roundToInt() + paddingStart.toInt(),
                            y = node.layoutYInAncestor(nodeContainer.node).roundToInt() + paddingTop.toInt()
                        )
                    }
                }
            }
        }

    }

}

fun YogaNode.printLayout() {
    Log.d("YogaNode", "Node: $this, x: $layoutX, y: $layoutY, w: $layoutWidth, h: $layoutHeight, childs: $childCount")
    for (i in 0 until childCount) {
        getChildAt(i).printLayout()
    }
}

fun YogaNode.layoutXInAncestor(ancestor: YogaNode): Float = when (val owner = owner) {
    ancestor -> layoutX
    null -> throw IllegalStateException("Given node is not this one's ancestor")
    else -> layoutX + owner.layoutXInAncestor(ancestor)
}

fun YogaNode.layoutYInAncestor(ancestor: YogaNode): Float = when (val owner = owner) {
    ancestor -> layoutY
    null -> throw IllegalStateException("Given node is not this one's ancestor")
    else -> layoutY + owner.layoutYInAncestor(ancestor)
}

class YogaModifier3(val node: YogaNode) : ParentDataModifier {
    override fun Density.modifyParentData(parentData: Any?): Any? {
        return this@YogaModifier3
    }
}


class YogaModifier2(val node: YogaNode) : ParentDataModifier {
    override fun Density.modifyParentData(parentData: Any?): Any? {
        return this@YogaModifier2
    }
}

class YogaModifier(val flexStyle: FlexStyle) : ParentDataModifier {
    override fun Density.modifyParentData(parentData: Any?): Any? {
        return this@YogaModifier
    }
}

@Preview(showBackground = true)
@Composable
fun YogaDefaultPreview() {
    YogaCompose(modifier = Modifier.width(200.dp), style = FlexStyle(flexWrap = FlexWrap.WRAP)) {
        AndroidView(modifier = Modifier.yoga(style = FlexStyle(flexGrow = 1f)), factory = { ctx ->
            TextView(ctx).apply {
                setText("Hello Android!")
                setBackgroundColor(ctx.getColor(R.color.teal_200))
                layoutParams = ViewGroup.LayoutParams(WRAP_CONTENT, ctx.resources.displayMetrics.density.toInt() * 48)
                alpha = 0.5f
            }
        })
        AndroidView(modifier = Modifier.yoga(), factory = { ctx ->
            TextView(ctx).apply {
                setText("Hello Android2!")
                setBackgroundColor(ctx.getColor(R.color.teal_700))
                alpha = 0.5f
            }
        })
        YogaCompose(style = FlexStyle(flexDirection = FlexDirection.COLUMN)) {
            AndroidView(modifier = Modifier.yoga(), factory = { ctx ->
                TextView(ctx).apply {
                    setText("Hello Android3!")
                    setBackgroundColor(ctx.getColor(R.color.purple_200))
                    alpha = 0.5f
                }
            })
            AndroidView(modifier = Modifier.yoga(), factory = { ctx ->
                TextView(ctx).apply {
                    setText("Hello Android4!")
                    setBackgroundColor(ctx.getColor(R.color.purple_700))
                    alpha = 0.5f
                }
            })

        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    Box(modifier = Modifier.width(200.dp)) {
        FlexBox() {
            Text(text = "Hello Android!", modifier = Modifier.height(48.dp))
            Text(text = "Hello Android2!")
            Text(text = "Hello Android3!")
            Text(text = "Hello Android4!")
        }
    }
}
