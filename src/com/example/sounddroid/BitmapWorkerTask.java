package com.example.sounddroid;

import java.lang.ref.WeakReference;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

class BitmapWorkerTask extends AsyncTask<Uri, Void, Bitmap> {
    protected final WeakReference<ImageView> imageViewReference;
    protected Uri uri = null;
    private Boolean enable_animation = true;

    public BitmapWorkerTask(ImageView imageView) {
        // Use a WeakReference to ensure the ImageView can be garbage collected
        imageViewReference = new WeakReference<ImageView>(imageView);
    }

    // Decode image in background.
    @Override
    protected Bitmap doInBackground(Uri... params) {
        uri = (Uri)params[0];
        Bitmap bitmap = null;
        ImageView imageView;
        if (imageViewReference != null) {
        	imageView = imageViewReference.get();
        	if (((AsyncDrawable)imageView.getDrawable()).getBitmap() == null) {
        		try {
        			bitmap = MediaStore.Images.Media.getBitmap(imageView.getContext().getContentResolver(), uri);
        		}
        		catch (Exception ex)
        		{
        		}
        	}
        }
        return (bitmap);
    }

    @Override
    protected void onPreExecute() {
    	super.onPreExecute();
        if (enable_animation && imageViewReference != null) {
        	final ImageView imageView = imageViewReference.get();
        	imageView.startAnimation(AnimationUtils.loadAnimation(imageView.getContext(), android.R.anim.fade_out));
        	imageView.setVisibility(View.INVISIBLE);
        }
    }
    
    // Once complete, see if ImageView is still around and set bitmap.
    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (isCancelled()) {
            bitmap = null;
        }

        if (imageViewReference != null && bitmap != null) {
            final ImageView imageView = imageViewReference.get();
            final BitmapWorkerTask bitmapWorkerTask =
                    getBitmapWorkerTask(imageView);
            if (this == bitmapWorkerTask && imageView != null) {
                imageView.setImageBitmap(bitmap);
                imageView.setVisibility(View.VISIBLE);
                if (enable_animation)
                	imageView.startAnimation(AnimationUtils.loadAnimation(imageView.getContext(), android.R.anim.fade_in));
            }
        } else {
        	if (imageViewReference != null)
        		imageViewReference.get().setImageResource(R.drawable.whiteflag);
        	final ImageView imageView = imageViewReference.get();
        	imageView.setVisibility(View.VISIBLE);
        	imageView.startAnimation(AnimationUtils.loadAnimation(imageView.getContext(), android.R.anim.fade_in));
        }
    }
    
    public static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
    	if (imageView != null) {
    		final Drawable drawable = imageView.getDrawable();
    		if (drawable instanceof AsyncDrawable) {
    			final AsyncDrawable asyncDrawable = (AsyncDrawable)drawable;
    			return asyncDrawable.getBitmapWorkerTask();
    		}
    	}
    	return null;
	}
    
    // ================================================================
 	// Load a bitmap into the specified ImageView per the specified URI
 	// ================================================================
 	public static void loadBitmap(Context context, Uri uri, ImageView imageView) {
 	    if (cancelPotentialWork(uri, imageView)) {
 	        final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
 	        Bitmap mPlaceHolderBitmap = null; // no default bitmap while image is loaded
 	        final AsyncDrawable asyncDrawable =
 	                new AsyncDrawable(context.getResources(), mPlaceHolderBitmap, task);
 	        imageView.setImageDrawable(asyncDrawable);
 	        task.execute(uri);
 	    }
 	}
 	
 	public static boolean cancelPotentialWork(Uri uri, ImageView imageView) {
 	    final BitmapWorkerTask bitmapWorkerTask = BitmapWorkerTask.getBitmapWorkerTask(imageView);

 	    if (bitmapWorkerTask != null) {
 	        final Uri bitmapUri = bitmapWorkerTask.uri;
 	        // If bitmapData is not yet set or it differs from the new data
 	        if (bitmapUri == null || bitmapUri != uri) {
 	            // Cancel previous task
 	            bitmapWorkerTask.cancel(true);
 	        } else {
 	            // The same work is already in progress
 	            return false;
 	        }
 	    }
 	    // No task associated with the ImageView, or an existing task was cancelled
 	    return true;
 	}
 	
    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap,
                BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference =
                new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }
}