package io.github.mishkun.compose.flexboxlayout

import android.os.Bundle
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.SubcomposeLayoutState
import androidx.compose.ui.layout.SubcomposeSlotReusePolicy
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.facebook.soloader.SoLoader
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayout
import io.github.mishkun.compose.flexboxlayout.ui.theme.ComposeFlexboxLayoutTheme
import io.github.mishkun.compose.flexboxlayout.yg.YogaLayout
import io.github.orioncraftmc.meditate.YogaMeasureOutput
import io.github.orioncraftmc.meditate.YogaNodeFactory
import io.github.orioncraftmc.meditate.enums.YogaFlexDirection
import io.github.orioncraftmc.meditate.enums.YogaMeasureMode
import io.github.orioncraftmc.meditate.enums.YogaWrap
import kotlin.random.Random


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
                addView(TextView(ctx).apply {
                    setText("Hello Android3!")
                    setBackgroundColor(ctx.getColor(R.color.purple_200))
                })
                addView(TextView(ctx).apply {
                    setText("Hello Android4!")
                    setBackgroundColor(ctx.getColor(R.color.purple_700))
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

@Composable
fun YogaCompose(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val subcomposeLayoutState = remember { SubcomposeLayoutState(SubcomposeSlotReusePolicy(1)) }
    SubcomposeLayout(state = subcomposeLayoutState, modifier = modifier)  { constraints ->
        val thisYogaNode = YogaNodeFactory.create()
        var measurables = subcompose(Random.nextInt()) { content() }
        val childNodes = measurables.mapIndexed { idx, measurable ->
            YogaNodeFactory.create().apply {
                setMeasureFunction { node, width, widthMode, height, heightMode ->
                    val constraint = Constraints(
                        minHeight = when (heightMode) {
                            null,
                            YogaMeasureMode.UNDEFINED,
                            YogaMeasureMode.AT_MOST -> 0
                            YogaMeasureMode.EXACTLY -> height.toInt()
                        },
                        maxHeight = when (heightMode) {
                            null,
                            YogaMeasureMode.UNDEFINED -> Constraints.Infinity
                            YogaMeasureMode.EXACTLY,
                            YogaMeasureMode.AT_MOST -> height.toInt()
                        },
                        minWidth = when (widthMode) {
                            null,
                            YogaMeasureMode.UNDEFINED,
                            YogaMeasureMode.AT_MOST -> 0
                            YogaMeasureMode.EXACTLY -> width.toInt()
                        },
                        maxWidth = when (widthMode) {
                            null,
                            YogaMeasureMode.UNDEFINED -> Constraints.Infinity
                            YogaMeasureMode.EXACTLY,
                            YogaMeasureMode.AT_MOST -> width.toInt()
                        }
                    )
                    val placeable = measurables[idx].measure(constraint)
                    node.data = placeable
                    measurables = subcompose(Random.nextInt()) { content() }
                    YogaMeasureOutput.make(placeable.measuredWidth, placeable.measuredHeight)
                }
            }
        }
        thisYogaNode.flexDirection = YogaFlexDirection.ROW
        thisYogaNode.wrap = YogaWrap.WRAP
        childNodes.forEachIndexed { idx, child -> thisYogaNode.addChildAt(child, idx) }
        thisYogaNode.setMaxHeight(constraints.maxHeight.toFloat())
        thisYogaNode.setMaxWidth(constraints.maxWidth.toFloat())
        thisYogaNode.setMinWidth(constraints.minWidth.toFloat())
        thisYogaNode.setMinHeight(constraints.minHeight.toFloat())
        thisYogaNode.calculateLayout(constraints.maxWidth.toFloat(), constraints.maxHeight.toFloat())
        layout(thisYogaNode.layoutWidth.toInt(), thisYogaNode.layoutHeight.toInt()) {
            childNodes.forEach { child ->
                val placeable = child.data as Placeable
                placeable.place(child.layoutX.toInt(), child.layoutY.toInt())
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun YogaDefaultPreview() {
        YogaCompose(modifier = Modifier
            .width(250.dp)) {
            Text(text = "Hello Android!", modifier = Modifier.height(48.dp))
            Text(text = "Hello Android2!")
            Text(text = "Hello Android3!")
            Text(text = "Hello Android4!")
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
