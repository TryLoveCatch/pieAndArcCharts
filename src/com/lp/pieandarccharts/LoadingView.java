package com.lp.pieandarccharts;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.NinePatch;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

public class LoadingView extends View {
	
	private Paint mPaintScan;
	private TextPaint mPaintTitle;
	private TextPaint mPaintCancel;
	private Paint mPaintLine;
	private Paint mPaintNumber;
	private Point mCenter;
	
	private int mDefaultWidth = R.dimen.dp_230;
	private int mDefaultHeight = R.dimen.dp_280;
	private int mPadding = R.dimen.dp_8;
	private int mTitleTextSize = R.dimen.dp_15;
	private int mNumBgRadius = R.dimen.dp_75;
	private String mTitleTextColor = "#333333";
	private int mCancelTextSize = R.dimen.dp_18;
	private String mCancelTextColor = "#35a4f2";
	private long mAnimateTime = 3000;
	
	private int mResIdFull = R.drawable.analysis_loading_sdcard_full;
	private int mResIdEmpty = R.drawable.analysis_loading_sdcard_empty;
	
	private String mTitle;
	
	private AnimatorSet mAnimaSet;
	private float mAnimValueScan = 0f;
	private ObjectAnimator mAnimaScan;
//	private float mAnimValueNumber = 0f;
//	private ObjectAnimator mAnimaNumber;
	private boolean mIsFull;
	private CustomAnimatoion mCustomAnimatoion;
	
	private PorterDuffXfermode mXfermode;
	private BitmapShader mBitmapShader;
	private Matrix mBitmapShaderMatrix;
	private Bitmap mBmpBg;
	private Bitmap mBmpIconFull;
	private Bitmap mBmpIconEmpty;
	private Bitmap mBmpScan; 
	private Bitmap mBmpRing;
	private Bitmap mBmpNumberBg;
	
	private RectF mRectTitle;
	private Rect mRectCancel;
	
	private OnCancelListener onCancelListener;
	
	public LoadingView(Context context) {
		super(context);
		init();
	}

	public LoadingView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		initAttrs(context, attrs);
		init();
	}

	public LoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initAttrs(context, attrs);
		init();
	}

	public LoadingView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initAttrs(context, attrs);
		init();
	}
	
	public void setTitle(String pTitle){
		
		mTitle = pTitle;
		
//		startAnimate();
	}
	
	public void startAnimate() {

        if (android.os.Build.VERSION.SDK_INT < 11){
        	if(mCustomAnimatoion.isRunning())
	        	return;
	        
	        mAnimValueScan = 0f;
//	        mAnimValueNumber = 0f;
	        startAnimation(mCustomAnimatoion);
        }else{
	        if(mAnimaSet.isRunning())
	        	return;
	        
	        mAnimValueScan = 0f;
//	        mAnimValueNumber = 0f;
	        mAnimaSet.start();
        }
    }
	
	public void stopAnimate(){
		if (android.os.Build.VERSION.SDK_INT < 11){
			if(mCustomAnimatoion!=null && mCustomAnimatoion.isRunning()){
				mCustomAnimatoion.cancel();
			}
		}else{
			if(mAnimaSet!=null && mAnimaSet.isRunning()){
				mAnimaSet.end();
				mAnimaSet.cancel();
			}
		}
	}
	
	public void setOnCancelListener(OnCancelListener onCancelListener){
		this.onCancelListener = onCancelListener;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int tWidth = dip2px(mDefaultWidth);
        int tHeight = dip2px(mDefaultHeight);
        setMeasuredDimension(
                Math.max(getSuggestedMinimumWidth(),
                        resolveSize(tWidth,
                                widthMeasureSpec)),
                Math.max(getSuggestedMinimumHeight(),
                        resolveSize(tHeight,
                                heightMeasureSpec)));
    }
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch(event.getAction()){
		case MotionEvent.ACTION_DOWN:
			if(isClickCancel((int)event.getX(), (int)event.getY())){
				mPaintCancel.setColor(Color.RED);
				invalidate(mRectCancel);
			}
			break;
		case MotionEvent.ACTION_UP:
			if(isClickCancel((int)event.getX(), (int)event.getY())){
				if(onCancelListener!=null){
					onCancelListener.onCancel();
				}
			}
		case MotionEvent.ACTION_CANCEL:
			mPaintCancel.setColor(Color.parseColor(mCancelTextColor));
			invalidate(mRectCancel);
			break;
		}
		return true;
	}
//	
//	@Override
//	public void setVisibility(int visibility) {
//		super.setVisibility(visibility);
//		if(visibility==View.GONE || visibility==View.INVISIBLE){
//			stopAnimate();
//		}else{
//			startAnimate();
//		}
//	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		canvas.drawColor(Color.TRANSPARENT);
		
		mCenter = new Point(getWidth() / 2, getWidth() / 2);
		
		drawBg(canvas);
		drawTitleText(canvas);
		drawCancelText(canvas);
		drawLine(canvas);
		drawBmpNumber(canvas);
		
		drawScan(canvas);
	}
	
	private void initAttrs(Context context, AttributeSet attrs){
		TypedArray tTypeArry = context.obtainStyledAttributes(attrs, R.styleable.LoadingView);
		mResIdFull = tTypeArry.getResourceId(R.styleable.LoadingView_iconFull, mResIdFull);
		mResIdEmpty = tTypeArry.getResourceId(R.styleable.LoadingView_iconEmpty, mResIdEmpty);
	}
	
	private void init(){
		
		mXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT);
		
		mBmpBg = BitmapFactory.decodeResource(getResources(), R.drawable.analysis_loading_bg);
		mBmpIconFull = BitmapFactory.decodeResource(getResources(), mResIdFull);
		mBmpIconEmpty = BitmapFactory.decodeResource(getResources(), mResIdEmpty);
		mBmpScan = BitmapFactory.decodeResource(getResources(), R.drawable.analysis_loading_scan_img);
		mBmpRing = BitmapFactory.decodeResource(getResources(), R.drawable.analysis_loading_scan_bg);
		mBmpNumberBg = BitmapFactory.decodeResource(getResources(), R.drawable.analysis_loading_01_bg);
		
		mPaintScan = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaintScan.setStyle(Style.STROKE);
		mPaintScan.setStrokeWidth(0);
		
		mPaintTitle = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		mPaintTitle.setColor(Color.parseColor(mTitleTextColor));
		mPaintTitle.setTextSize(dip2px(mTitleTextSize));
		mPaintCancel = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		mPaintCancel.setColor(Color.parseColor(mCancelTextColor));
		mPaintCancel.setTextSize(dip2px(mCancelTextSize));
		mPaintCancel.setTypeface(Typeface.DEFAULT_BOLD);
		
		mPaintLine = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaintLine.setColor(Color.parseColor("#dedfe0"));
		
		mPaintNumber = new Paint(Paint.ANTI_ALIAS_FLAG);
		mBitmapShader = new BitmapShader(mBmpNumberBg, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
		mBitmapShaderMatrix = new Matrix();
		mBitmapShader.setLocalMatrix(mBitmapShaderMatrix);
		mPaintNumber.setShader(mBitmapShader);
		
		mAnimaScan = ObjectAnimator.ofFloat(this, "animateScan", 0f, 1f);
		mAnimaScan.setRepeatCount(ValueAnimator.INFINITE);
		mAnimaScan.setRepeatMode(ValueAnimator.RESTART);
		mAnimaScan.setInterpolator(new LinearInterpolator());
		mAnimaScan.setDuration(mAnimateTime);
		mAnimaScan.addListener(new Animator.AnimatorListener(){

			@Override
			public void onAnimationStart(Animator animation) {
				
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				
			}

			@Override
			public void onAnimationRepeat(Animator animation) {
				mIsFull = !mIsFull;
			}
			
		});
//		mAnimaNumber = ObjectAnimator.ofFloat(this, "animValueNumber", 0f, 1f);
//		mAnimaNumber.setRepeatCount(ValueAnimator.INFINITE);
//		mAnimaNumber.setRepeatMode(ValueAnimator.RESTART);
//		mAnimaNumber.setInterpolator(new LinearInterpolator());
//		mAnimaNumber.setDuration(mAnimateTime);
		
		mAnimaSet = new AnimatorSet();
		mAnimaSet.playTogether(/**mAnimaNumber,*/ mAnimaScan);
		
		mCustomAnimatoion = new CustomAnimatoion();
	}
	
	
	private int dip2px(int pId){
		return getContext().getResources().getDimensionPixelSize(pId);
	}
	
	private void drawTitleText(Canvas pCanvas){
		if(mTitle==null || mTitle.length() <= 0) return;
		StringBuffer tTxt = new StringBuffer(mTitle);
		float tTxtHeight = mPaintTitle.descent() - mPaintTitle.ascent();
        float tVerticalTextOffset = (tTxtHeight / 2) - mPaintTitle.descent();
        float tHorizontalTextOffset = mPaintTitle.measureText(tTxt.toString()) / 2;
        if(mRectTitle==null){
	        mRectTitle = new RectF();
	        mRectTitle.left = 0;
	        mRectTitle.top = 0;
	        mRectTitle.right = getWidth();
	        mRectTitle.bottom = tTxtHeight + dip2px(mPadding) * 2;
        }
    	pCanvas.drawText(tTxt.toString()
    			, mRectTitle.centerX() - tHorizontalTextOffset
    			, mRectTitle.centerY() + tVerticalTextOffset
    			, mPaintTitle);
	}
	
	private void drawCancelText(Canvas pCanvas){
		StringBuffer tTxt = new StringBuffer(getResources().getString(R.string.confirm_cancel));
		float tTxtHeight = mPaintCancel.descent() - mPaintCancel.ascent();
        float tVerticalTextOffset = (tTxtHeight / 2) - mPaintCancel.descent();
        float tHorizontalTextOffset = mPaintCancel.measureText(tTxt.toString()) / 2;
        if(mRectCancel==null){
        	mRectCancel = new Rect();
        	mRectCancel.left = 0;
        	mRectCancel.top = getWidth();
        	mRectCancel.right = getWidth();
//        	mRectCancel.bottom = Math.round(mRectCancel.top + tTxtHeight + dip2px(mPadding) * 2);
        	mRectCancel.bottom = Math.round(mRectCancel.top + getHeight() - getWidth());
        }
    	pCanvas.drawText(tTxt.toString()
    			, mRectCancel.centerX() - tHorizontalTextOffset
    			, mRectCancel.centerY() + tVerticalTextOffset
    			, mPaintCancel);
	}
	
	private void drawLine(Canvas pCanvas){
		pCanvas.drawLine(0, mRectCancel.top, getWidth(), mRectCancel.top + 1, mPaintLine);
	}
	
	private void drawBmpRing(Canvas pCanvas){
		Rect tRect = new Rect();
		tRect.left = mCenter.x - mBmpRing.getWidth()/2;
		tRect.top = mCenter.y - mBmpRing.getHeight()/2;
		tRect.right = mCenter.x + mBmpRing.getWidth()/2;
		tRect.bottom = mCenter.y + mBmpRing.getHeight()/2;
		pCanvas.drawBitmap(mBmpRing, null, tRect, null);
	}
	
	private void drawBmpNumber(Canvas pCanvas){
		int tCount = pCanvas.saveLayer(0, 0, getWidth(), getWidth(), null, Canvas.ALL_SAVE_FLAG);
		
		Rect tRect = new Rect();
		tRect.left = 0;
		tRect.top = 0;
		tRect.right = getWidth();
		tRect.bottom = getWidth();
		
		drawBmpRing(pCanvas);
//		mBitmapShaderMatrix.setTranslate(0, getWidth() * mAnimValueNumber);
		mBitmapShaderMatrix.setTranslate(0, getWidth() * mAnimValueScan);
		mBitmapShader.setLocalMatrix(mBitmapShaderMatrix);
		mPaintNumber.setShader(mBitmapShader);
		pCanvas.drawCircle(mCenter.x, mCenter.y, dip2px(mNumBgRadius), mPaintNumber);
		mPaintNumber.setXfermode(null);
		
		pCanvas.restoreToCount(tCount);
	}
	
	private void drawScan(Canvas pCanvas){
		
		Rect tRect = new Rect();
		tRect.left = mCenter.x - mBmpIconFull.getWidth()/2;
		tRect.top = mCenter.y - mBmpIconFull.getHeight()/2;
		tRect.right = mCenter.x + mBmpIconFull.getWidth()/2;
		tRect.bottom = mCenter.y + mBmpIconFull.getHeight()/2;
		if(mIsFull){
			pCanvas.drawBitmap(mBmpIconEmpty, null, tRect, null);
		}else{
			pCanvas.drawBitmap(mBmpIconFull, null, tRect, null);
		}
		
		int tCount = pCanvas.saveLayer(0, 0, getWidth(), getWidth(), null, Canvas.ALL_SAVE_FLAG);
		
		if(mIsFull){
			pCanvas.drawBitmap(mBmpIconFull, null, tRect, null);
		}else{
			pCanvas.drawBitmap(mBmpIconEmpty, null, tRect, null);
		}
		
		mPaintScan.setXfermode(mXfermode);
		tRect = new Rect();
		tRect.left = -Math.round(getWidth() * (1 - mAnimValueScan));
		tRect.top = 0;
		tRect.right = tRect.left + getWidth();
		tRect.bottom = getWidth();
		if(mIsFull){
			pCanvas.rotate(180, mCenter.x, mCenter.y);
		}
		pCanvas.drawBitmap(mBmpScan, null, tRect, mPaintScan);
		mPaintScan.setXfermode(null);
		
		pCanvas.restoreToCount(tCount);
	}
	
	private void drawBg(Canvas pCanvas){
		Rect tRect = new Rect();
		tRect.left = 0;
		tRect.top = 0;
		tRect.right = getWidth();
		tRect.bottom = getHeight();
		draw9Png(pCanvas, mBmpBg, tRect, null);
	}
	
	public float getAnimateScan() {
		return mAnimValueScan;
	}

	public void setAnimateScan(float animateValue) {
		this.mAnimValueScan = animateValue;
		postInvalidate();
	}
	
//	public void setAnimValueNumber(float animateValue) {
//		this.mAnimValueNumber = animateValue;
//		postInvalidate();
//	}
	
	private void draw9Png(Canvas pCanvas, Bitmap pBmp, Rect pRect, Paint pPaint){
		NinePatch patch = new NinePatch(pBmp, pBmp.getNinePatchChunk(), null);  
        patch.draw(pCanvas, pRect, pPaint);  
	}
	
	private boolean isClickCancel(int pX, int pY){
		return mRectCancel.contains(pX, pY);
	}

	public interface OnCancelListener{
		void onCancel();
	}
	
	private class CustomAnimatoion extends Animation implements Animation.AnimationListener{
		private boolean mIsRunning = false;;
		@Override
		public void initialize(int width, int height, int parentWidth, int parentHeight) {
			super.initialize(width, height, parentWidth, parentHeight);
			setDuration(mAnimateTime);
			setRepeatMode(Animation.RESTART);
			setRepeatCount(Animation.INFINITE);
			setInterpolator(new LinearInterpolator());
			setAnimationListener(this);
		}
		@Override
		protected void applyTransformation(float interpolatedTime, Transformation t) {
			super.applyTransformation(interpolatedTime, t);
			setAnimateScan(interpolatedTime);
		}
		@Override
		public void onAnimationStart(Animation animation) {
			mIsRunning = true;
		}
		@Override
		public void onAnimationEnd(Animation animation) {
			mIsRunning = false;
		}
		@Override
		public void onAnimationRepeat(Animation animation) {
			
		}
		
		public boolean isRunning(){
			return mIsRunning;
		}
	}
}
