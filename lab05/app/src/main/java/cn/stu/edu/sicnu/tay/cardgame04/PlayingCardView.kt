package cn.stu.edu.sicnu.tay.cardgame04

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.view.View
import android.view.ViewOutlineProvider

/**
 * 按真实扑克牌比例绘制的牌：正面为白色牌面 + 点数和花色，背面为牌背图。
 */
class PlayingCardView(context: Context) : View(context) {

    companion object {
        private var backBitmap: Bitmap? = null
    }

    private var card: Card? = null
    private var faceUp = false
    private var matched = false

    private var cornerRadius = 0f

    private val clipPath = Path()
    private val facePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.parseColor("#DDE1E6")
    }
    private val backPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val rankPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.LEFT
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val suitPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.LEFT
    }
    private val centerSuitPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    init {
        isClickable = true
        elevation = 3f * context.resources.displayMetrics.density
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, cornerRadius)
            }
        }
        if (backBitmap == null) {
            backBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.card_back)
        }
    }

    fun bind(card: Card, faceUp: Boolean, matched: Boolean) {
        val wasFaceUp = this.faceUp
        this.card = card
        this.faceUp = faceUp
        this.matched = matched
        isEnabled = !matched
        isClickable = !matched

        if (matched) {
            if (alpha > 0f) {
                animate().alpha(0f).setDuration(220).start()
            }
        } else {
            animate().cancel()
            alpha = 1f
            if (faceUp && !wasFaceUp) {
                scaleX = 0.7f
                animate().scaleX(1f).setDuration(150).start()
            }
        }

        contentDescription = if (faceUp) card.toString() else context.getString(R.string.card_hidden)
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        cornerRadius = w * 0.08f
        borderPaint.strokeWidth = w * 0.014f
        invalidateOutline()
    }

    /**
     * 实验五：作为 RecyclerView 的条目视图时，宽度由 GridLayoutManager 分配，
     * 高度按扑克牌 5:7 的比例自动计算，保证每张牌大小一致、界面规整。
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val w = measuredWidth
        if (w > 0) {
            setMeasuredDimension(w, (w * 7f / 5f).toInt())
        }
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        if (w == 0f || h == 0f) return

        clipPath.reset()
        clipPath.addRoundRect(0f, 0f, w, h, cornerRadius, cornerRadius, Path.Direction.CW)

        canvas.save()
        canvas.clipPath(clipPath)
        if (faceUp && card != null) {
            drawFace(canvas, card!!, w, h)
        } else {
            drawBack(canvas, w, h)
        }
        canvas.restore()
    }

    private fun drawBack(canvas: Canvas, w: Float, h: Float) {
        val bitmap = backBitmap
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, null, android.graphics.RectF(0f, 0f, w, h), backPaint)
        } else {
            facePaint.color = Color.parseColor("#8C1D2F")
            canvas.drawRect(0f, 0f, w, h, facePaint)
        }
    }

    private fun drawFace(canvas: Canvas, card: Card, w: Float, h: Float) {
        facePaint.color = Color.WHITE
        canvas.drawRect(0f, 0f, w, h, facePaint)

        val suitColor = if (card.suit == "♥" || card.suit == "♦") {
            Color.parseColor("#C62828")
        } else {
            Color.parseColor("#263238")
        }

        rankPaint.color = suitColor
        rankPaint.textSize = h * 0.16f
        suitPaint.color = suitColor
        suitPaint.textSize = h * 0.115f

        val padX = w * 0.085f
        val padY = h * 0.065f
        drawCorner(canvas, card, padX, padY)

        // 右下角为旋转 180° 的镜像
        canvas.save()
        canvas.rotate(180f, w / 2f, h / 2f)
        drawCorner(canvas, card, padX, padY)
        canvas.restore()

        // 中央大花色
        centerSuitPaint.color = suitColor
        centerSuitPaint.textSize = h * 0.30f
        val centerBaseline = h / 2f - (centerSuitPaint.ascent() + centerSuitPaint.descent()) / 2f
        canvas.drawText(card.suit, w / 2f, centerBaseline, centerSuitPaint)

        // 内边框
        canvas.drawRoundRect(
            borderPaint.strokeWidth / 2f,
            borderPaint.strokeWidth / 2f,
            w - borderPaint.strokeWidth / 2f,
            h - borderPaint.strokeWidth / 2f,
            cornerRadius,
            cornerRadius,
            borderPaint
        )
    }

    private fun drawCorner(canvas: Canvas, card: Card, x: Float, y: Float) {
        val rankBaseline = y - rankPaint.fontMetrics.ascent
        canvas.drawText(card.rank, x, rankBaseline, rankPaint)
        canvas.drawText(card.suit, x, rankBaseline + suitPaint.textSize * 1.15f, suitPaint)
    }
}
