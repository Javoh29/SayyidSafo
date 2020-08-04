package uz.mnsh.sayyidsafo.utils

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import uz.mnsh.sayyidsafo.R

class FrameLayoutHardware  @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): FrameLayout(context, attrs, defStyleAttr) {

    private val mask: MaskPath
    private val paint: Paint

    init {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)

        paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(context, android.R.color.white)
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        }

        val cornerRadius = context.resources.getDimension(R.dimen.corner_radius)
        val collapsedRadius = 0f
        mask = MaskPathRoundedCutout(cornerRadius, cornerRadius)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mask.rebuildPath(measuredWidth, measuredHeight)
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        val path = mask.getPath()
        canvas.drawPath(path, paint)
    }
}