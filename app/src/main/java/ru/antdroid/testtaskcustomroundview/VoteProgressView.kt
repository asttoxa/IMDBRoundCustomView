package ru.antdroid.testtaskcustomroundview

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.max


class VoteProgressView : View {

    private val mDpToPxScale = resources.displayMetrics.density
    private val mSpToPxScale = resources.displayMetrics.scaledDensity

    private var mRatingValue = DEFAULT_RATING_VALUE
    private var mCircleColor = DEFAULT_CIRCLE_COLOR // цвет прогресс бара
    private val mCircleStrokeWidth = DEFAULT_STROKE_WIDTH // толщина прогресс бара
    private var mWidthView = DEFAULT_WIDTH * mDpToPxScale
    private var mCircleDiameter = mWidthView - mCircleStrokeWidth * mDpToPxScale / 2// диаметр прогресс бара
    private var mTextType = DEFAULT_RATING_TYPE // текст типа
    private var mTextValue = DEFAULT_RATING_VALUE.toString()// текст значения
    private var mTextSizeType = DEFAULT_TYPE_TEXT_SIZE // размер текста типа


    private var mPointerPosition = 0F //Представляет прогресс на окружности в геометрических градусах.
    private var mProgress = mRatingValue // значение прогресса
    private var mMax = 10F // Максимальное значение прогресс бара
    private var mProgressDegrees = 0F

    private var xCoordinateTypeText : Float = 0F
    private var yCoordinateTypeText : Float  = 0F
    private var xCoordinateValueText : Float  = 0F
    private var yCoordinateValueText  : Float = 0F


    private val mCircleProgressPaint = Paint()
    private val mTextTypePaint = Paint()
    private val mCircleProgressPath = Path()
    private val mCircleRectF = RectF()
    private val mTextBoundRect = Rect()


    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs, defStyleAttr)
    }

    private fun init(attrs: AttributeSet?, defStyleAttr: Int) {
        val attrArray = context.obtainStyledAttributes(attrs, R.styleable.VoteProgressView, defStyleAttr, 0)
        initAttributes(attrArray)
        attrArray.recycle()
        initPaints()
    }

    private fun initAttributes(attrArray: TypedArray) {
        mProgress = attrArray.getFloat(R.styleable.VoteProgressView_ratingValue, DEFAULT_RATING_VALUE)
        fixTheValue()
        convertToStringFormat()

        mTextType = attrArray.getString(R.styleable.VoteProgressView_ratingType) ?: DEFAULT_RATING_TYPE
        mCircleColor = attrArray.getColor(R.styleable.VoteProgressView_circle_color, DEFAULT_CIRCLE_COLOR)

    }

    private fun fixTheValue() {
        if (mProgress >= mMax) mProgress = mMax - 0.001f
        if (mProgress < 0f) mProgress = 0f
    }

    private fun convertToStringFormat() {
        mTextValue = String.format("%.1f", myRound(mProgress))
    }

    private fun myRound(numb: Float): Float {
        val result = Math.round(numb * 10)
        return result.toFloat() / 10
    }

    fun setProgress(progress: Float) {
        if (mProgress != progress) {
            mProgress = progress
            fixTheValue()
            convertToStringFormat()
            recalculateAll()
            invalidate()
        }
    }

    fun setType(type: String) {
        mTextType = type
    }

    private fun initPaints() {
        mCircleProgressPaint.isAntiAlias = true
        mCircleProgressPaint.isDither = true
        mCircleProgressPaint.color = mCircleColor
        mCircleProgressPaint.strokeWidth = mCircleStrokeWidth * mDpToPxScale
        mCircleProgressPaint.style = Paint.Style.STROKE

        val tf = Typeface.createFromAsset(context.assets, "fonts/BebasNeue.ttf")
        mTextTypePaint.isAntiAlias = true
        mTextTypePaint.color = mCircleColor
        mTextTypePaint.textSize = mTextSizeType * mSpToPxScale
        mTextTypePaint.style = Paint.Style.FILL
        mTextTypePaint.letterSpacing = 0.04f
        mTextTypePaint.typeface = tf

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.drawPath(mCircleProgressPath, mCircleProgressPaint)

        canvas?.drawText(
            mTextType,
            xCoordinateTypeText,
            yCoordinateTypeText,
            mTextTypePaint
        )

        canvas?.drawText(
            mTextValue,
            xCoordinateValueText,
            yCoordinateValueText,
            mTextTypePaint
        )

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val measureWidthMode = MeasureSpec.getMode(widthMeasureSpec)
        val measureHeightMode = MeasureSpec.getMode(heightMeasureSpec)
        val measureWidth = MeasureSpec.getSize(widthMeasureSpec)
        val measureHeight = MeasureSpec.getSize(heightMeasureSpec)

        val preWidth = when (measureWidthMode) {
            MeasureSpec.EXACTLY ->
                max(measureWidth , mWidthView.toInt())
            MeasureSpec.AT_MOST ->
                 mWidthView.toInt()
            MeasureSpec.UNSPECIFIED ->
                mWidthView.toInt()
            else -> 0
        }

        val preHeight = when (measureHeightMode) {
            MeasureSpec.EXACTLY ->
                max(measureHeight, mWidthView.toInt())
            MeasureSpec.AT_MOST ->
                mWidthView.toInt()
            MeasureSpec.UNSPECIFIED ->
                mWidthView.toInt()
            else -> 0
        }

        mWidthView = (Math.min(preWidth, preHeight)).toFloat() -
                Math.min(paddingTop + paddingBottom, paddingStart + paddingEnd)

        if(mWidthView < DEFAULT_WIDTH * mDpToPxScale) mWidthView = DEFAULT_WIDTH * mDpToPxScale

        setMeasuredDimension(mWidthView.toInt(), mWidthView.toInt())

      //  setMeasuredDimension(Math.min(preWidth, preHeight), Math.min(preWidth, preHeight))
        mCircleDiameter = mWidthView - mCircleStrokeWidth * mDpToPxScale / 2

        mTextTypePaint.getTextBounds(mTextType, 0, mTextType.length, mTextBoundRect)
        val mTextWidthType = mTextTypePaint.measureText(mTextType)
        val mTextHeightType = mTextBoundRect.height()
        xCoordinateTypeText = mWidthView / 2 - (mTextWidthType / 2f)
        yCoordinateTypeText =  mWidthView / 2 + (mTextHeightType + 1 * mDpToPxScale)


        mTextTypePaint.getTextBounds(mTextValue, 0, mTextValue.length, mTextBoundRect)
        val mTextWidthValue = mTextTypePaint.measureText(mTextValue)
        //mTextHeight = mTextBoundRect.height()
        xCoordinateValueText = mWidthView / 2 - (mTextWidthValue / 2f)
        yCoordinateValueText = mWidthView / 2 - 3 * mDpToPxScale

        recalculateAll()

    }

    private fun recalculateAll() {
        calculatePointerAngle()
        calculateProgressDegrees()

        mCircleRectF.set(
            mCircleStrokeWidth * mDpToPxScale / 2,
            mCircleStrokeWidth * mDpToPxScale / 2,
            mCircleDiameter - (mCircleStrokeWidth * mDpToPxScale / 2),
            mCircleDiameter - (mCircleStrokeWidth * mDpToPxScale / 2)
        )
        mCircleProgressPath.reset()
        mCircleProgressPath.addArc(mCircleRectF, -90F, mProgressDegrees)
    }

    private fun calculatePointerAngle() {
        val progressPercent = mProgress / mMax
        mPointerPosition = progressPercent * 360F
        mPointerPosition %= 360f
    }

    private fun calculateProgressDegrees() {
        mProgressDegrees = mPointerPosition // Verified
        mProgressDegrees = if (mProgressDegrees < 0) 360f + mProgressDegrees else mProgressDegrees // Verified
    }


    companion object {
        private const val DEFAULT_RATING_VALUE = 5f
        private const val DEFAULT_RATING_TYPE = "IMDB"
        private const val DEFAULT_TYPE_TEXT_SIZE = 12f
        private const val DEFAULT_CIRCLE_COLOR = Color.WHITE
        private const val DEFAULT_STROKE_WIDTH = 2F
        private const val DEFAULT_WIDTH = 36F
    }

}