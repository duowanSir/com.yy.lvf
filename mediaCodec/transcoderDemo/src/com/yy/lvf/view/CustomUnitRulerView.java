package com.yy.lvf.view;

import com.yy.lvf.transcoder.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by slowergun on 2016/10/11 
 * view的自定义属性
 * view绘制流程
 * view的事件传递
 * canvas和paint的运用和matrix运用
 */
public class CustomUnitRulerView extends View {
	private final static boolean VERBOSE = true;
	private final static String TAG = CustomUnitRulerView.class.getSimpleName();
	private int mMinUnit;// 最小单位
	private int mMultiple;// 最大刻度和最小刻度之间的倍数;
	private int mMinValue;// 最大值
	private int mMaxValue;// 最小值

	private int mUnitLineCount;
	private float dp1;

	private float mXAxisStartCoordinate = Float.MIN_VALUE;
	private float mIndicatorHeight;
	private int mIndicatorColor;
	private float mMinUnitWidth;
	private float mMinUnitLineWidth;
	private float mMinUnitLineHeight;
	private float mMidUnitLineWidth;
	private float mMidUnitLineHeight;
	private float mMaxUnitLineWidth;
	private float mMaxUnitLineHeight;
	private int mMinUnitLineColor;
	private int mMidUnitLineColor;
	private int mMaxUnitLineColor;
	private Paint mMinUnitLinePaint;
	private Paint mMidUnitLinePaint;
	private Paint mMaxUnitLinePaint;
	private Paint mIndicatorPaint;

	private boolean mNeedRounding = true;
	private float mXAxisPreCoordinate = 0;
	private float mXAxisSlideDistance = 0;
	private Callback mCb;

	public interface Callback {
		void slide(int value);

		void slideCompleted(int value);
	}

	public CustomUnitRulerView(Context context) {
		super(context);
		init(context, null);
	}

	public CustomUnitRulerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public CustomUnitRulerView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		setClickable(true);
		TypedArray a = null;
		dp1 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, context.getResources().getDisplayMetrics());
		mMinUnit = 10;
		mMinValue = 0;
		mMaxValue = 1000;
		mMultiple = 10;

		mIndicatorHeight = dp1 * 10;
		mMinUnitWidth = dp1 * 5;
		mMinUnitLineWidth = mMidUnitLineWidth = dp1;
		mMaxUnitLineWidth = (float) (dp1 * 1.5);
		mMinUnitLineHeight = dp1 * 2;
		mMidUnitLineHeight = mMinUnitLineHeight * 2;
		mMaxUnitLineHeight = mMidUnitLineHeight + mMinUnitLineHeight;
		mIndicatorColor = Color.BLUE;
		mMinUnitLineColor = Color.GRAY;
		mMidUnitLineColor = Color.DKGRAY;
		mMaxUnitLineColor = Color.BLACK;

		if (attrs != null) {
			try {
				a = context.obtainStyledAttributes(attrs, R.styleable.CustomUnitRulerView, 0, 0);
				mMinUnit = a.getInt(R.styleable.CustomUnitRulerView_min_unit, mMinUnit);
				mMinValue = a.getInt(R.styleable.CustomUnitRulerView_min_value, mMinValue);
				mMaxValue = a.getInt(R.styleable.CustomUnitRulerView_max_value, mMaxValue);
				mMultiple = a.getInt(R.styleable.CustomUnitRulerView_multiple, mMultiple);

				mIndicatorHeight = a.getDimension(R.styleable.CustomUnitRulerView_indicator_height, mIndicatorHeight);
				mIndicatorColor = a.getColor(R.styleable.CustomUnitRulerView_indicator_color, mIndicatorColor);
				mMinUnitWidth = a.getDimension(R.styleable.CustomUnitRulerView_min_unit_width, mMinUnitWidth);
				mMinUnitLineWidth = a.getDimension(R.styleable.CustomUnitRulerView_min_unit_line_witdh, dp1);
				mMinUnitLineHeight = a.getDimension(R.styleable.CustomUnitRulerView_min_unit_line_height, mMinUnitLineHeight);
				mMidUnitLineWidth = a.getDimension(R.styleable.CustomUnitRulerView_mid_unit_line_witdh, dp1);
				mMidUnitLineHeight = a.getDimension(R.styleable.CustomUnitRulerView_mid_unit_line_height, mMidUnitLineHeight);
				mMaxUnitLineWidth = a.getDimension(R.styleable.CustomUnitRulerView_max_unit_line_witdh, mMaxUnitLineWidth);
				mMaxUnitLineHeight = a.getDimension(R.styleable.CustomUnitRulerView_max_unit_line_height, mMaxUnitLineHeight);
				mMinUnitLineColor = a.getColor(R.styleable.CustomUnitRulerView_min_unit_line_color, mMinUnitLineColor);
				mMidUnitLineColor = a.getColor(R.styleable.CustomUnitRulerView_mid_unit_line_color, mMidUnitLineColor);
				mMaxUnitLineColor = a.getColor(R.styleable.CustomUnitRulerView_max_unit_line_color, mMaxUnitLineColor);
			} finally {
				a.recycle();
			}
		}
		initPaint();
	}

	private void initPaint() {
		mIndicatorPaint = new Paint();
		mIndicatorPaint.setColor(mIndicatorColor);

		mMinUnitLinePaint = new Paint();
		mMinUnitLinePaint.setColor(mMinUnitLineColor);
		mMinUnitLinePaint.setStrokeWidth(mMinUnitLineWidth);

		mMidUnitLinePaint = new Paint();
		mMidUnitLinePaint.setColor(mMidUnitLineColor);
		mMidUnitLinePaint.setStrokeWidth(mMidUnitLineWidth);

		mMaxUnitLinePaint = new Paint();
		mMaxUnitLinePaint.setColor(mMaxUnitLineColor);
		mMaxUnitLinePaint.setStrokeWidth(mMaxUnitLineWidth);
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(View.MeasureSpec.getSize(widthMeasureSpec), measureHeight(heightMeasureSpec));
		// 依据大整刻度居中原则计算刻度起始坐标
		int maxUnit = mMultiple * mMinUnit;
		int leftValue = mMinValue - mMinValue % maxUnit;
		int i = mMaxValue % maxUnit;
		int rightValue;
		if (i == 0) {
			rightValue = mMaxValue;
		} else {
			rightValue = mMaxValue - i + mMinUnit;
		}
		int midValue = (leftValue + rightValue) / 2;

		if (VERBOSE) {
			Log.d(TAG, "value: left = " + leftValue + ", mid = " + midValue + ", right = " + rightValue);
		}
		mXAxisStartCoordinate = getMeasuredWidth() / 2 - midValue / mMinUnit * mMinUnitWidth;
		int unitCount = (rightValue - leftValue) / mMinUnit;
		mUnitLineCount = unitCount + 1;
		if (VERBOSE) {
			Log.d(TAG, "start x = " + mXAxisStartCoordinate + ", indicator x = " + getMeasuredWidth() / 2 + ", unit width = " + mMinUnitWidth);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		drawRulerScale(canvas);
		drawIndicator(canvas);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float width;
		int value;
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mNeedRounding = false;
			mXAxisPreCoordinate = event.getX();
			break;
		case MotionEvent.ACTION_MOVE:
			mXAxisSlideDistance = event.getX() - mXAxisPreCoordinate;
			mXAxisPreCoordinate = event.getX();
			width = slideCalculate();
			invalidate();
			if (mCb != null) {
				value = (int) (width / mMinUnitWidth * mMinUnit);
				if (value <= mMinValue) {
					value = mMinValue;
				} else if (value >= mMaxValue) {
					value = mMaxValue;
				}
				mCb.slide(value);
			}
			break;
		case MotionEvent.ACTION_UP:
			mNeedRounding = true;
			width = slideCalculate();
			invalidate();
			if (mCb != null) {
				value = (int) (width / mMinUnitWidth * mMinUnit);
				if (value <= mMinValue) {
					value = mMinValue;
				} else if (value >= mMaxValue) {
					value = mMaxValue;
				}
				mCb.slide(value);
			}
			break;
		default:
			break;
		}
		return super.onTouchEvent(event);
	}

	public void setCallback(Callback cb) {
		mCb = cb;
	}
	
	public void setBoundary(int min, int max) {
		mMinValue = min;
		mMaxValue = max;
		requestLayout();
		invalidate();
	}

	private int measureHeight(int heightMeasureSpec) {
		int measureMode = View.MeasureSpec.getMode(heightMeasureSpec);
		int measureSize = View.MeasureSpec.getSize(heightMeasureSpec);
		switch (measureMode) {
		case View.MeasureSpec.UNSPECIFIED:
		case View.MeasureSpec.EXACTLY:
		case View.MeasureSpec.AT_MOST:
		default:
			measureSize = (int) (getPaddingBottom() + getPaddingTop() + mIndicatorHeight + mMaxUnitLineHeight + dp1 * 10);
			break;
		}
		return measureSize;
	}

	private void drawRulerScale(Canvas canvas) {
		int midMultiple = mMultiple / 2;
		boolean needMidUnit = false;
		if (midMultiple * 2 == mMultiple) {
			needMidUnit = true;
		}

		float stx, sty, spx, spy;
		Paint p = null;
		for (int i = 0; i < mUnitLineCount; i++) {
			stx = spx = mXAxisStartCoordinate + i * mMinUnitWidth;
			spy = getMeasuredHeight();
			if (i % mMultiple == 0) {// 大刻度线
				sty = getMeasuredHeight() - mMaxUnitLineHeight;
				p = mMaxUnitLinePaint;
			} else if (needMidUnit && i % midMultiple == 0) {// 中间刻度线
				sty = getMeasuredHeight() - mMidUnitLineHeight;
				p = mMidUnitLinePaint;
			} else {// 小刻度线
				sty = getMeasuredHeight() - mMinUnitLineHeight;
				p = mMinUnitLinePaint;
			}
			canvas.drawLine(stx, sty, spx, spy, p);
		}
	}

	private void drawIndicator(Canvas canvas) {// 滑动不取整，滑动完毕取整。
		float stx, sty, spx, spy;
		spy = getMeasuredHeight() - mMaxUnitLineHeight;
		sty = spy - mIndicatorHeight;

		stx = spx = getMeasuredWidth() / 2;
		canvas.drawLine(stx, sty, spx, spy, mIndicatorPaint);
	}

	/**
	 * @param xAxisSlideDistance 终止 - 起始
	 */
	private float slideCalculate() {
		float width = getMeasuredWidth() / 2 - mXAxisStartCoordinate;// 指示器和起始点之间的距离
		if (mNeedRounding && mXAxisSlideDistance != 0) {
			boolean isPositive = false;
			if (VERBOSE) {
				Log.d(TAG, "start x = " + mXAxisStartCoordinate + ", indicator x = " + getMeasuredWidth() / 2);
			}
			if (width > 0) {
				isPositive = true;
			} else {
				isPositive = false;
			}
			width = Math.abs(width);
			int mul = (int) (width / mMinUnitWidth);
			float left = mMinUnitWidth * mul;
			float right = left + mMinUnitWidth;
			if (left == width) {
				right = left;
			}
			float mid = (left + right) / 2;
			if (width <= mid) {
				width = left;
			} else {
				width = right;
			}
			if (!isPositive) {
				width = -width;
			}
			mXAxisStartCoordinate = getMeasuredWidth() / 2 - width;
			if (VERBOSE) {
				Log.d(TAG, "start x = " + mXAxisStartCoordinate + ", indicator x = " + getMeasuredWidth() / 2 + ", left = " + left + ", right = " + right + ", width = " + width);
			}
		} else {
			mXAxisStartCoordinate += mXAxisSlideDistance;
		}
		return width;
	}

}
