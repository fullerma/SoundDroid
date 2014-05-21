package com.example.sounddroid;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.util.LruCache;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class SongListAdapter extends CursorAdapter {
	
	public SongListAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }
	
	// Get max available VM memory, exceeding this amount will throw an
    // OutOfMemory exception. Stored in kilobytes as LruCache takes an
    // int in its constructor.
    final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

    // Use 1/8th of the available memory for this memory cache.
    final int cacheSize = maxMemory / 8;
    
	final LruCache<String, Bitmap> mMemoryCache = null; // for storing bitmap cache for album art
    //mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
    //    @Override
    //    protected int sizeOf(String key, Bitmap bitmap) {
    //        // The cache size will be measured in kilobytes rather than
    //        // number of items.
    //        return bitmap.getByteCount() / 1024;
    //    }
    //};
	
	final class ViewHolder  {
        int titleIndex;
        int artistIndex;
        int durationIndex;
        int albumidIndex;
        TextView title;
        TextView artist;
        TextView duration;
        ImageView thumbnail;
   }

	@Override
	public void bindView(View aView, Context aContext, Cursor aCursor) {
		//View v = aView.getRootView();
		String selectedSongID = ActivitySongs.SelectedSongID;
		String songID = aCursor.getString(aCursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
		ViewHolder holder = (ViewHolder)aView.getTag();
        holder.title.setText(aCursor.getString(holder.titleIndex));
        holder.artist.setText(aCursor.getString(holder.artistIndex));
        holder.duration.setText(GetMinutesSeconds(aCursor.getInt(holder.durationIndex)));
        long AlbumID = aCursor.getLong(holder.albumidIndex);
        
        // Set the album art image
        //long AlbumID = aCursor.getLong(aCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
        try {
        	//if (songID.equals(selectedSongID)) {
        		//holder.thumbnail.setImageResource(R.drawable.ic_action_headphones);
        		//holder.thumbnail.setVisibility(View.VISIBLE);
        	//} else {
        		final Uri ART_CONTENT_URI = Uri.parse("content://media/external/audio/albumart");
        		Uri albumArtUri = ContentUris.withAppendedId(ART_CONTENT_URI, AlbumID);
        		//holder.thumbnail.setTag(AlbumID); // so the async process knows if we're dealing with the right view
        		holder.thumbnail.setTag(albumArtUri);
        		BitmapWorkerTask.loadBitmap(aContext, albumArtUri, holder.thumbnail);
        		//new ImageLoader(holder.thumbnail, albumArtUri).execute();
//        		Bitmap bitmap = MediaStore.Images.Media.getBitmap(aContext.getContentResolver(), albumArtUri);
//        		holder.thumbnail.setImageBitmap(bitmap);
//        		//ImageView ivAlbumArt=(ImageView)aView.findViewById(R.id.thumbnail);
//        		//ivAlbumArt.setImageBitmap(bitmap);
        	//}
        } catch (Exception ex) {
        	// Ignore errors
        }
	}
	 
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
	    final View view = LayoutInflater.from(context).inflate(R.layout.layout_song, parent, false);
	    ViewHolder holder = new ViewHolder();
	    holder.titleIndex = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
	    holder.artistIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
	    holder.durationIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
	    holder.albumidIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
	    holder.title = (TextView)view.findViewById(R.id.title);
	    holder.artist = (TextView)view.findViewById(R.id.artist);
	    holder.duration = (TextView)view.findViewById(R.id.duration);
	    holder.thumbnail = (ImageView)view.findViewById(R.id.thumbnail);
	    view.setTag(holder);
	    return view;
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
	
	// ===================
	// Add bitmap to cache
	// ===================
	public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
	    if (getBitmapFromMemCache(key) == null) {
	        mMemoryCache.put(key, bitmap);
	    }
	}

	// =====================
	// Get bitmap from cache
	// =====================
	public Bitmap getBitmapFromMemCache(String key) {
	    return mMemoryCache.get(key);
	}
}