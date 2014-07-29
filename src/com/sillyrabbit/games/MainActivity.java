package com.sillyrabbit.games;

import com.android_app.R;
import com.sillyrabbit.games.wordladder.DictionaryParser;
import com.sillyrabbit.games.wordladder.DictionaryParser.DictionaryPatternException;
import com.sillyrabbit.games.wordladder.Difficulty;
import com.sillyrabbit.games.wordladder.WordLadder;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.os.Build;

public class MainActivity extends ActionBarActivity {
	public static final String TAG = "WordLadder";
	private TextView loadingInfoTextView;
	private ProgressBar loadingInfoProgressBar;

	private Button startGameButton;
	private Button settingsButton;
	private Button highScoresButton;
	
	private TextView puzzleTextView;
	
	
	private Handler handler;
	private BackgroundMusic mBGM;
	
	private Boolean loadComplete = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		handler = new Handler();
		mBGM = BackgroundMusic.getInstance(this);
		
		if (savedInstanceState == null) {
				getSupportFragmentManager().beginTransaction()
				 .add(R.id.container, new MainFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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

	public class MainFragment extends Fragment {
		
		public MainFragment() {
			setRetainInstance(true);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);

			loadingInfoProgressBar = (ProgressBar) rootView.findViewById(R.id.loadingInfo_progressBar);
			loadingInfoTextView = (TextView) rootView.findViewById(R.id.loadingInfo_textView);
			
			startGameButton = (Button) rootView.findViewById(R.id.startGame_button);
			settingsButton = (Button) rootView.findViewById(R.id.settings_button);
			highScoresButton = (Button) rootView.findViewById(R.id.highScores_button);
			
			synchronized(loadComplete) {
				if(!loadComplete) {
					startGameButton.setVisibility(View.INVISIBLE);
					startGameButton.setEnabled(false);
					settingsButton.setVisibility(View.INVISIBLE);
					settingsButton.setEnabled(false);
					highScoresButton.setVisibility(View.INVISIBLE);
					highScoresButton.setEnabled(false);

					loadingInfoProgressBar.setVisibility(View.VISIBLE);
					try {
						resourceLoaderThread.start();
					} catch(Exception e) {
						Log.w(TAG, "Fragment thread exception :" + e.getMessage());
						Log.d(TAG, "Fragment onCreate loadComplete? :" + loadComplete);
					}
				}
				else
					loadingInfoProgressBar.setVisibility(View.INVISIBLE);
			}
			return rootView;
		}
	}
	
	Thread resourceLoaderThread = new Thread(new Runnable() {
		public void run() {
			Log.d(TAG, "Started loading dictionary");
			Message message = MessageHelper.getStartMessage("Loading Dictionary");
			loadingInfoHandle(message);
			try {
				DictionaryParser.init(MainActivity.this);
			} catch(DictionaryPatternException e) {
//				uncomment if you want parse errors
//				Log.v(TAG, "Dictionary parsing errors :" e.getMessage());
			} catch(Exception e) {
				Log.e(TAG, e.getMessage());
			} finally {
				message = MessageHelper.getStopMessage();
				loadingInfoHandle(message);
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Log.d(TAG, "Finished loading dictionary");
			
			synchronized(loadComplete) {
				loadComplete = true;
				handler.post(new Runnable() {
					public void run() {
						startGameButton.setVisibility(View.VISIBLE);
						startGameButton.setEnabled(true);
						settingsButton.setVisibility(View.VISIBLE);
						settingsButton.setEnabled(true);
						highScoresButton.setVisibility(View.VISIBLE);
						highScoresButton.setEnabled(true);

					}
				});
				Log.d(TAG, "loadComplete? :" + loadComplete);
			}
		}
	});
	
	public synchronized void loadingInfoHandle(Message message) {
		Bundle bundle = message.getData();
		final String operation = bundle.get("operation").toString();
		final String text = bundle.getString("text");
		if(operation.equals("start")) {
			Runnable start = new Runnable() {
				public void run() {
					loadingInfoProgressBar.setVisibility(View.VISIBLE);
					loadingInfoProgressBar.setIndeterminate(true);
					loadingInfoTextView.setVisibility(View.VISIBLE);
					loadingInfoTextView.setText(text);
				}
			};
			runOnUiThread(start);
		}
		else if(operation.equals("update")) {
			Runnable update = new Runnable() {
				public void run() {
	//				Log.v(TAG, "loadingInfoTextView text :" + text);
					loadingInfoTextView.setText(text);
				}
			};
			runOnUiThread(update);
		}
		else if(operation.equals("stop")) {
			Runnable stop = new Runnable() {
				public void run() {
					loadingInfoTextView.setText("");
					loadingInfoProgressBar.setVisibility(View.INVISIBLE);
					loadingInfoTextView.setVisibility(View.INVISIBLE);
				}
			};
			runOnUiThread(stop);
		}
	}
	
	@Override
	protected void onDestroy() {
		mBGM.pause();
		super.onDestroy();
	}
	
	@Override
	protected void onPause() {
		mBGM.pause();
		super.onPause();
	}
	
	@Override
	protected void onStop() {
		mBGM.pause();
		super.onStop();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		SharedPreferences preferences = getSharedPreferences(SettingsActivity.PREFERENCE_FILE, 0);
		boolean userWantsMusic = preferences.getBoolean("music", true);
		if(userWantsMusic)
			mBGM.play();
	}
	
	
	public void startGameButton_onClick(View view) {
		Intent myIntent = new Intent(this, GameActivity.class);
//		myIntent.putExtra("key", value); //Optional parameters
		startActivity(myIntent);
	}
	
	public void settingsButton_onClick(View view) {
		Intent myIntent = new Intent(this, SettingsActivity.class);
//		myIntent.putExtra("key", value); //Optional parameters
		startActivity(myIntent);
	}
	
	public void highScoresButton_onClick(View view) {
		
	}
	
	public static class MessageHelper {
		public static Message getStartMessage(String text) {
			Bundle bundle = new Bundle();
			Message message = new Message();
			bundle.putString("operation", "start");
			bundle.putString("text", text);
			message.setData(bundle);
			return message;
		}
		
		public static Message getUpdateMessage(String text) {
			Bundle bundle = new Bundle();
			Message message = new Message();
			bundle.putString("operation", "update");
			bundle.putString("text", text);
			message.setData(bundle);
			return message;
		}
		
		public static Message getStopMessage() {
			Bundle bundle = new Bundle();
			Message message = new Message();
			bundle.putString("operation", "stop");
			message.setData(bundle);
			return message;
		}	
	}
}
