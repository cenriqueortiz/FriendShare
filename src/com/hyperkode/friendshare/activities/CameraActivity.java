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

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
//import android.widget.ProgressBar;

public class CameraActivity extends Activity {
    private final static int REQUEST_PICTURE = 0;
    private ImageView capturedImage;
    //private ProgressBar titleProgressBar;
    private Activity mThis;
    
    /** 
     * Create Activity life-cycle method 
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mThis = this;
        setupScreen();
        setupButton();
    }

    /**
     * Set up the screen
     */
    private void setupScreen() {
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.cameraphoto);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
        //titleProgressBar = (ProgressBar) findViewById(R.id.title_progressbar);
    }    

    /**
     * Called when the Camera activity returns
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {    
         //String timestamp = Long.toString(System.currentTimeMillis());
         if (requestCode == REQUEST_PICTURE) {
             if (resultCode == Activity.RESULT_OK) {
                  Bundle b = intent.getExtras(); 
                  Bitmap pic = (Bitmap) b.get("data");
                  if (pic != null) {
                      capturedImage = (ImageView) this.findViewById(R.id.picture);
                      capturedImage.setImageBitmap(pic);
                      capturedImage.invalidate();
                  }
              } else if (resultCode == Activity.RESULT_CANCELED) {
                  // Activity canceled; in this case do nothing.
             }
         } else {
             super.onActivityResult(requestCode, resultCode, intent);
         }
    }    

    ///////////////////////////////////////////////////////////////////////////

    /**
     * Setup the button
     */
    private void setupButton() {
        Button takePhoto = (Button) findViewById(R.id.take_photo);
        takePhoto.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
              //String timestamp = Long.toString(System.currentTimeMillis());
              //String photoDir = Environment.getExternalStorageDirectory() + "data/friendshare" + timestamp; 
                Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);   
              //camera.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(photoDir)));
                mThis.startActivityForResult(camera, REQUEST_PICTURE);            
            }
        });
    }
    
}
