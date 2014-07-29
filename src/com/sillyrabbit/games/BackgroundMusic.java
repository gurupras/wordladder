package com.sillyrabbit.games;

import java.io.IOException;

import com.android_app.R;

import android.content.Context;
import android.media.MediaPlayer;

public class BackgroundMusic {
	private boolean continuePlaying = true;
	private MediaPlayer player;
	private Context context;
	private static BackgroundMusic instance;

	private BackgroundMusic(Context context) {
		this.context = context;
	}
	public synchronized static BackgroundMusic getInstance(Context context) {
		if(instance == null) {
			instance = new BackgroundMusic(context);
			instance.init();
		}
		return instance;
	}
	
	public synchronized void pause() {
		if(player == null || !player.isPlaying() || continuePlaying)
			return;
		player.pause();
	}
	
	public synchronized void disable() {
		if(player == null || !player.isPlaying())
			return;
		else {
			player.stop();
			player.reset();
			player.release();
			player = null;
		}
	}
	
	public synchronized void play() {
		if(player == null)
			init();
		if(player.isPlaying())
			return;
		try {
			player.start();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void init() {
		if(player == null) {
			player = MediaPlayer.create(context, R.raw.bgm_1);
			player.setLooping(true);
			player.setVolume(15, 15);
		}
	}
	
	public synchronized void setActivityWide(boolean flag) {
		this.continuePlaying = flag;
	}
}