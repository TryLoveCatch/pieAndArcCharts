package com.lp.pieandarccharts;

import java.text.DecimalFormat;
import java.util.List;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.os.Handler;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

public class ArcView extends View {
	private final static String MSG_CENTER_SUF = "%";
	private final static float MIN_PERCENT = 0.01f;
	private final static float DEFAULT_ANGLE = 270;
	private final static float START_ANGLE = 135;
	
	
	private Paint mPaint;
	private TextPaint mTxtPaint;
	private TextPaint mTxtSufPaint;
	private Paint mBgArcPaint;
	private RectF mRect;
	private PointF mCenter;
	
	private int mDefaultSize = R.dimen.dp_82;
	private float mMinAngle = 1f;
	
	private int mStorkWidth = dip2px(R.dimen.dp_8);
	private int mCenterTextSize = dip2px(R.dimen.dp_25);
	private int mCenterTextSufSize = dip2px(R.dimen.dp_10);
	private int mCenterTextColor = Color.parseColor("#999999");
	private int mBgArcColor = R.color.bg_black;
	
	private long mAnimateTimeProgress = 1500;
	private long mAnimateTimeSwitch = 600;
	private long mDelayTimeSwitch = 2000;
	
	private DecimalFormat mFormat = new DecimalFormat("#.##");
	private float mPercent;
	private List<Long> mArrData;
	private int[] mColors;
	private float mAngle;
	private int mRadius;
	private float mPercentSecond;
	private List<Long> mArrDataSecond;
	private int[] mArrColorsSecond;
	private boolean mIsShowAnimaed;
	
	private float mAnimValueProgress = 1f;
	private ObjectAnimator mAnimaProgress;
	private float mAnimValueSwitch = 1f;
	private ObjectAnimator mAnimaSwitch;
	private Bitmap mBmpIcon;
	private boolean mIsShowBmp;
	
	private Camera mCamera;
	private float mFromeDegress = 0;
	private float mToDegress = 0;
	private Paint mBmpPaint;
	
	private Handler mHandler = new Handler();
	private boolean mIsStopped;

	public ArcView(Context context) {
		super(context);
		init();
	}

	public ArcView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		initAttrs(context, attrs);
		init();
	}

	public ArcView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initAttrs(context, attrs);
		init();
	}

	public ArcView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initAttrs(context, attrs);
		init();
	}
	
	public void setColorAndData(int[] pColors, List<Long> pArrData, int pResId){
		if(mAnimaProgress.isRunning()){
			mArrColorsSecond = pColors;
			mArrDataSecond = pArrData;
			if(mArrDataSecond.get(1) <= 0){
				mPercentSecond = 0;
			}else{
				mPercentSecond = 1.0f * mArrDataSecond.get(0) / mArrDataSecond.get(1) * 100;
			}
			return;
		}
		
		mColors = pColors;
		mArrData = pArrData;
		if(mArrData.get(1) <= 0){
			mPercent = 0;
		}else{
			mPercent = 1.0f * mArrData.get(0) / mArrData.get(1) * 100;
		}
		if(mBmpIcon==null)
			mBmpIcon = BitmapFactory.decodeResource(getResources(), pResId);
		calcAnglse();
		startAnimate();
	}
	
	public void startAnimate() {
        if (android.os.Build.VERSION.SDK_INT < 11)
            return;
        if(mIsStopped){
        	mIsStopped = false;
        	startAnimSwitch();
        }
        if(mIsShowAnimaed) {
//        	startAnimSwitch();
			return;
		}
        if(mAnimaProgress.isRunning())
        	return;
        
        mAnimValueProgress = 1f;
        mAnimaProgress.start();
        mIsShowAnimaed = true;
    }
	
	public void stopAnimate(){
		mIsStopped = true;
		mAnimaProgress.cancel();
		mAnimaSwitch.cancel();
		mHandler.removeCallbacks(mThreadAnim);
		setAnimValueSwitch(1f);
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
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
//		mHandler.removeCallbacks(mThreadAnim);
//		if(mBmpIcon!=null){
//			mBmpIcon.recycle();
//			mBmpIcon = null;
//		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		canvas.drawColor(Color.TRANSPARENT);
		
		mRadius = getWidth() > getHeight() ? (getHeight() - 2*mStorkWidth) / 2 : (getWidth() - 2*mStorkWidth) / 2;
		mRect = new RectF();
		mRect.left = getWidth() / 2 - mRadius - mStorkWidth/2;
		mRect.top = getHeight() / 2 - mRadius - mStorkWidth/2;
		mRect.right = getWidth() / 2 + mRadius + mStorkWidth/2;
		mRect.bottom = getHeight() / 2 + mRadius + mStorkWidth/2;
		
		mCenter = new PointF(mRect.centerX(), mRect.centerY());
		
		
		drawBgArc(canvas);
		drawArc(canvas);
		canvas.save();
		canvas.clipRect(new RectF(mCenter.x - mRadius + mStorkWidth, mCenter.y - mBmpIcon.getHeight()/2
				, mCenter.x + mRadius - mStorkWidth, mCenter.y + mBmpIcon.getHeight()/2));
		
//		drawCenterText(canvas);
//		drawIcon(canvas);
		
		Matrix tM = new Matrix();
		mCamera.save();
		mCamera.rotateY(mFromeDegress + (mToDegress - mFromeDegress) * mAnimValueSwitch);
		mCamera.getMatrix(tM);
		mCamera.restore();
		tM.preTranslate(-mCenter.x, -mCenter.y);
		tM.postTranslate(mCenter.x, mCenter.y);
		canvas.concat(tM);
		draw3DRotateBmp(canvas);
		draw3DRotateTxt(canvas);
		
		canvas.restore();
	}
	
	
	private void initAttrs(Context context, AttributeSet attrs){
		TypedArray tTypeArry = context.obtainStyledAttributes(attrs, R.styleable.ArcView);
		mCenterTextColor = tTypeArry.getColor(R.styleable.ArcView_centerTextColor, mCenterTextColor);
		mCenterTextSize = tTypeArry.getDimensionPixelSize(R.styleable.ArcView_centerTextSize, mCenterTextSize);
		mCenterTextSufSize = tTypeArry.getDimensionPixelSize(R.styleable.ArcView_centerTextSufSize, mCenterTextSufSize);
		mBgArcColor = tTypeArry.getColor(R.styleable.ArcView_arcBgColor, mBgArcColor);
		mStorkWidth = tTypeArry.getDimensionPixelSize(R.styleable.ArcView_arcStorkWidth, mStorkWidth);
	}
	
	private void init(){
		mIsShowBmp = false;
		mIsStopped = false;
		mCamera = new Camera();
		
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setStyle(Style.STROKE);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setStrokeWidth(mStorkWidth);
		mTxtPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		mTxtPaint.setColor(mCenterTextColor);
		mTxtPaint.setTextSize(mCenterTextSize);
		mTxtSufPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		mTxtSufPaint.setColor(mCenterTextColor);
		mTxtSufPaint.setTextSize(mCenterTextSufSize);
		mBgArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mBgArcPaint.setStrokeWidth(mStorkWidth);
		mBgArcPaint.setStyle(Style.STROKE);
		mBgArcPaint.setStrokeCap(Paint.Cap.ROUND);
		mBgArcPaint.setColor(mBgArcColor);
		mBmpPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mBmpPaint.setStyle(Style.FILL);
		mBmpPaint.setColor(Color.WHITE);
		mBmpPaint.setAlpha(0);
		
		
		mAnimaProgress = ObjectAnimator.ofFloat(this, "animateValue", 0f, 1f);
		mAnimaProgress.setInterpolator(new AccelerateDecelerateInterpolator());
		mAnimaProgress.setDuration(mAnimateTimeProgress);
		mAnimaProgress.addListener(new Animator.AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {
				startAnimSwitch();
			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {
				
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				if(mArrColorsSecond!=null){
					mPercent = mPercentSecond;
					setColorAndData(mArrColorsSecond, mArrDataSecond, -1);
				}
				
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				
			}
		});
		mAnimaSwitch = ObjectAnimator.ofFloat(this, "animValueSwitch", 0f, 1f);
//		mAnimaSwitch.setRepeatCount(ValueAnimator.INFINITE);
//		mAnimaSwitch.setInterpolator(new LinearInterpolator());
		mAnimaSwitch.setInterpolator(new AccelerateDecelerateInterpolator());
		mAnimaSwitch.setDuration(mAnimateTimeSwitch);
		mAnimaSwitch.addListener(new Animator.AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {
				if(mIsShowBmp){
					mFromeDegress = 180;
					mToDegress = 360;
				}else{
					mFromeDegress = 0;
					mToDegress = 180;
				}
				mIsShowBmp = !mIsShowBmp;
			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
//				mIsShowBmp = !mIsShowBmp;
//				mAnimValueSwitch = 0f;
				if(!mIsStopped){
					mHandler.postDelayed(mThreadAnim, mDelayTimeSwitch);
				}
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				
			}
		});
	}
	
	private void calcAnglse(){
		mAngle =  1.0f * mArrData.get(0) / mArrData.get(1) * DEFAULT_ANGLE;
		
		if(mAngle < mMinAngle){
			mAngle = mMinAngle;
		}
	}
	
	private int dip2px(int pId){
		return getContext().getResources().getDimensionPixelSize(pId);
	}
	
	private void drawArc(Canvas pCanvas){
//		mPaint.setColor(mColor);
		if(mPaint.getShader()==null){
			int[] colors = {getResources().getColor(mColors[0]), getResources().getColor(mColors[1])}; 
			float[] positions = {0f, mAngle/360};
			SweepGradient gradient = new SweepGradient(mCenter.x, mCenter.y, colors , positions);
			Matrix tM = new Matrix();
			tM.setRotate(START_ANGLE - 10, mCenter.x, mCenter.y);
			gradient.setLocalMatrix(tM);
			mPaint.setShader(gradient);
		}
		pCanvas.drawArc(mRect, START_ANGLE, mAngle * mAnimValueProgress, false, mPaint);
	}
	private void drawBgArc(Canvas pCanvas){
		pCanvas.drawArc(mRect, START_ANGLE, DEFAULT_ANGLE, false, mBgArcPaint);
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
        if(mIsShowBmp){
        	float tStart = mCenter.y + mBmpIcon.getHeight()/2 + tTxtHeight;
        	float tEnd = mCenter.y + tVerticalTextOffset;
        	pCanvas.drawText(tTxt.toString(), mCenter.x - tHorizontalTextOffset
        			, tStart - (tStart - tEnd) * mAnimValueSwitch
        			, mTxtPaint);
        	pCanvas.drawText(MSG_CENTER_SUF, mCenter.x - tHorizontalTextOffset + mTxtPaint.measureText(tTxt.toString())
		        	, tStart - (tStart - tEnd) * mAnimValueSwitch
		        	, mTxtSufPaint);
        }else{
        	float tStart = mCenter.y + tVerticalTextOffset;
        	float tEnd = (mCenter.y - mBmpIcon.getHeight()/2) - tTxtHeight ;
        	pCanvas.drawText(tTxt.toString(), mCenter.x - tHorizontalTextOffset
        			, tStart - (tStart - tEnd) * mAnimValueSwitch
        			, mTxtPaint);
        	pCanvas.drawText(MSG_CENTER_SUF, mCenter.x - tHorizontalTextOffset + mTxtPaint.measureText(tTxt.toString())
		        	, tStart - (tStart - tEnd) * mAnimValueSwitch
		        	, mTxtSufPaint);
        }
	}
	
	private void drawIcon(Canvas pCanvas){
		RectF tRect = new RectF();
		tRect.left = mCenter.x - mBmpIcon.getWidth()/2;
		if(mIsShowBmp){
			tRect.top = (mCenter.y - mBmpIcon.getHeight()/2) - mAnimValueSwitch * mBmpIcon.getHeight();
		}else{
			tRect.top = (mCenter.y + mBmpIcon.getHeight()/2) - mAnimValueSwitch * mBmpIcon.getHeight();
		}
		tRect.right = tRect.left + mBmpIcon.getWidth();
		tRect.bottom = tRect.top + mBmpIcon.getHeight();
		pCanvas.drawBitmap(mBmpIcon, null, tRect, null);
	}
	
	public float getAnimateValue() {
		return mAnimValueProgress;
	}

	public void setAnimateValue(float animateValue) {
//		Log.e("...", animateValue + "");
		this.mAnimValueProgress = animateValue;
		postInvalidate();
	}
	
	public void setAnimValueSwitch(float animateValue) {
//		Log.e("...", animateValue + "");
		this.mAnimValueSwitch = animateValue;
		postInvalidate();
	}
	
	private Runnable mThreadAnim = new Runnable() {
		
		@Override
		public void run() {
			mAnimaSwitch.start();
			
//			mHandler.postDelayed(mThreadAnim, mDelayTimeSwitch);
		}
	};
	
	private void draw3DRotateBmp(Canvas pCanvas){
		RectF tRect = new RectF();
		tRect.left = mCenter.x - mBmpIcon.getWidth()/2;
		tRect.top = mCenter.y - mBmpIcon.getHeight()/2;
		tRect.right = tRect.left + mBmpIcon.getWidth();
		tRect.bottom = tRect.top + mBmpIcon.getHeight();
		
		pCanvas.save();
		Matrix tM = new Matrix();
		mCamera.save();
		mCamera.rotateY(180);
		mCamera.getMatrix(tM);
		mCamera.restore();
		tM.preTranslate(-tRect.centerX(), -tRect.centerY());
		tM.postTranslate(tRect.centerX(), tRect.centerY());
		pCanvas.concat(tM);
		if(mIsShowBmp){
			if(mAnimValueSwitch > 0.5f){
//				mBmpPaint.setAlpha((int)(0 + 255 * mAnimValueSwitch));
				mBmpPaint.setAlpha(255);
			}
		}else{
			if(mAnimValueSwitch <= 0.5f){
//				mBmpPaint.setAlpha((int)(255 - 255 * mAnimValueSwitch * 2));
			}else{
				mBmpPaint.setAlpha(0);
			}
		}
		
		pCanvas.drawBitmap(mBmpIcon, null, tRect, mBmpPaint);
		pCanvas.restore();
	}
	
	private void draw3DRotateTxt(Canvas pCanvas){
		if(mIsShowBmp){
			if(mAnimValueSwitch <= 0.5f){
//				mTxtPaint.setAlpha((int)(255 - 255 * mAnimValueSwitch * 2));
//				mTxtSufPaint.setAlpha((int)(255 - 255 * mAnimValueSwitch * 2));
			}else{
				mTxtPaint.setAlpha(0);
				mTxtSufPaint.setAlpha(0);
			}
		}else{
			if(mAnimValueSwitch > 0.5f){
//				mTxtPaint.setAlpha((int)(255 * mAnimValueSwitch));
//				mTxtSufPaint.setAlpha((int)(255 * mAnimValueSwitch));
				mTxtPaint.setAlpha(255);
				mTxtSufPaint.setAlpha(255);
			}
		}
		StringBuffer tTxt = new StringBuffer();
		if(mPercent > 0 && mPercent <= MIN_PERCENT){
			tTxt.append(MIN_PERCENT);
		}else{
			tTxt.append(mPercent > 10 ? Math.round(mPercent) : mFormat.format(mPercent));
		}
		float tTxtHeight = mTxtPaint.descent() - mTxtPaint.ascent();
        float tVerticalTextOffset = (tTxtHeight / 2) - mTxtPaint.descent();
        float tHorizontalTextOffset = (mTxtPaint.measureText(tTxt.toString()) + mTxtSufPaint.measureText(MSG_CENTER_SUF)) / 2;
    	float tStart = mCenter.y + tVerticalTextOffset;
    	pCanvas.drawText(tTxt.toString(), mCenter.x - tHorizontalTextOffset
    			, tStart
    			, mTxtPaint);
    	pCanvas.drawText(MSG_CENTER_SUF, mCenter.x - tHorizontalTextOffset + mTxtPaint.measureText(tTxt.toString())
	        	, tStart
	        	, mTxtSufPaint);
	}
	
	private void startAnimSwitch(){
		if (android.os.Build.VERSION.SDK_INT < 11)
            return;
		if(mAnimaSwitch.isRunning()) return;
		mAnimaSwitch.start();
	}

}
