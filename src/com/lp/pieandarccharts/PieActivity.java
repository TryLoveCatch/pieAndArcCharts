package com.lp.pieandarccharts;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;

public class PieActivity extends Activity{

	private PieView mPieView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pie);
		mPieView = (PieView)findViewById(R.id.pieview);
		List<Long> tArrData = new ArrayList<Long>();
		tArrData.add(990l);
		tArrData.add(1021l);
		tArrData.add(999333l);
		tArrData.add(1025l);
		tArrData.add(1024333l);
		tArrData.add(1024333l);
		tArrData.add(1024333l);
		tArrData.add(990l+1021+999333+1025+1024333+1024333+1024333);
		List<Integer> tArrColors = new ArrayList<Integer>();
		tArrColors.add(0xff4dcfff);
		tArrColors.add(0xffffcd1a);
		tArrColors.add(0xffff674d);
		tArrColors.add(0xff93e62e);
		tArrColors.add(0xff62768c);
		tArrColors.add(0xffa3b6cc);
		tArrColors.add(0xffEBECED);
		mPieView.setColorAndData(tArrColors, tArrData, 56.9f);
	}
}
