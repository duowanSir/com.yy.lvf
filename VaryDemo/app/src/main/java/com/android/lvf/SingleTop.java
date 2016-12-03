package com.android.lvf;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class SingleTop extends Activity implements OnClickListener {
	TextView mInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mInfo = (TextView) findViewById(R.id.info_tv);
		mInfo.setText(getClass().getName());
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.standard) {
			Intent intent = new Intent(this, Standard.class);
			startActivity(intent);
		} else if (v.getId() == R.id.single_top) {
			Intent intent = new Intent(this, SingleTop.class);
			startActivity(intent);
		} else if (v.getId() == R.id.single_task) {
			Intent intent = new Intent(this, SingleTask.class);
			startActivity(intent);
		} else {
			Intent intent = new Intent(this, SingleInstance.class);
			startActivity(intent);
		}
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		Toast.makeText(this, getClass().getName(), Toast.LENGTH_SHORT).show();
		super.onNewIntent(intent);
	}
}
