package com.example.sounddroid;
import java.lang.ref.WeakReference;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class ImageLoader extends AsyncTask<Object, String, Bitmap> {
    private WeakReference<ImageView> albumArtViewReference;
    private Bitmap bitmap = null;
    private String viewTag;
    private Boolean enable_animation = true;
    private ImageView albumArtView = null;
    private int mPosition;
    private Uri imageUri;
    
    public ImageLoader(ImageView view, Uri uri) {
    	albumArtViewReference = new WeakReference<ImageView>(view);
    	mPosition = view.getId();
    	imageUri = uri;
    	viewTag = view.getTag().toString();
    }

    @Override
    protected Bitmap doInBackground(Object... parameters) {
    	if (albumArtViewReference != null) {
    		albumArtView = albumArtViewReference.get();
    		bitmap = null;
    		//if (albumArtView.isShown()) { // Only get album art for visible views
    			try {
    				bitmap = MediaStore.Images.Media.getBitmap(albumArtView.getContext().getContentResolver(), imageUri);
    			} catch (Exception ex)
    			{
    			}
    		//}
    	}
        return bitmap;
    }

    @Override
    protected void onPreExecute() {
    	super.onPreExecute();
        if (enable_animation && albumArtViewReference != null) {
        	albumArtView = albumArtViewReference.get();
        	albumArtView.startAnimation(AnimationUtils.loadAnimation(albumArtView.getContext(), android.R.anim.fade_out));
        	albumArtView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
    	super.onPostExecute(bitmap);
    	if (isCancelled()) {
    		bitmap = null;
    	}
        if (bitmap != null && albumArtViewReference != null) {
        	albumArtView = albumArtViewReference.get();
        	//if (albumArtView.getId() == mPosition) {
        	if (albumArtView.getTag().toString() == viewTag) {
        		albumArtView.setImageBitmap(bitmap);
        		albumArtView.setVisibility(View.VISIBLE);
        		if (enable_animation)
        			albumArtView.startAnimation(AnimationUtils.loadAnimation(albumArtView.getContext(), android.R.anim.fade_in));
        	}
        } else {
        	if (albumArtViewReference != null)
        		albumArtViewReference.get().setImageResource(R.drawable.whiteflag);
        	albumArtView.setVisibility(View.VISIBLE);
        	albumArtView.startAnimation(AnimationUtils.loadAnimation(albumArtView.getContext(), android.R.anim.fade_in));
        }
    }
}