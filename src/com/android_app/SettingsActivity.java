package com.android_app;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.os.Build;

public class SettingsActivity extends ActionBarActivity {
	protected static final String PREFERENCE_FILE = "preferences";
	public final String TAG = "WordLadder->Settings"; 
	private SharedPreferences preferences;
	
	private CheckBox musicCheckBox;
	private Handler handler;
	
	private Boolean userWantsMusic = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		handler = new Handler();
		preferences = getSharedPreferences(PREFERENCE_FILE, 0);
		
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new SettingsFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public class SettingsFragment extends Fragment {

		public SettingsFragment() {
			setRetainInstance(true);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_settings,
					container, false);
			
			musicCheckBox = (CheckBox) rootView.findViewById(R.id.musicCheckBox);
			synchronized(userWantsMusic) {
				userWantsMusic = preferences.getBoolean("music", true);
				musicCheckBox.setChecked(userWantsMusic);
			}
			
			
			musicCheckBox.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					musicCheckBox_onClick(view);
				}
			});
			return rootView;
		}
		
		public synchronized void musicCheckBox_onClick(View view) {
			BackgroundMusic bgm = BackgroundMusic.getInstance(SettingsActivity.this);
			if(musicCheckBox.isChecked()) {
				userWantsMusic = true;
				bgm.play();
			}
			else {
				userWantsMusic = false;
				bgm.setActivityWide(false);
				bgm.disable();
			}
		}
	}
	
	@Override
	public void onStop() {
		SharedPreferences.Editor editor = preferences.edit();
		if(editor == null)
			Log.e(TAG, "Could not save preferences!");
		else {
			editor.putBoolean("music", userWantsMusic);
			editor.commit();
		}
		super.onStop();
	}
}
