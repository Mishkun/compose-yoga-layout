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
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MultiMeasureLayout
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.facebook.soloader.SoLoader
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayout
import io.github.mishkun.compose.flexboxlayout.ui.theme.ComposeFlexboxLayoutTheme
import io.github.mishkun.compose.flexboxlayout.yg.YogaLayout
import io.github.orioncraftmc.meditate.YogaConstants
import io.github.orioncraftmc.meditate.YogaMeasureOutput
import io.github.orioncraftmc.meditate.YogaNode
import io.github.orioncraftmc.meditate.YogaNodeFactory
import io.github.orioncraftmc.meditate.enums.YogaEdge
import io.github.orioncraftmc.meditate.enums.YogaFlexDirection
import io.github.orioncraftmc.meditate.enums.YogaMeasureMode
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
    AndroidView(modifier = Modifier.width(200.dp), factory = { ctx ->
        FlexboxLayout(ctx).apply {
            addView(TextView(ctx).apply {
                setText("Hello Android!")
                setBackgroundColor(ctx.getColor(R.color.teal_200))
                layoutParams = FlexboxLayout.LayoutParams(WRAP_CONTENT, ctx.resources.displayMetrics.density.toInt() * 48)
            })
            addView(TextView(ctx).apply {
                setText("Hello Android2!")
                setBackgroundColor(ctx.getColor(R.color.teal_700))
            })
            addView(TextView(ctx).apply {
                setText("Hello Android3!")
                setBackgroundColor(ctx.getColor(R.color.purple_200))
            })
            addView(TextView(ctx).apply {
                setText("Hello Android4!")
                setBackgroundColor(ctx.getColor(R.color.purple_700))
            })
            setFlexDirection(FlexDirection.ROW)
            flexWrap = FlexWrap.WRAP

            val view: View = getChildAt(0)
            val lp = view.layoutParams as FlexboxLayout.LayoutParams
            lp.flexGrow = 1f
            view.layoutParams = lp
        }
    })
}

val YogaNodeLocal: androidx.compose.runtime.ProvidableCompositionLocal<YogaNode?> = compositionLocalOf { null }

@Composable
fun YogaNode(
    modifier: Modifier = Modifier,
    flexDirection: YogaFlexDirection = YogaFlexDirection.ROW,
    content: @Composable () -> Unit
) {
    val nodeContainer = remember {
        FlexNodeContainer(
            YogaNodeFactory.create().apply {
                this.flexDirection = flexDirection
            }
        )
    }
    Box(
        modifier
            .then(YogaModifier2(nodeContainer.node))
            .layout { measurable, constraints ->
//                nodeContainer.node.dirty()
                val placeable = measurable.measure(constraints)
                layout(placeable.width, placeable.height) {
                    placeable.place(0, 0)
                }
            }
    ) {
        content()
    }
}
data class FlexNodeContainer(
    val node: YogaNode
)
@Composable
fun YogaRoot(
    modifier: Modifier = Modifier,
    flexDirection: YogaFlexDirection = YogaFlexDirection.ROW,
    content: @Composable () -> Unit
) {
    val rootNodeContainer = remember { FlexNodeContainer(YogaNodeFactory.create().apply {
        this.flexDirection = flexDirection
        this.wrap = YogaWrap.WRAP
    }) }
    CompositionLocalProvider(YogaNodeLocal provides rootNodeContainer.node) {
        MultiMeasureLayout(content = content, modifier = modifier) { measurables: List<Measurable>, constraints: Constraints ->
            val allNodeContainerMeasurables = measurables.filter {
                it.parentData is YogaModifier2
            }

            val nodeContainers = allNodeContainerMeasurables.mapNotNull {
                it.parentData as? YogaModifier2
            }

            if (rootNodeContainer.node.childCount > nodeContainers.count()) {
                val numberOfChildrenToRemove = rootNodeContainer.node.childCount - nodeContainers.count()

                for (i in 1..numberOfChildrenToRemove) {
                    rootNodeContainer.node.removeChildAt(
                        rootNodeContainer.node.childCount - 1
                    )
                }
            }

            nodeContainers.forEachIndexed { index, layoutContainer ->
                layoutContainer.node.setMeasureFunction { _, suggestedWidth, widthMode, suggestedHeight, heightMode ->
                    val placeable = allNodeContainerMeasurables[index].measure(
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
                            YogaMeasureMode.AT_MOST -> kotlin.math.min(measuredSize, constrainedSize)
                        }
                    }
                    return@setMeasureFunction YogaMeasureOutput.make(
                        sanitize(suggestedWidth, placeable.width.toFloat(), widthMode),
                        sanitize(suggestedHeight, placeable.height.toFloat(), heightMode)
                    )
                }

                if (rootNodeContainer.node.childCount >= index + 1) {
                    if (rootNodeContainer.node.getChildAt(index) != layoutContainer.node) {
                        rootNodeContainer.node.removeChildAt(index)
                        rootNodeContainer.node.addChildAt(layoutContainer.node, index)
                    }
                } else {
                    rootNodeContainer.node.addChildAt(layoutContainer.node, index)
                }
            }

            rootNodeContainer.node.calculateLayout(
//                flexibleAxes.contains(Axis.HORIZONTAL).let {
//                    if (it) {
//                        YogaConstants.UNDEFINED
//                    } else {
                        if (constraints.hasBoundedWidth) {
                            constraints.maxWidth.toFloat()
                        } else {
                            YogaConstants.UNDEFINED
//                        }
//                    }
                },
//                flexibleAxes.contains(Axis.VERTICAL).let {
//                    if (it) {
//                        YogaConstants.UNDEFINED
//                    } else {
                        if (constraints.hasBoundedHeight) {
                            constraints.maxHeight.toFloat()
                        } else {
                            YogaConstants.UNDEFINED
//                        }
//                    }
                }
            )

            val placeables = allNodeContainerMeasurables.mapIndexed { index, measurable ->
                val node = nodeContainers[index].node

                val paddingStart = node.getLayoutPadding(YogaEdge.START)
                val paddingEnd = node.getLayoutPadding(YogaEdge.END)
                val paddingTop = node.getLayoutPadding(YogaEdge.TOP)
                val paddingBottom = node.getLayoutPadding(YogaEdge.BOTTOM)

                measurable.measure(
                    Constraints(
                        maxWidth = node.layoutWidth.roundToInt() - paddingStart.toInt() - paddingEnd.toInt(),
                        maxHeight = node.layoutHeight.roundToInt() - paddingTop.toInt() - paddingBottom.toInt()
                    )
                )
            }

            layout(
                rootNodeContainer.node.layoutWidth.roundToInt(),
                rootNodeContainer.node.layoutHeight.roundToInt()
            ) {
                placeables.forEachIndexed { index, placeable ->
                    val node = nodeContainers[index].node

                    val paddingStart = node.getLayoutPadding(YogaEdge.START)
                    val paddingTop = node.getLayoutPadding(YogaEdge.TOP)

                    placeable.place(
                        x = node.layoutX.roundToInt() + paddingStart.toInt(),
                        y = node.layoutY.roundToInt() + paddingTop.toInt()
                    )
                }
            }
        }
    }
}

@Composable
fun Testlayout(num: Int, content: @Composable () -> Unit) {
    Layout(content = content) { measurables, constraints ->
        Log.d("TestLayout", "$num Before measure")
        val placeables = measurables.map { it.measure(constraints) }
        Log.d("TestLayout", "$num After measure")
        layout(constraints.maxWidth, constraints.maxHeight) {
            Log.d("TestLayout", "$num Before place")
            placeables.forEach { it.place(0,0) }
            Log.d("TestLayout", "$num After place")
        }
    }
}

@Composable
@Preview
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

@Composable
fun YogaCompose(
    modifier: Modifier = Modifier,
    flexDirection: YogaFlexDirection = YogaFlexDirection.ROW,
    content: @Composable () -> Unit
) {
    val node = YogaNodeLocal.current
    if (node != null) {
        io.github.mishkun.compose.flexboxlayout.YogaNode(modifier, flexDirection, content)
    } else {
        YogaRoot(modifier, flexDirection, content)
    }
}

class YogaModifier2(val node: YogaNode) : ParentDataModifier {
    override fun Density.modifyParentData(parentData: Any?): Any? {
        return this@YogaModifier2
    }
}

class YogaModifier(val flexGrow: Int) : ParentDataModifier {
    override fun Density.modifyParentData(parentData: Any?): Any? {
        return this@YogaModifier
    }
}

@Preview(showBackground = true)
@Composable
fun YogaDefaultPreview() {
    YogaCompose(modifier = Modifier.width(200.dp)) {
        YogaCompose() {
            AndroidView(modifier = YogaModifier(flexGrow = 1), factory = { ctx ->
                TextView(ctx).apply {
                    setText("Hello Android!")
                    setBackgroundColor(ctx.getColor(R.color.teal_200))
                    layoutParams = ViewGroup.LayoutParams(WRAP_CONTENT, ctx.resources.displayMetrics.density.toInt() * 48)
                    alpha = 0.5f
                }
            })
        }
        YogaCompose() {
            AndroidView(factory = { ctx ->
                TextView(ctx).apply {
                    setText("Hello Android2!")
                    setBackgroundColor(ctx.getColor(R.color.teal_700))
                    alpha = 0.5f
                }
            })
        }
        YogaCompose(flexDirection = YogaFlexDirection.COLUMN) {
            YogaCompose() {
                AndroidView(factory = { ctx ->
                    TextView(ctx).apply {
                        setText("Hello Android3!")
                        setBackgroundColor(ctx.getColor(R.color.purple_200))
                        alpha = 0.5f
                    }
                })
            }
            YogaCompose() {
                AndroidView(factory = { ctx ->
                    TextView(ctx).apply {
                        setText("Hello Android4!")
                        setBackgroundColor(ctx.getColor(R.color.purple_700))
                        alpha = 0.5f
                    }
                })
            }

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
