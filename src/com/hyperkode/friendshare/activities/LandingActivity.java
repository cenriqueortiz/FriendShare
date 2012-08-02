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
import java.util.ArrayList;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
//import android.widget.ProgressBar;

public class LandingActivity extends Activity {
  //private ProgressBar titleProgressBar;
    private Activity mThis;
  //private ArrayList<ImageView> imageViews = new ArrayList<ImageView>();
    private int []mImagesRes = new int[]{
        R.drawable.camera_icon,
        R.drawable.map_icon1,
        R.drawable.twitter_newbird_blue_sm,
//      R.drawable.search,
//      R.drawable.images,
    };
    private static final int CAMERA_POS  = 0;
    private static final int MAP_POS     = 1;
    private static final int TWITTER_POS = 2;
//  private int SEARCH_POS  = 2;
//  private int IMAGES_POS  = 3;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mThis = this;
        setupScreen();
        setupGridView();
    }

    /**
     * Set up the screen
     */
    private void setupScreen() {
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.landing);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
      //titleProgressBar = (ProgressBar) findViewById(R.id.title_progressbar);
    }

    private void setupGridView() {        
        GridView grid = (GridView) findViewById(R.id.gridview);
        grid.setAdapter(new GridAdapter(this, mImagesRes));
        grid.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                Intent i = null;
                switch(pos) {
                    case CAMERA_POS:
                        i = new Intent(mThis, CameraActivity.class);
                        mThis.startActivity(i);
                        break;
                    case MAP_POS:
                        i = new Intent(mThis, TwitterMapViewActivity.class);
                        mThis.startActivity(i);
                        break;

                    case TWITTER_POS:
                        i = new Intent(mThis, TwitterList.class);
                        mThis.startActivity(i);
                        break;
                }
            }
        });
    }
    
    //////////////////////////////////////////////////////////////////////////////
    
    public class GridAdapter extends BaseAdapter {
      //private Context context;
        private int[] imageResIds;
        private ArrayList<ImageView> imageViews;
        
        public GridAdapter(Context context, int[] imageResIds) {
            //this.context = context;
            this.imageResIds = imageResIds;
            imageViews = new ArrayList<ImageView>(imageResIds.length);   
            
            for (int i=0; i<imageResIds.length; i++) {
                ImageView iv = new ImageView(context);
                float density = getResources().getDisplayMetrics().density;
                iv.setScaleType(ImageView.ScaleType.FIT_CENTER);     
                // see http://developer.android.com/guide/practices/ui_guidelines/icon_design.html
                final int w = (int) (48 * density + 0.5f);
                iv.setLayoutParams(new GridView.LayoutParams(w * 2, w * 2));
                
                iv.setImageResource(imageResIds[i]);
                imageViews.add(iv);
            }
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            return imageViews.get(position);
        }

        public final int getCount() {
            return imageResIds.length;
        }

        public final View getItem(int position) {
            return imageViews.get(position);
        }

        public final long getItemId(int position) {
            return position;
        }
    }   
}
