package com.lp.pieandarccharts;

import android.app.Activity;
import android.os.Bundle;

public class LoadingActivity extends Activity{

	private LoadingView mLoadingView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loading);
		mLoadingView = (LoadingView)findViewById(R.id.loadingView);
		mLoadingView.startAnimate();
	}
}
