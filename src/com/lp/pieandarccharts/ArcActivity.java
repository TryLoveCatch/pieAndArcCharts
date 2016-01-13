package com.lp.pieandarccharts;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;

public class ArcActivity extends Activity{

	private ArcView mArcView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.arc);
		mArcView = (ArcView)findViewById(R.id.arcview);
		List<Long> tArr = new ArrayList<Long>();
		tArr.add(123456l);
		tArr.add(1234560l);
		mArcView.setColorAndData(new int[]{R.color.music_start_color, R.color.music_end_color}, tArr, R.drawable.home_music);
	}
}
