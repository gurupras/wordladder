package com.android_app;

import com.android_app.R;
import com.games.wordladder.DictionaryParser;
import com.games.wordladder.Difficulty;
import com.games.wordladder.WordLadder;

import android.support.v7.app.ActionBarActivity;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {
	public static final String TAG = "WordLadder";
	private TextView loadingInfoTextView;
	private TextView puzzleTextView;
	private TextView generatorInfoTextView;
	private ProgressBar loadingInfoProgressBar;
	private Handler handler;
	private BackgroundMusic mBGM = new BackgroundMusic();
 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		handler = new Handler();
		
		loadingInfoProgressBar = (ProgressBar) findViewById(R.id.loadingInfo_progressBar);
		loadingInfoTextView = (TextView) findViewById(R.id.loadingInfo_textView);
		puzzleTextView = (TextView) findViewById(R.id.puzzle_textView);
		generatorInfoTextView = (TextView) findViewById(R.id.generatorInfo_textView);
		loadingInfoProgressBar.setVisibility(View.VISIBLE);
		loadingInfoTextView.setVisibility(View.VISIBLE);
		loadingInfoTextView.setText("Loading Dictionary");
		
		resourceLoaderThread.start();
		
		Thread gameThread = new Thread(new Runnable() {
			public void run() {
				try {
					resourceLoaderThread.join();
					progressThread.interrupt();
					progressThread.join();
					
//					Now start UI for word ladder generation
					Message message = MessageHelper.getStartMessage("Generating word ladder");
					loadingInfoHandle(message);
					progressThread = new Thread(progressRunnable);
					Log.v(TAG, "Starting game progress thread");
					Thread.sleep(100);
					progressThread.start();
					final WordLadder wl = new WordLadder(Difficulty.HARD, MainActivity.this);
					progressThread.interrupt();
					message = MessageHelper.getStopMessage();
					loadingInfoHandle(message);
					
					handler.post(new Runnable() {
						public void run() {
							puzzleTextView.setText(wl.getPath());
						}
					});
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		gameThread.start();
	}

	
	Runnable resourceLoader = new Runnable() {
		public void run() {
			Log.d(TAG, "Started loading dictionary");
			Message message = MessageHelper.getStartMessage("Loading Dictionary");
			loadingInfoHandle(message);
			progressThread.start();
			try {
				DictionaryParser.init(MainActivity.this);
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
		}
	};
	
	Runnable progressRunnable = new Runnable() {
		public void run() {
//			int idx = 0;
//			final String originalString = loadingInfoTextView.getText().toString();
//			Bundle bundle = new Bundle();
//			bundle.putString("operation", "update");
//			Message message = new Message();
//			message.setData(bundle);
//			while(true) {
//				if(idx % 4 == 0) {
//					bundle.putString("text", originalString);
//					idx = 0;
//				}
//				else {
//						String str = loadingInfoTextView.getText().toString();
//						char[] array = str.toCharArray();
//						array[array.length - 4 + idx] = '.';
//						str = String.valueOf(array);
//						bundle.putString("text", str);
//				}
//				infoHandler.dispatchMessage(message);
//				try {
//					Log.v(TAG, "progressThread running with text :" + originalString);
//					Thread.sleep(10);
//				} catch (InterruptedException e) {
//					bundle.putString("operation", "stop");
//					infoHandler.dispatchMessage(message);
//					Log.v(TAG, "progressRunnable terminating due to interrupt");
//					break;
//				}
//				idx++;
//			}
		}
	};
	
	Thread resourceLoaderThread = new Thread(resourceLoader);
	Thread progressThread = new Thread(progressRunnable);
	
	
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
//					Log.v(TAG, "loadingInfoTextView text :" + text);
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

	public synchronized void generatorInfoHandle(Message message) {
		
		Bundle bundle = message.getData();
		final String text = bundle.getString("text");
		Runnable updatePuzzle = new Runnable() {
			public void run() {
				generatorInfoTextView.setText(text);		
			}
		};
		runOnUiThread(updatePuzzle);
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
		mBGM.resume();
	}
	

	private class BackgroundMusic extends AsyncTask<Void, Void, Void> {
		private MediaPlayer player;
		@Override
		protected synchronized Void doInBackground(Void... params) {
			if(player == null)
				player = MediaPlayer.create(MainActivity.this, R.raw.bgm_1);
			if(!player.isPlaying()) {
				player.setLooping(true);
				player.setVolume(15, 15);
				player.start();
			}
			return null;
		}
		public synchronized void pause() {
			if(player == null || !player.isPlaying())
				return;
			player.pause();
		}
		public synchronized void resume() {
			if(player == null) {
				execute(new Void[] {null});
				return;
			}
			if(player.isPlaying())
				return;
			player.start();
		}
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
