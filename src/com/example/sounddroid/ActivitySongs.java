package com.example.sounddroid;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class ActivitySongs extends Activity {

	Cursor musicCursor = null;
	int music_column_index;
	MediaPlayer mMediaPlayer = null;
	int currentSongIndex = -1; // no song selected initially
	String songFilename = "";
	boolean playPauseButtonInPlayState = true; // changed to false when showing the "pause" image
	ImageView savedImageView = null;
	Drawable savedAlbumArt = null;
	static String SelectedSongID = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_songs);
		ShowSongs();
		ImageView playPauseButton = (ImageView) findViewById(R.id.PlayPauseButton);
		playPauseButton.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
		    	if (mMediaPlayer != null) {
		    		ImageView playPauseButton = (ImageView) findViewById(R.id.PlayPauseButton);
		    		if (playPauseButtonInPlayState) {
		    			playPauseButton.setImageResource(R.drawable.button_pause);
		    			mMediaPlayer.start();
		    			playPauseButtonInPlayState = false;
		    		} else {
		    			playPauseButton.setImageResource(R.drawable.button_play);
		    			mMediaPlayer.pause();
		    			playPauseButtonInPlayState = true;
		    		}
		    	}
		    	else
		    		Toast.makeText(getApplicationContext(), "Select a song first", Toast.LENGTH_LONG).show();
		    }
		});
		//ListView listView = (ListView) findViewById(R.id.songListView);
		//musicCursor.moveToPosition(1);
		//long AlbumID = musicCursor.getLong(musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
		//final Uri ART_CONTENT_URI = Uri.parse("content://media/external/audio/albumart");
        //Uri albumArtUri = ContentUris.withAppendedId(ART_CONTENT_URI, AlbumID);
        //Object o = listView.getItemAtPosition(1);
        //String s = o.toString();
		//new ImageLoader().execute(listView.getItemAtPosition(1), albumArtUri);
		//LoadAlbumArt();
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // so screen doesn't turn off while main window is shown (otherwise, media player dies when screen turns off)
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        //Toast.makeText(getApplicationContext(), "DEBUG: Just Resumed", Toast.LENGTH_LONG).show();
        
        // If the media player isn't created, create it
        //if (mMediaPlayer == null) {
      	//  mMediaPlayer = new MediaPlayer();
      	//  mMediaPlayer.setOnCompletionListener(MusicFinishedCallbackListener);
        //}
    }
    
    @Override
    public void onPause() {
        
        // Release the media player because we don't need it when paused
        // and other activities might need to use it.
        if (mMediaPlayer != null) {
      	  mMediaPlayer.stop();
      	  mMediaPlayer.release();
            mMediaPlayer = null;
        }
        
        super.onPause();
    }
    
    @Override
	protected void onDestroy() {
		super.onDestroy();
		if (musicCursor != null) {
			musicCursor.close();
			musicCursor = null;
		}
	}
    
    // ================================================================================
    // Get music from Media Store database and display in our custom list
    // ================================================================================
	public void ShowSongs() {
		// ------------------
		// Find Music (songs)
		// ------------------
		// ensure only music that has a title and artist is selected
		String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"
				+ " AND " + MediaStore.Audio.Media.ARTIST + " IS NOT NULL"
				+ " AND " + MediaStore.Audio.Media.TITLE + " IS NOT NULL"; 

		String[] projection = {
				MediaStore.Audio.Media._ID,
		        MediaStore.Audio.Media.ARTIST,
		        MediaStore.Audio.Media.TITLE,
		        MediaStore.Audio.Media.DATA,
		        MediaStore.Audio.Media.ALBUM_ID,
		        MediaStore.Audio.Media.DURATION
		};

		// Execute query and sort by Song Title
		try {
			musicCursor = getContentResolver().query(
	            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
	            projection, selection, null, MediaStore.Audio.Media.TITLE_KEY);
		} catch (Exception ex) {
			Toast.makeText(getApplicationContext(), "Error occurred while querying MediaStore:" + ex.getMessage(), Toast.LENGTH_LONG).show();
		}
		
		// Create our custom song adapter and bind songs from MediaStore to our listview
		ListView listView = (ListView) findViewById(R.id.songListView);
		if (musicCursor != null) {
			SongListAdapter mySongAdapter = new SongListAdapter(getApplicationContext(), musicCursor);
			listView.setAdapter(mySongAdapter);
			listView.setOnItemClickListener(musicgridlistener);
		}
		else {
			// Let user know no music found
			String  messageArray[]={"No music found."};
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, messageArray);
			listView.setAdapter(adapter);
		}
	}
	
	// ==========================================================
	// Function to convert milliseconds to minutes:seconds string
	// ==========================================================
	public String GetMinutesSeconds(int dur) {
		String MillisecondsToMinutesSeconds = "";
		int minutes = dur / 1000 / 60;
		int seconds = dur / 1000 % 60;
		MillisecondsToMinutesSeconds = String.format("%02d:%02d", minutes, seconds);
		
		return MillisecondsToMinutesSeconds;
	}
	
	// ===========================================================================================
	// Completion Listener called by media player when song has finished playing (plays next song)
	// ===========================================================================================
	private MediaPlayer.OnCompletionListener MusicFinishedCallbackListener = new MediaPlayer.OnCompletionListener() {
		   @Override
		   public void onCompletion(MediaPlayer mp)
		   {
			   currentSongIndex = currentSongIndex + 1; // move to next song
			   try {
				   musicCursor.moveToPosition(currentSongIndex);
				   songFilename = musicCursor.getString(music_column_index);
			   } catch (Exception e) {
				   // Couldn't move to the next song, so assume the list is done
				   songFilename = "";
				   mMediaPlayer = null;
				   ImageView playPauseButton = (ImageView) findViewById(R.id.PlayPauseButton);
				   playPauseButton.setImageResource(R.drawable.button_play);
	    			playPauseButtonInPlayState = true;
			   }
			   mp.reset();
               //Toast.makeText(getApplicationContext(), "setting data source to " + filename, Toast.LENGTH_LONG).show();
               if (songFilename != "") {
            	   try {
            		   mp.setDataSource(songFilename);
               		//Toast.makeText(getApplicationContext(), "preparing player...", Toast.LENGTH_LONG).show();
            		   mp.prepare();
            		   mp.start();
            	   } catch (Exception e) {
            		   String eMessage = e.getMessage();
            		   Toast.makeText(getApplicationContext(), "Error:" + e.getMessage() + ", cause:" + e.getCause() + ", e:" + e, Toast.LENGTH_LONG).show();
            	   }
               }
		   }
	   };
	
	// =============================================
	// Function to load album art for visible songs
	// =============================================
	public void LoadAlbumArt() {
		ListView songList = (ListView) findViewById(R.id.songListView);
		int FirstVisibleSongIndex = 1;//songList.getFirstVisiblePosition();
		int LastVisibleSongIndex = 10;//songList.getLastVisiblePosition();
		Drawable img;
		ImageView ivAlbumArt;
		Cursor AlbumIDCursor;
		long AlbumID;
		String AlbumIDSelect;
		Bitmap bitmap = null;
		

		String[] projection = {
				MediaStore.Audio.Albums._ID,
				MediaStore.Audio.Albums.ALBUM_ART
		};

		for (int i=FirstVisibleSongIndex; i<LastVisibleSongIndex; i++) {
			musicCursor.move(i);
			// Get Album ID for this song (if any)
			AlbumID = musicCursor.getLong(musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
			final Uri ART_CONTENT_URI = Uri.parse("content://media/external/audio/albumart");
	        Uri albumArtUri = ContentUris.withAppendedId(ART_CONTENT_URI, AlbumID);

	        try {
	            bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), albumArtUri);
	            ivAlbumArt=(ImageView)findViewById(R.id.thumbnail);
			    ivAlbumArt.setImageBitmap(bitmap);
	        } catch (Exception exception) {
	            // log error
	        }
	        
	        //songList.postInvalidate(); // to redraw the view
			//AlbumIDSelect = MediaStore.Audio.Albums._ID + "=" + AlbumID;
			//AlbumIDCursor = getContentResolver().query(
		    //        MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
		    //        projection, AlbumIDSelect, null, null);
			//String artPath = AlbumIDCursor.getString(AlbumIDCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
			//img = Drawable.createFromPath(artPath);
		    //ivAlbumArt=(ImageView)findViewById(R.id.thumbnail);
		    //ivAlbumArt.setImageDrawable(img);
		}
	}

	// ===================================================================
	// Handle a song being clicked in the list (play it with media player)
	// ===================================================================
	 private OnItemClickListener musicgridlistener = new OnItemClickListener() {
         public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        	 //Toast.makeText(getApplicationContext(), "DEBUG: selected item " + position, Toast.LENGTH_LONG).show();
        	 v.setSelected(true);
        	 //v.setBackgroundColor(Color.CYAN);
               System.gc();
               music_column_index = musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
               SelectedSongID = musicCursor.getString(musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
               currentSongIndex = position;
               musicCursor.moveToPosition(currentSongIndex);
               songFilename = musicCursor.getString(music_column_index);
               // Change the album art to show song "selected" for playing
               ImageView ivAlbumArt=(ImageView)v.findViewById(R.id.thumbnail);
               MarkSongSelected(ivAlbumArt);
               try {
            	   // If the media player isn't created, create it
                   if (mMediaPlayer == null) {
                	   mMediaPlayer = new MediaPlayer();
                	   mMediaPlayer.setOnCompletionListener(MusicFinishedCallbackListener);
                   }

                   mMediaPlayer.reset();
                   //Toast.makeText(getApplicationContext(), "setting data source to " + filename, Toast.LENGTH_LONG).show();
                   if (songFilename != "") {
                   		mMediaPlayer.setDataSource(songFilename);
                   		//Toast.makeText(getApplicationContext(), "preparing player...", Toast.LENGTH_LONG).show();
                   		mMediaPlayer.prepare();
                   		mMediaPlayer.start();
                   		ImageView playPauseButton = (ImageView) findViewById(R.id.PlayPauseButton);
                   		playPauseButton.setImageResource(R.drawable.button_pause);
                   		playPauseButtonInPlayState = false;
                   }
               } catch (Exception e) {
            	   //AlertDialog alertDialog;
            	   //alertDialog = new AlertDialog.Builder(getApplicationContext()).create();
            	   //alertDialog.setTitle("Unable to Play Song");
            	   //alertDialog.setMessage("Error:" + e.getMessage());
            	   //alertDialog.show();
            	   Toast.makeText(getApplicationContext(), "Error:" + e.getMessage() + ", cause:" + e.getCause() + ", e:" + e, Toast.LENGTH_LONG).show();
               }
         }
   };
   
   private void MarkSongSelected(ImageView iv)
   {
	   if (iv != null) {
		   // If a previous song was selected, restore its album art
		   if (savedImageView != null) {
			   //savedImageView.setImageDrawable(savedAlbumArt);
		   }
       
		   //savedImageView = iv;
		   //savedAlbumArt = savedImageView.getDrawable();
		   //iv.setImageResource(R.drawable.ic_action_headphones);
		   //iv.startAnimation(AnimationUtils.loadAnimation(iv.getContext(), android.R.anim.fade_in));
	   }
   }
}
