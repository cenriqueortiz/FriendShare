/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hyperkode.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.HashMap;
import java.util.Stack;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class _3LevelImageDownloader {
    private static final String TAG = "ImageDownloader";
    private static final String DIR_PATH = "data/friendshare";
	private HashMap<String, SoftReference<Bitmap>> imageMap = new HashMap<String, SoftReference<Bitmap>>();
	private static _3LevelImageDownloader mDefaultInstance = null;
	private File cacheDir;
	//private Context mContext;
	private ImageQueue imageQueue = new ImageQueue();
	private Thread imageLoaderThread = new Thread(new ImageQueueManager());
	private int mDefaultIconId;
	
	private ProgressBar loadingSpinner;

	private _3LevelImageDownloader() {
		// Make background thread low priority, to avoid affecting UI performance
		imageLoaderThread.setPriority(Thread.NORM_PRIORITY-1);
		// Find the dir to save cached images
		String sdState = android.os.Environment.getExternalStorageState();
		if (sdState.equals(android.os.Environment.MEDIA_MOUNTED)) {
			File sdDir = android.os.Environment.getExternalStorageDirectory();		
			cacheDir = new File(sdDir,DIR_PATH);
		}

		if (cacheDir != null) {
	        if(!cacheDir.exists()) {
	            boolean ret = cacheDir.mkdirs();
	            if (!ret)
	                Log.d(TAG, "Cant make dirs: " + cacheDir.getPath());
	        }
		}

	}
	
	public static _3LevelImageDownloader getDefaultInstance() {
		if (mDefaultInstance == null) {
			mDefaultInstance = new _3LevelImageDownloader();
		}
		return mDefaultInstance;
	}
	

	public void displayImage(
	        Context ctx, 
	        String url, 
	        ImageView imageView, 
	        int defaultIconId,
	        Handler completionHandler) {
		if (url == null || imageView == null)
			return;
		this.mDefaultIconId = defaultIconId;
		addLoadingSpinnerView(ctx, imageView);
		if(imageMap.containsKey(url)) {
			imageView.setImageBitmap(imageMap.get(url).get());
		} else {
			queueImage(url, imageView, completionHandler);
		}
	}

    private void addLoadingSpinnerView(Context c, ImageView iv) {
        loadingSpinner = new ProgressBar(c);
        loadingSpinner.setIndeterminate(true);
        Drawable d = loadingSpinner.getIndeterminateDrawable();
        iv.setImageDrawable(d);
    }	
	
	private void queueImage(String url, ImageView imageView, Handler completionHandler) {
		// This ImageView might have been used for other images, so we clear 
		// the queue of old tasks before starting.
		try {
			imageQueue.removeAll(imageView);
		} catch (Exception e) {			
		}
		ImageRef p=new ImageRef(url, imageView, completionHandler);
		queueImage(p);
	}
	
		
	private void queueImage(ImageRef p) {
			
		synchronized(imageQueue.imageRefs) {
			imageQueue.imageRefs.push(p);
			imageQueue.imageRefs.notifyAll();
		}

		// Start thread if it's not started yet
		if(imageLoaderThread.getState() == Thread.State.NEW)
			imageLoaderThread.start();
	}

	private Bitmap getBitmap(String url) {
		String filename = String.valueOf(url.hashCode());
		File f = new File(cacheDir, filename);
		Bitmap bitmap = null;
		// Is the bitmap in our cache?
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inTempStorage = new byte[16*1024];
			//options.inSampleSize = 2;
			bitmap = BitmapFactory.decodeFile(f.getPath(), options);
			if(bitmap != null) {
				Log.d(TAG, "Loaded from Cachedir: " + url + " File: " + f.getPath());			
				return bitmap;
			}
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			System.gc();
			return null;
		}

		// Nope, have to download it
		try {
			Log.d(TAG, "Downloading: " + url);
			bitmap = BitmapFactory.decodeStream(new URL(url).openConnection().getInputStream());
			// save bitmap to cache for later
			writeFile(bitmap, f);
			return bitmap;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		} catch (OutOfMemoryError e) {
        	Log.e(TAG, "Image Downloader (130) Out Of Memory Error " + e.getLocalizedMessage());
        	System.gc();
        }
		return null;
	}

	private void writeFile(Bitmap bmp, File f) {
		FileOutputStream out = null;

		try {
			out = new FileOutputStream(f);
			bmp.compress(Bitmap.CompressFormat.PNG, 80, out);
			Log.d(TAG, "Wrote to Cachedir: ?? File: " + f.getPath());			

		} catch (Exception e) {
			e.printStackTrace();
		}
		finally { 
			try { if (out != null ) out.close(); }
			catch(Exception ex) {} 
		}
	}

	/** Classes **/

	private class ImageRef {
		public String url;
		public ImageView imageView;
		public int retries;
		public Handler completionHandler;
		
		public static final int maxRetries = 5;

		public ImageRef(String u, ImageView i, Handler completionHandler) {
			url=u;
			imageView=i;
			retries = 0;
			this.completionHandler = completionHandler;
		}
	}

	//stores list of images to download
	private class ImageQueue {
		private Stack<ImageRef> imageRefs = new Stack<ImageRef>();

		// removes all instances of this ImageView
		public void removeAll(ImageView view) {

			for(int i = 0 ;i < imageRefs.size();) {
				if(imageRefs.get(i).imageView == view)
					imageRefs.remove(i);
				else ++i;
			}
		}
	}

	private class ImageQueueManager implements Runnable {
		@Override
		public void run() {
			try {
				while(true) {
					// Thread waits until there are images in the 
					// queue to be retrieved
					if(imageQueue.imageRefs.size() == 0) {
						synchronized(imageQueue.imageRefs) {
							imageQueue.imageRefs.wait();
						}
					}

					// When we have images to be loaded
					if(imageQueue.imageRefs.size() != 0) {
						ImageRef imageToLoad;

						synchronized(imageQueue.imageRefs) {
							imageToLoad = imageQueue.imageRefs.pop();
						}

						Bitmap bmp = getBitmap(imageToLoad.url);
						if (bmp != null) {
							imageMap.put(imageToLoad.url, new SoftReference<Bitmap>(bmp));
							Object tag = imageToLoad.imageView.getTag();
	
							// Make sure we have the right view - thread safety defender
							if(tag != null && ((String)tag).equals(imageToLoad.url)) {
								BitmapDisplayer bmpDisplayer = 
									new BitmapDisplayer(bmp, imageToLoad.imageView, imageToLoad.completionHandler);
	
								Activity a = (Activity)imageToLoad.imageView.getContext();
								a.runOnUiThread(bmpDisplayer);
							}
						} else {
							// Queue it up again
							if (imageToLoad.retries < ImageRef.maxRetries) {
								imageToLoad.retries++;
								queueImage(imageToLoad);
							}
						}
					}

					if(Thread.interrupted())
						break;
				}
			} catch (InterruptedException e) {}
		}
	}

	//Used to display bitmap in the UI thread
	private class BitmapDisplayer implements Runnable {
		Bitmap bitmap;
		ImageView imageView;
		Handler completionHandler;

		public BitmapDisplayer(Bitmap b, ImageView i, Handler completionHandler) {
			bitmap=b;
			imageView=i;
			this.completionHandler = completionHandler;
		}

		public void run() {
			if(bitmap != null) {
				imageView.setImageBitmap(bitmap);
			} else {
			    if (mDefaultIconId != -1)
			        imageView.setImageResource(mDefaultIconId);
			}
			if (completionHandler != null) {
			    completionHandler.sendEmptyMessage(1);
			}
		}
	}
}
