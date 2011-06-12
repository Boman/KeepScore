package com.nolanlawson.keepscore;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.nolanlawson.keepscore.db.Game;
import com.nolanlawson.keepscore.db.GameDBHelper;
import com.nolanlawson.keepscore.db.PlayerScore;
import com.nolanlawson.keepscore.helper.PreferenceHelper;
import com.nolanlawson.keepscore.util.UtilLogger;
import com.nolanlawson.keepscore.widget.PlayerView;

public class GameActivity extends Activity {
	
	public static final String EXTRA_PLAYER_NAMES = "playerNames";
	public static final String EXTRA_GAME_ID = "gameId";
	
	private static final UtilLogger log = new UtilLogger(GameActivity.class);
	
	private Game game;
	private List<PlayerScore> playerScores;
	private int numPlayers;
	private PowerManager.WakeLock wakeLock;
	
	private List<PlayerView> playerViews;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        createGame();
        
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(getContentViewResId());
        
        setUpWidgets();
        
        playerViews.get(0).getNameTextView().setSelected(false);
        
		PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, getPackageName());
    }

	private int getContentViewResId() {
		switch (numPlayers) {
		case 2:
			return R.layout.game_2;
		case 3:
		case 4:
			return R.layout.game_3_to_4;
		case 5:
		case 6:
		default:
			return R.layout.game_5_to_6;
		}
	}
	
	

	@Override
	protected void onPause() {
		super.onPause();
		
		if (wakeLock.isHeld()) {
			log.d("Releasing wakelock");
			wakeLock.release();
		}		
		
		if (shouldAutosave()) {
			saveGame(true);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		boolean useWakeLock = PreferenceHelper.getBooleanPreference(
				R.string.pref_use_wake_lock, R.string.pref_use_wake_lock_default, this);
		if (useWakeLock && !wakeLock.isHeld()) {
			log.d("Acquiring wakelock");
			wakeLock.acquire();
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.game_menu, menu);
	    
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
	    switch (item.getItemId()) {
	    case R.id.menu_history:
	    	Intent historyIntent = new Intent(this, HistoryActivity.class);
	    	historyIntent.putExtra(HistoryActivity.EXTRA_GAME, game);
	    	startActivity(historyIntent);
	    	break;
	    case R.id.menu_save:
	    	saveGame(false);
	    	break;
	    case R.id.menu_settings:
	    	Intent settingsIntent = new Intent(GameActivity.this, SettingsActivity.class);
	    	startActivity(settingsIntent);
	    	break;
	    }
	    return false;
	}
	
	private boolean shouldAutosave() {
		// only autosave if the user has changed SOMETHING, i.e. the scores aren't all just zero
		
		for (PlayerView playerView : playerViews) {
			if (playerView.getShouldAutosave().get()) {
				return true;
			}
		}
		
		return false;
	}
	
	private void createGame() {

		if (getIntent().hasExtra(EXTRA_PLAYER_NAMES)) {
			// starting a new game
			createNewGame();
		} else {
			// loading an existing game
			createExistingGame();
		}
	}

	private void createExistingGame() {
		int gameId = getIntent().getIntExtra(EXTRA_GAME_ID, 0);
		
		GameDBHelper dbHelper = null;
		try {
			dbHelper = new GameDBHelper(this);
			game = dbHelper.findGameById(gameId);
			playerScores = game.getPlayerScores();
			numPlayers = playerScores.size();
		} finally {
			if (dbHelper != null) {
				dbHelper.close();
			}
		}
		
		log.d("loaded game: %s", game);
		log.d("loaded playerScores: %s", playerScores);
		
	}

	private void createNewGame() {

        String[] playerNames = getIntent().getStringArrayExtra(EXTRA_PLAYER_NAMES);
        
        game = new Game();
        
        playerScores = new ArrayList<PlayerScore>();
        
        
        game.setDateStarted(System.currentTimeMillis());
        game.setPlayerScores(playerScores);
        
        for (int i = 0; i < playerNames.length; i++) {
        	
        	PlayerScore playerScore = new PlayerScore();
        	
        	playerScore.setName(playerNames[i]);
        	playerScore.setPlayerNumber(i);
        	playerScore.setHistory(new ArrayList<Integer>());
        	playerScore.setScore(PreferenceHelper.getIntPreference(
        			R.string.pref_initial_score, R.string.pref_initial_score_default, GameActivity.this));
        	
        	playerScores.add(playerScore);
        }
        
        numPlayers = playerNames.length;
        

		log.d("created new game: %s", game);
		log.d("created new playerScores: %s", playerScores);
	}

	private void saveGame(final boolean autosaved) {
		
		// do in the background to avoid jankiness
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				
				GameDBHelper dbHelper = null;
				try {
					dbHelper = new GameDBHelper(GameActivity.this);
					dbHelper.saveGame(game, autosaved);
				} finally {
					if (dbHelper != null) {
						dbHelper.close();
					}
				}
				
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				int resId = autosaved ? R.string.toast_saved_automatically : R.string.toast_saved;
				Toast.makeText(GameActivity.this, resId, Toast.LENGTH_SHORT).show();
			}
			
			
		}.execute((Void)null);
		
		for (PlayerView playerView : playerViews) {
			playerView.getShouldAutosave().set(false);
		}
		
	}	
	
	private void setUpWidgets() {

		playerViews = new ArrayList<PlayerView>();
		
		for (int i = 0; i < numPlayers; i++) {
			
			PlayerScore playerScore = playerScores.get(i);
			
			int resId = getPlayerViewResId(i);
			
			View view = findViewById(resId);
			
			PlayerView playerView = new PlayerView(this, view, playerScore);
			
			// sometimes the text gets cut off in the 6 player view, so make the player name smaller there
			if (numPlayers >= 5) {
				playerView.getNameTextView().setTextSize(
						getResources().getDimension(R.dimen.player_name_5_to_6));
			}
	    	
			playerViews.add(playerView);
			
		}
		
		if (numPlayers == 3) {
			// hide the "fourth" player
			findViewById(R.id.player_4).setVisibility(View.INVISIBLE);
		} else if (numPlayers == 5) {
			// hide the "sixth" player
			findViewById(R.id.player_6).setVisibility(View.INVISIBLE);
		}
		
		

    			
		
	}

	private int getPlayerViewResId(int playerNumber) {
		switch (playerNumber) {
		case 0:
			return R.id.player_1;
		case 1:
			return R.id.player_2;
		case 2:
			return R.id.player_3;
		case 3:
			return R.id.player_4;	
		case 4:
			return R.id.player_5;
		case 5: 
		default:
			return R.id.player_6;
		}
	}
}
