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

package com.hyperkode.friendshare.fragment;

import com.hyperkode.friendshare.R;
import java.util.ArrayList;
import android.app.Activity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
//import android.widget.ProgressBar;
import android.widget.ProgressBar;

public class LandingFragment extends Fragment {
  //private ProgressBar titleProgressBar;
    private Activity mThisActivity;
    private View mThisView = null;
    private static LandingFragment instance;
  //private ArrayList<ImageView> imageViews = new ArrayList<ImageView>();
    private int []mImagesRes = new int[]{
        R.drawable.camera,
        R.drawable.map,
        R.drawable.twitter_newbird_blue_sm,
//      R.drawable.images,
    };
    private static final int CAMERA_POS  = 0;
    private static final int MAP_POS     = 1;
    private static final int TWITTER_POS = 2;
//  private int IMAGES_POS  = 3;
    private ProgressBar titleProgressBar;

    
    public LandingFragment() {}

    public static LandingFragment newInstance() {
        if (instance == null) {
            instance = new LandingFragment();
        }
        return instance;
    }    

    @Override
    public View onCreateView(
            LayoutInflater inflater, 
            ViewGroup container,
            Bundle savedInstanceState) {
        mThisActivity = this.getActivity();
        return inflater.inflate(R.layout.landing, container, false);
    }    

    /** Called when the activity is first created. */
    @Override
    public void onResume() {
        super.onResume();
        mThisActivity = this.getActivity();
        setupGridView();
        titleProgressBar = (ProgressBar) this.getActivity().findViewById(R.id.title_progressbar);
        if (titleProgressBar != null)
            titleProgressBar.setVisibility(View.INVISIBLE);
    }    
    
    private void setupGridView() {        
        GridView grid = (GridView) this.getActivity().findViewById(R.id.gridview);
        grid.setAdapter(new GridAdapter(this.getActivity(), mImagesRes));
        grid.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                Intent i = null;
                switch(pos) {
                    case CAMERA_POS:
                      //FragmentManager fragmentManager = LandingFragment.this.getActivity().getFragmentManager();
                        FragmentManager fm = LandingFragment.this.getActivity().getSupportFragmentManager();
                        FragmentTransaction ft = fm.beginTransaction();
                        ft.add(R.id.main_view_containter, LandingFragment.this);
                        ft.addToBackStack(null);
                        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                        ft.commit();
                        break;

                    case MAP_POS:
                        //i = new Intent(mThis, TwitterMapViewActivity.class);
                        //mThis.startActivity(i);
                        break;

                    case TWITTER_POS:
                        //i = new Intent(mThis, TwitterList.class);
                        //mThis.startActivity(i);
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
