/*
 * Copyright (c) 2012-2013 NetEase, Inc. and other contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.netease.qa.emmagee.activity;

import java.io.DataOutputStream;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.qa.emmagee.R;
import com.netease.qa.emmagee.utils.Settings;

/**
 * Setting Page of Emmagee
 * 
 * @author andrewleo
 */
public class SettingsActivity extends Activity {

	private static final String LOG_TAG = "Emmagee-" + SettingsActivity.class.getSimpleName();

	private CheckBox chkFloat;
	private CheckBox chkRoot;
	private CheckBox chkAutoStop;
	private TextView tvTime;
	private LinearLayout about;
	private LinearLayout mailSettings;

	private SharedPreferences preferences;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(LOG_TAG, "onCreate");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.settings);

		chkFloat = (CheckBox) findViewById(R.id.floating);
		chkRoot = (CheckBox) findViewById(R.id.is_root);
		chkAutoStop = (CheckBox) findViewById(R.id.auto_stop);
		tvTime = (TextView) findViewById(R.id.time);
		about = (LinearLayout) findViewById(R.id.about);
		mailSettings = (LinearLayout) findViewById(R.id.mail_settings);
		SeekBar timeBar = (SeekBar) findViewById(R.id.timeline);
		ImageView btnSave = (ImageView) findViewById(R.id.btn_set);
		RelativeLayout floatingItem = (RelativeLayout) findViewById(R.id.floating_item);
		RelativeLayout autoStopItem = (RelativeLayout) findViewById(R.id.auto_stop_item);
		LinearLayout layGoBack = (LinearLayout) findViewById(R.id.lay_go_back);
		LinearLayout layHeapItem = (LinearLayout) findViewById(R.id.heap_item);
		

		btnSave.setVisibility(ImageView.INVISIBLE);
		
		preferences = Settings.getDefaultSharedPreferences(getApplicationContext());
		int interval = preferences.getInt(Settings.KEY_INTERVAL, 5);
		boolean isfloat = preferences.getBoolean(Settings.KEY_ISFLOAT, true);
		boolean isRoot = preferences.getBoolean(Settings.KEY_ROOT, false);
		boolean autoStop = preferences.getBoolean(Settings.KEY_AUTO_STOP, true);
		
		tvTime.setText(String.valueOf(interval));
		chkFloat.setChecked(isfloat);
		chkRoot.setChecked(isRoot);
		chkAutoStop.setChecked(autoStop);
		
		timeBar.setProgress(interval);
		timeBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				tvTime.setText(Integer.toString(arg1 + 1));
			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
				// when tracking stoped, update preferences
				int interval = arg0.getProgress() + 1;
				preferences.edit().putInt(Settings.KEY_INTERVAL, interval).commit();
			}
		});

		layGoBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SettingsActivity.this.finish();
				Intent intent = new Intent();
				intent.setClass(SettingsActivity.this, MainPageActivity.class);
				startActivity(intent);
			}
		});

		mailSettings.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.setClass(SettingsActivity.this, MailSettingsActivity.class);
				startActivity(intent);
			}
		});

		about.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.setClass(SettingsActivity.this, AboutActivity.class);
				startActivity(intent);
			}
		});

		floatingItem.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				boolean isChecked = chkFloat.isChecked();
				chkFloat.setChecked(!isChecked);
				preferences.edit().putBoolean(Settings.KEY_ISFLOAT, !isChecked).commit();
			}
		});
		
		autoStopItem.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				boolean isChecked = chkAutoStop.isChecked();
				chkAutoStop.setChecked(!isChecked);
				preferences.edit().putBoolean(Settings.KEY_AUTO_STOP, !isChecked).commit();
			}
		});
		
		// get root permission
		layHeapItem.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// if root checkbox is checked, change status to
				// opposite;otherwise, try to upgrade app to root
				boolean isChecked = chkRoot.isChecked();
				if (isChecked) {
					chkRoot.setChecked(!isChecked);
					preferences.edit().putBoolean(Settings.KEY_ROOT, !isChecked).commit();
				} else {
					boolean root = upgradeRootPermission(getPackageCodePath());
					if (root) {
						Log.d(LOG_TAG, "root succeed");
						chkRoot.setChecked(!isChecked);
						preferences.edit().putBoolean(Settings.KEY_ROOT, !isChecked).commit();
					} else {
						// if root failed, tell user to check if phone is rooted
						Toast.makeText(getBaseContext(), getString(R.string.root_failed_notification), Toast.LENGTH_LONG).show();
					}
				}

			}
		});
	}

	@Override
	public void finish() {
		super.finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	/**
	 * upgrade app to get root permission
	 * 
	 * @return is root successfully
	 */
	public static boolean upgradeRootPermission(String pkgCodePath) {
		Process process = null;
		DataOutputStream os = null;
		try {
			String cmd = "chmod 777 " + pkgCodePath;
			process = Runtime.getRuntime().exec("su"); // 切换到root帐号
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes(cmd + "\n");
			os.writeBytes("exit\n");
			os.flush();
			int existValue = process.waitFor();
			if (existValue == 0) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			Log.w(LOG_TAG, "upgradeRootPermission exception=" + e.getMessage());
			return false;
		} finally {
			try {
				if (os != null) {
					os.close();
				}
				process.destroy();
			} catch (Exception e) {
			}
		}
	}
}
