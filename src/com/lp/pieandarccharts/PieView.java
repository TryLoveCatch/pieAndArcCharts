package com.lp.pieandarccharts;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

public class PieView extends View {
	private final static String MSG_CENTER_SUF = "%";
	private final static float MIN_PERCENT = 0.01f;
	
	private Paint mPaint;
	private TextPaint mTxtPaint;
	private TextPaint mTxtSufPaint;
	private Paint mTransparentPaint;
	private RectF mRect;
	private PointF mCenter;
	
	private int mDefaultSize = R.dimen.dp_82;
	private int mPadding = R.dimen.dp_10;
	private float mSpaceAngle = 6f;
	private float mSpaceMinAngle = mSpaceAngle / 2;
	private float mMinAngle = 1f;
	
	private int mCenterColor = Color.WHITE;
	private int mCenterTextSize = dip2px(R.dimen.dp_18);
	private int mCenterTextSufSize = dip2px(R.dimen.dp_10);
	private int mCenterTextColor = Color.parseColor("#999999");
	private int mCenterSufColor = Color.parseColor("#999999");
	private long mAnimateTime = 1500;
	private boolean mIsAnimEnable = true;
	
	private DecimalFormat mFormat = new DecimalFormat("#.##");
	private float mPercent;
	private List<Long> mArrData;
	private List<Integer> mArrColors;
	private float[] mArrAngles;
	private int mRadius;
	private float mPercentSecond;
	private List<Long> mArrDataSecond;
	private List<Integer> mArrColorsSecond;
	private boolean mIsShowAnimaed;
	
	private float animateValue = 1f;
	private ObjectAnimator mAnima;
	

	public PieView(Context context) {
		super(context);
		init();
	}

	public PieView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		initAttrs(context, attrs);
		init();
	}

	public PieView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initAttrs(context, attrs);
		init();
	}

	public PieView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initAttrs(context, attrs);
		init();
	}
	
	public void setColorAndData(List<Integer> pArrColors, List<Long> pArrData, float pPercent){
		if(mAnima.isRunning()){
			mArrColorsSecond = pArrColors;
			mArrDataSecond = pArrData;
			mPercentSecond = pPercent;
			return;
		}
		
		mArrColors = pArrColors;
		mArrData = pArrData;
		mPercent = pPercent;
		calcAnglse();
		
		if(!mIsAnimEnable) postInvalidate();
		
		if(!mIsShowAnimaed){
			startAnimate();
		}else{
			postInvalidate();
		}
	}
	
	public void startAnimate() {
		if(mIsShowAnimaed) return;
        if (android.os.Build.VERSION.SDK_INT < 11)
            return;
        if(mAnima.isRunning())
        	return;
        
        animateValue = 1f;
        mAnima.start();
        mIsShowAnimaed = true;
    }
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size = dip2px(mDefaultSize);
        setMeasuredDimension(
                Math.max(getSuggestedMinimumWidth(),
                        resolveSize(size,
                                widthMeasureSpec)),
                Math.max(getSuggestedMinimumHeight(),
                        resolveSize(size,
                                heightMeasureSpec)));
    }
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		canvas.drawColor(Color.TRANSPARENT);
		
		mRadius = getWidth() > getHeight() ? (getHeight() - 2*dip2px(mPadding)) / 2 : (getWidth() - 2*dip2px(mPadding)) / 2;
		mRect = new RectF();
		mRect.left = getWidth() / 2 - mRadius;
		mRect.top = getHeight() / 2 - mRadius;
		mRect.right = getWidth() / 2 + mRadius;
		mRect.bottom = getHeight() / 2 + mRadius;
		
		mCenter = new PointF(mRect.centerX(), mRect.centerY());
		
		
		drawArc(canvas);
		drawTransparentCicle(canvas);
		drawCenterText(canvas);
	}
	
	private void initAttrs(Context context, AttributeSet attrs){
		TypedArray tTypeArry = context.obtainStyledAttributes(attrs, R.styleable.PieView);
		mCenterColor = tTypeArry.getColor(R.styleable.PieView_centerBgColor, mCenterColor);
		mCenterTextColor = tTypeArry.getColor(R.styleable.PieView_centerTextColor, mCenterTextColor);
		mCenterSufColor = tTypeArry.getColor(R.styleable.PieView_centerSufColor, mCenterSufColor);
		mCenterTextSize = tTypeArry.getDimensionPixelSize(R.styleable.PieView_centerTextSize, mCenterTextSize);
		mCenterTextSufSize = tTypeArry.getDimensionPixelSize(R.styleable.PieView_centerTextSufSize, mCenterTextSufSize);
	}
	
	private void init(){
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setStyle(Style.FILL);
		mTxtPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		mTxtPaint.setColor(mCenterTextColor);
		mTxtPaint.setTextSize(mCenterTextSize);
		mTxtSufPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		mTxtSufPaint.setColor(mCenterSufColor);
		mTxtSufPaint.setTextSize(mCenterTextSufSize);
		mTransparentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTransparentPaint.setStrokeWidth(0);
		mTransparentPaint.setStyle(Style.FILL_AND_STROKE);
		mTransparentPaint.setColor(mCenterColor);
//		mTransparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		
		
		mAnima = ObjectAnimator.ofFloat(this, "animateValue", 0f, 1f);
		mAnima.setInterpolator(new AccelerateDecelerateInterpolator());
		mAnima.setDuration(mAnimateTime);
		mAnima.addListener(new Animator.AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {
				
			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {
				
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				if(mArrColorsSecond!=null){
					setColorAndData(mArrColorsSecond, mArrDataSecond, mPercentSecond);
				}
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				
			}
		});
	}
	
	private void calcAnglse(){
		mArrAngles = new float[mArrColors.size()];
		
		int tCountSpace = 0;
		for(int i=0;i<mArrAngles.length;i++){
			if(mArrData.get(i) > 0){
				tCountSpace++;
			}
		}
		
		float tAngleValid = 360f - 1.0f * tCountSpace * mSpaceAngle;
		float tExtraAngle = 0;
		List<Integer> tArrIndex = new ArrayList<Integer>();
		float tAngleSum = 0f;
		for(int i=0;i<mArrAngles.length;i++){
			mArrAngles[i] = 1.0f * mArrData.get(i) / mArrData.get(mArrAngles.length) * tAngleValid;
			
			if(mArrAngles[i]==0) continue;
			
			if(mArrAngles[i] < mMinAngle){
				tExtraAngle += (mMinAngle - mArrAngles[i]);
				mArrAngles[i] = mMinAngle;
			}else{
				tArrIndex.add(i);
				tAngleSum += mArrAngles[i];
			}
		}
		
		if(tExtraAngle > 0){
			float tSpacePerAngle = 1.0f * tExtraAngle / tCountSpace;
			if((tSpacePerAngle + mSpaceMinAngle) <= mSpaceAngle){
				mSpaceAngle -= tSpacePerAngle;
			}else{
				mSpaceAngle = mSpaceMinAngle;
				tExtraAngle -= (mSpaceAngle - mSpaceMinAngle) * tCountSpace;
				
				
				for(int i=0;i<mArrAngles.length;i++){
					if(tArrIndex.contains(Integer.valueOf(i))){
						mArrAngles[i] += 1.0f * mArrAngles[i] / tAngleSum * tExtraAngle;
					}
				}
			}
		}
	}
	
	private int dip2px(int pId){
		return getContext().getResources().getDimensionPixelSize(pId);
	}
	
	private void drawArc(Canvas pCanvas){
		float tStartAngle = 0;
		for(int i=0;i<mArrAngles.length;i++){
			float tNewAngle = mArrAngles[i];
			if(tNewAngle > 0.0f){
				mPaint.setColor(mArrColors.get(i));
				pCanvas.drawArc(mRect, tStartAngle* this.getAnimateValue(), tNewAngle * this.getAnimateValue(), true, mPaint);
				tStartAngle += (tNewAngle + mSpaceAngle)  * this.getAnimateValue();
			}
		}
	}
	private void drawTransparentCicle(Canvas pCanvas){
		pCanvas.drawCircle(mCenter.x, mCenter.y, mRadius * 3 / 4, mTransparentPaint);
	}
	private void drawCenterText(Canvas pCanvas){
		StringBuffer tTxt = new StringBuffer();
		if(mPercent > 0 && mPercent <= MIN_PERCENT){
			tTxt.append(MIN_PERCENT);
		}else{
			tTxt.append(mPercent > 10 ? Math.round(mPercent) : mFormat.format(mPercent));
		}
		float tTxtHeight = mTxtPaint.descent() - mTxtPaint.ascent();
        float tVerticalTextOffset = (tTxtHeight / 2) - mTxtPaint.descent();
        float tHorizontalTextOffset = (mTxtPaint.measureText(tTxt.toString()) + mTxtSufPaint.measureText(MSG_CENTER_SUF)) / 2;
		pCanvas.drawText(tTxt.toString(), mCenter.x - tHorizontalTextOffset, mCenter.y + tVerticalTextOffset, mTxtPaint);
		
		pCanvas.drawText(MSG_CENTER_SUF, mCenter.x - tHorizontalTextOffset + mTxtPaint.measureText(tTxt.toString())
						, mCenter.y + tVerticalTextOffset, mTxtSufPaint);
	}
	
	public float getAnimateValue() {
		return animateValue;
	}

	public void setAnimateValue(float animateValue) {
//		Log.e("...", animateValue + "");
		this.animateValue = animateValue;
		postInvalidate();
	}

}
