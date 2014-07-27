package com.android_app;

import com.android_app.MainActivity.MessageHelper;
import com.games.wordladder.Difficulty;
import com.games.wordladder.WordLadder;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.os.Build;
import static com.android_app.MainActivity.TAG;

public class GameActivity extends ActionBarActivity {
	private TextView loadingInfoTextView;
	private TextView generatorInfoTextView;
	private ProgressBar loadingInfoProgressBar;
	
	private Handler handler;
	
	private TextView puzzleTextView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);
		handler = new Handler();
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new GameFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.game, menu);
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

	public class GameFragment extends Fragment {
		
		public GameFragment() {
			setRetainInstance(true);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_game, container,
					false);

			puzzleTextView = (TextView) rootView.findViewById(R.id.puzzle_textView);
			loadingInfoProgressBar = (ProgressBar) rootView.findViewById(R.id.loadingInfo_progressBar);
			loadingInfoTextView = (TextView) rootView.findViewById(R.id.loadingInfo_textView);
			generatorInfoTextView = (TextView) rootView.findViewById(R.id.generatorInfo_textView);
			
			gameThread.start();
			
			return rootView;
		}
	}
	
	Thread gameThread = new Thread(new Runnable() {
		public void run() {
			try {
	//			Now start UI for word ladder generation
				Message message = MessageHelper.getStartMessage("Generating word ladder");
				loadingInfoHandle(message);
				Log.v(TAG, "Starting game progress thread");
				Thread.sleep(100);
				final WordLadder wl = new WordLadder(Difficulty.HARD, GameActivity.this);
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
}
