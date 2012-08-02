/*
 *  Copyright (C) 2010-2012 C. Enrique Ortiz <enrique.ortiz@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.    
 */

package com.hyperkode.friendshare.activities;

import com.hyperkode.friendshare.R;
import java.io.OutputStream;
import java.util.List;
//import java.text.SimpleDateFormat;
import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Audio.Media;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
//import android.widget.ProgressBar;

public class CameraActivity2 extends Activity implements SurfaceHolder.Callback {
    private static final String TAG = "CameraActivity2";
    //private ProgressBar titleProgressBar;
    //private Activity mThis;    
    private Camera camera;
    private boolean isPreviewRunning = false;
    //private SimpleDateFormat timeStampFormat = new SimpleDateFormat("yyyyMMddHHmmssSS");
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Uri targetResource = Media.EXTERNAL_CONTENT_URI;    
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //mThis = this;
        setupScreen();
        setupButton();
    }

    /**
     * Set up the screen
     */
    private void setupScreen() {
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.cameraphoto2);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
        //titleProgressBar = (ProgressBar) findViewById(R.id.title_progressbar);
        this.surfaceView = (SurfaceView) findViewById(R.id.surface);
        this.surfaceHolder = this.surfaceView.getHolder();
        this.surfaceHolder.addCallback(this);
        //this.surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);        
    }    

    private void setupButton() {
        Button takePhoto = (Button) findViewById(R.id.take_photo);
        takePhoto.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                try {
                    takePicture();                    
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                    e.printStackTrace();
                }                
            }
        });
    }

    public boolean takePicture() {
        ImageCaptureCallback cam = null;
            /*
            try {
                String filename = this.timeStampFormat.format(new Date());
                ContentValues values = new ContentValues();
                values.put(MediaColumns.TITLE, filename);
                values.put(ImageColumns.DESCRIPTION, "Beginning Android");
                Uri uri = getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, values);
                cam = new ImageCaptureCallback(getContentResolver().openOutputStream(uri));
            } catch (Exception ex) {
                Log.e(TAG, ex.toString());
                ex.printStackTrace();
            }
            */
            cam = new ImageCaptureCallback(null);
            this.camera.takePicture(this.mShutterCallback, this.mPictureCallbackRaw, cam);
            return true;
    }    
    
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        MenuItem item = menu.add(0, 0, 0, "View Pictures");
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(Intent.ACTION_VIEW, CameraActivity2.this.targetResource);
                startActivity(intent);
                return true;
            }
        });
        return true;
    }    

    Camera.PictureCallback mPictureCallbackRaw = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera c) {
            CameraActivity2.this.camera.startPreview();
        }
    };

    Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {
        }
    };

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        if (this.isPreviewRunning) {
            this.camera.stopPreview();
        }
        Camera.Parameters p = this.camera.getParameters();
        List<Camera.Size> previewSizes = p.getSupportedPreviewSizes();
        Camera.Size cs = previewSizes.get(7);
        try {
            p.setPreviewSize(cs.width, cs.height);
//          p.setPreviewSize(w, h);
            this.camera.setParameters(p);
            this.camera.setPreviewDisplay(holder);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.camera.startPreview();
        this.isPreviewRunning = true;
    }

    public void surfaceCreated(SurfaceHolder holder) {
        this.camera = Camera.open();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        this.camera.stopPreview();
        this.isPreviewRunning = false;
        this.camera.release();
    }
    
    public class ImageCaptureCallback implements PictureCallback {

        private OutputStream filoutputStream;

        public ImageCaptureCallback(OutputStream filoutputStream) {
            this.filoutputStream = filoutputStream;
        }

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            if (filoutputStream == null) {
                return;
            }
            try {
                this.filoutputStream.write(data);
                this.filoutputStream.flush();
                this.filoutputStream.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }
    
}
