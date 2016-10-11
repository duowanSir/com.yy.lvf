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
import android.view.View;

/**
 * Created by slowergun on 2016/10/11 
 * view的自定义属性
 * view绘制流程
 * view的事件传递
 * canvas和paint的运用和matrix运用
 */
public class VideoBitRateRulerView extends View {
	private final static boolean VERBOSE = false;
	private final static String TAG = VideoBitRateRulerView.class.getSimpleName();
	private int mMinUnit;// 最小单位
	private int mMultiple;// 最大刻度和最小刻度之间的倍数;
	private int mMinValue;// 最大值
	private int mMaxValue;// 最小值

	private float mIndicatorHeight;
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

	public VideoBitRateRulerView(Context context) {
		super(context);
		init(context, null);
	}

	public VideoBitRateRulerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public VideoBitRateRulerView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		TypedArray a = null;
		int dp1 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, context.getResources().getDisplayMetrics());
		mIndicatorHeight = dp1 * 10;
		mMinUnitWidth = dp1 * 5;
		mMinUnitLineWidth = mMidUnitLineWidth = dp1;
		mMaxUnitLineWidth = (float) (dp1 * 1.5);
		mMinUnitLineHeight = dp1 * 2;
		mMidUnitLineHeight = mMinUnitLineHeight * 2;
		mMaxUnitLineHeight = mMidUnitLineHeight + mMinUnitLineHeight;
		mMinUnitLineColor = Color.GRAY;
		mMidUnitLineColor = Color.DKGRAY;
		mMaxUnitLineColor = Color.BLACK;

		if (attrs != null) {
			try {
				a = context.obtainStyledAttributes(attrs, R.styleable.VideoBitRateRulerView, 0, 0);
				mIndicatorHeight = a.getDimension(R.styleable.VideoBitRateRulerView_indicator_height, mIndicatorHeight);
				mMinUnitWidth = a.getDimension(R.styleable.VideoBitRateRulerView_min_unit_width, mMinUnitWidth);
				mMinUnitLineWidth = a.getDimension(R.styleable.VideoBitRateRulerView_min_unit_line_witdh, dp1);
				mMinUnitLineHeight = a.getDimension(R.styleable.VideoBitRateRulerView_min_unit_line_height, mMinUnitLineHeight);
				mMidUnitLineWidth = a.getDimension(R.styleable.VideoBitRateRulerView_mid_unit_line_witdh, dp1);
				mMidUnitLineHeight = a.getDimension(R.styleable.VideoBitRateRulerView_mid_unit_line_height, mMidUnitLineHeight);
				mMaxUnitLineWidth = a.getDimension(R.styleable.VideoBitRateRulerView_max_unit_line_witdh, mMaxUnitLineWidth);
				mMaxUnitLineHeight = a.getDimension(R.styleable.VideoBitRateRulerView_max_unit_line_height, mMaxUnitLineHeight);
				mMinUnitLineColor = a.getColor(R.styleable.VideoBitRateRulerView_min_unit_line_color, mMinUnitLineColor);
				mMidUnitLineColor = a.getColor(R.styleable.VideoBitRateRulerView_mid_unit_line_color, mMidUnitLineColor);
				mMaxUnitLineColor = a.getColor(R.styleable.VideoBitRateRulerView_max_unit_line_color, mMaxUnitLineColor);
			} finally {
				a.recycle();
			}
		}
	}

	private void initPaint() {
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(0, 0);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}

	private int measureHeight(int heightMeasureSpec) {
		int measureMode = View.MeasureSpec.getMode(heightMeasureSpec);
		int measureSize = View.MeasureSpec.getSize(heightMeasureSpec);
		return 0;
	}

	private void drawRulerScale(Canvas canvas) {
		// 依据大整刻度居中原则计算刻度起始坐标
		int leftValue = mMinValue - mMinValue % mMultiple;
		int rightValue = mMaxValue + mMultiple - mMaxValue % mMultiple;
		int midValue = (leftValue + rightValue) / 2;
		if (VERBOSE) {
			Log.d(TAG, "value: left = " + leftValue + ", mid = " + midValue + ", right = " + rightValue);
		}

		float xAxisStartCoordinate = getMeasuredWidth() / 2 - midValue / mMinUnit * mMinUnitWidth;

		int midMultiple = mMultiple / 2;
		boolean needMidUnit = false;
		if (midMultiple * 2 == mMultiple) {
			needMidUnit = true;
		}
		int unitCount = (rightValue - leftValue) / mMinUnit;
		int unitLineCount = unitCount + 1;

		float stx, sty, spx, spy;
		Paint p = null;
		for (int i = 0; i < unitLineCount; i++) {
			stx = spx = xAxisStartCoordinate + i * mMinUnitWidth;
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

	private void drawIndicator(Canvas canvas, boolean needRounding) {// 滑动不取整，滑动完毕取整。
		float stx, sty, spx, spy;
		spy = getMeasuredHeight() - mMaxUnitLineHeight;
		sty = spy - mIndicatorHeight;
		stx = spx = getMeasuredWidth() / 2;
		canvas.drawLine(stx, sty, spx, spy, mIndicatorPaint);
	}

	/**
	 * @param xAxisSlideDistance 终止 - 起始
	 */
	private void slideCalculate(Canvas canvas, float xAxisSlideDistance, boolean needRounding) {// 根据手势滑动距离，计算canvas偏移位置。
		if (needRounding) {
			boolean isPositive = false;
			if (xAxisSlideDistance >= 0) {
				isPositive = true;
			} else {
				isPositive = false;
			}
			xAxisSlideDistance = Math.abs(xAxisSlideDistance);
			float mul = xAxisSlideDistance / mMinUnitWidth;
			float left = mMinUnitWidth * mul;
			float right = left + mMinUnitWidth;
			if (left == xAxisSlideDistance) {
				left = right = xAxisSlideDistance;
			}
			float mid = (left + right) / 2;
			if (xAxisSlideDistance <= mid) {
				xAxisSlideDistance = left;
			} else {
				xAxisSlideDistance = right;
			}
			if (!isPositive) {
				xAxisSlideDistance = -xAxisSlideDistance;
			}
		}
		canvas.translate(xAxisSlideDistance, 0);// 平移canvas就不用重画尺子的刻度
	}

}
