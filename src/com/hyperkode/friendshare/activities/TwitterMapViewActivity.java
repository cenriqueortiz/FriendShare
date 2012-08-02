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

import java.io.IOException;
import java.util.List;

import twitter4j.GeoLocation;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

import com.hyperkode.friendshare.R;
import com.hyperkode.friendshare.map.TwitterItemizedOverlay;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class TwitterMapViewActivity extends MapActivity implements LocationListener {
    private final static String TAG = "TwitterMapViewActivity";
    private final String PREFS_NAME = "Prefs";
    private SharedPreferences mSettings;
    private String accessToken;
    private String accessTokenSecret;
    private MapView mapView;
    private MapController mapController;
    private List<Overlay> mapOverlays;
    protected ResponseList<Status> mStatuses = null;
    private Twitter twitter;
    private LocationManager locMgr;
    //private String locProvider;
    private Geocoder geocoder; 
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupScreen();
        setupMapView();
        setupTwitter();
        getHomeTimeline();
        setLocationManager();
        new GetLastLocTask().execute(this);
    }    
    
    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
    
    //////////////////////////////////////////////////////////////////////////////////
    
    // Location change Listener
    @Override
    public void onLocationChanged(Location location) {
        
        String text = String.format("Lat:\t %f\nLong:\t %f\nAlt:\t %f\nBearing:\t %f", location.getLatitude(), 
        location.getLongitude(), location.getAltitude(), location.getBearing());
        Log.i(TAG, text);
          
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 10);
            for (Address address : addresses) {
                Log.i(TAG, address.getAddressLine(0));
            }
        
            int lat = (int)(location.getLatitude() * 1E6);
            int lon = (int)(location.getLongitude() * 1E6);
            GeoPoint point = new GeoPoint(lat, lon);
            
            OverlayItem overlayitem = new OverlayItem(point, "Coordinates", String.format("Lat: %s, Lon: %s", Integer.toString(lat), Integer.toString(lon)));
            Drawable drawable = TwitterMapViewActivity.this.getResources().getDrawable(R.drawable.overlay_redx);
            TwitterItemizedOverlay itemizedoverlay = new TwitterItemizedOverlay(drawable, TwitterMapViewActivity.this);               
            itemizedoverlay.addOverlay(overlayitem);
            mapOverlays.add(itemizedoverlay);         
            mapController.animateTo(point);
            
          } catch (IOException e) {
              Log.e(TAG, "Could not get Geocoder data", e);
          }
    }    
    

    @Override
    public void onProviderDisabled(String provider) {
    }


    @Override
    public void onProviderEnabled(String provider) {
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }    

    //////////////////////////////////////////////////////////////////////////////////

    private void setupScreen() {
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.twitter_mapview);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
        setTitle(R.string.title_twitter_list);
        //titleProgressBar = (ProgressBar) findViewById(R.id.title_progressbar);
    }    

    private void setupMapView() {
        mapView = (MapView) findViewById(R.id.twitter_mapview_id);
        mapView.setBuiltInZoomControls(true);
        mapController = mapView.getController(); 
        mapOverlays = mapView.getOverlays();
        
    }
    
    private void setupTwitter() {
        mSettings = getSharedPreferences(PREFS_NAME, 0);
        accessToken = mSettings.getString( "twitter_access_token", null );
        accessTokenSecret = mSettings.getString( "twitter_access_token_secret", null );  
        // Get Access Token and persist it  
        AccessToken at = new AccessToken(accessToken, accessTokenSecret);  
        // initialize Twitter4J  
        twitter = new TwitterFactory().getInstance();  
        twitter.setOAuthConsumer(
            getString(R.string.TWITTER_CONSUMER_KEY), 
            getString(R.string.TWITTER_CONSUMER_SECRET));
        twitter.setOAuthAccessToken(at);
    }
    
    private void getHomeTimeline() {
        new TwitterTask().execute(this);
    }    
    
    private void setLocationManager() {
        geocoder = new Geocoder(this);
        locMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 10, this);
    }

    //////////////////////////////////////////////////////////////////////////////////
    
    // AsyncTask
    class GetLastLocTask extends AsyncTask<Context, Integer, Integer> {
        GeoPoint point;

        protected void onPreExecute() {
        }

        protected Integer doInBackground(Context... params) {
            
            if (locMgr == null)
                return 0;

            Location loc = locMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (loc == null)
                return 0; // Provider is not enabled
          
            double lat1e6 = loc.getLatitude()*1E6;
            double lon1e6 = loc.getLongitude()*1E6;
            point = new GeoPoint((int)lat1e6, (int)lon1e6);

            OverlayItem overlayitem = new OverlayItem(point, "Last Known Position", "You are here!");
            Drawable drawable = TwitterMapViewActivity.this.getResources().getDrawable(R.drawable.overlay_redx);
            TwitterItemizedOverlay itemizedoverlay = new TwitterItemizedOverlay(drawable, TwitterMapViewActivity.this);
            itemizedoverlay.addOverlay(overlayitem);
            mapOverlays.add(itemizedoverlay);       
            return 0;
        }

        protected void onPostExecute(Integer result) {
            if (point != null)
                mapController.animateTo(point);
            mapController.setZoom(30);
            mapView.invalidate();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////

    // AsyncTask
    class TwitterTask extends AsyncTask<Context, Integer, Integer> {

        protected void onPreExecute() {
        }

        protected Integer doInBackground(Context... params) {
            try {
                mStatuses = twitter.getHomeTimeline();
            } catch (TwitterException e) {
                e.printStackTrace();
            }

            GeoLocation g = null;
            for (twitter4j.Status status : mStatuses) {
                g = status.getGeoLocation();
                if(g != null) {
                    double lat = g.getLatitude();
                    double lon = g.getLongitude();
                    GeoPoint point = new GeoPoint(
                        (int) (lat * 1E6), 
                        (int) (lon * 1E6));
                    twitter4j.User user = status.getUser();
                    OverlayItem overlayitem = new OverlayItem(point, (user==null)?"":user.getName(), status.getText());
                    Drawable drawable = TwitterMapViewActivity.this.getResources().getDrawable(R.drawable.overlay_redx);
                    TwitterItemizedOverlay itemizedoverlay = new TwitterItemizedOverlay(drawable, TwitterMapViewActivity.this);               
                    itemizedoverlay.addOverlay(overlayitem);
                    mapOverlays.add(itemizedoverlay);       
                }
                System.out.println(status.getUser().getName() + ":" + status.getText());
            }
            return 0;
        }

        protected void onPostExecute(Integer result) {
            mapView.invalidate();
        }
    }
}
