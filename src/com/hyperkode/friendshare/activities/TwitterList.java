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
import com.hyperkode.friendshare.db.DBAccess;
import com.hyperkode.friendshare.entities.FriendStatus;
import com.hyperkode.utils._2LevelImageDownloader;
//import com.hyperkode.friendshare.db.DBAccess;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.ResponseList;
import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class TwitterList extends Activity {
    private final String PREFS_NAME = "Prefs";
    private SharedPreferences mSettings;
    private String accessToken;
    private String accessTokenSecret;
    private ListView listView;
    private Twitter twitter4j;
    private String searchTerm;
    private ArrayList<FriendStatus> mFriendsStatus = new ArrayList<FriendStatus>();
    private FriendStatusAdapter mFriendStatusAdapter;
    private DBAccess dbAccess = null;
    
    private _2LevelImageDownloader imageDownloader;

    /**
     * Create Activity life-cycle method
     * @param savedInstanceState the saved instance
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null)
            searchTerm = savedInstanceState.getString("SearchTermString");
        setupScreen();
        setupDB();
        setupListView();
        setupTwitter();
        setupSearchButton();
        imageDownloader = new _2LevelImageDownloader();
    }
    
    /**
     * Destroy the Activity life-cycle method
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        dbAccess.closeDB();
    }
    
    /**
     * Save the current search term instance
     * @param savedInstanceState the instance state to save
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
      savedInstanceState.putString("SearchTermString", searchTerm);
      super.onSaveInstanceState(savedInstanceState);
    }   
    
    /////////////////////////////////////////////////////////////////////////////

    private void setupScreen() {
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.twitter_list);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
        setTitle(R.string.title_twitter_list);
        //titleProgressBar = (ProgressBar) findViewById(R.id.title_progressbar);
    }
    
    private void setupDB() {
        dbAccess = new DBAccess(this);
        dbAccess.openDB();
    }
    
    private void setupListView() {
        // Set the list view adapter
        listView = (ListView) findViewById(R.id.twitter_listview);

        // Set the header before setting the Adapter
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup header = (ViewGroup)inflater.inflate(R.layout.twitter_searchbox, listView, false);
        listView.addHeaderView(header, null, false);

        mFriendsStatus = dbAccess.selectAllFriendStatus();
        
        // Set the Adapter
        mFriendStatusAdapter  = new FriendStatusAdapter(this, mFriendsStatus, R.layout.twitter_list_item);

        listView.setAdapter(mFriendStatusAdapter);              
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,int position,  long id) {
            }
        });   
    }

    private void setupTwitter() {
        mSettings = getSharedPreferences(PREFS_NAME, 0);
        accessToken = mSettings.getString( "twitter_access_token", null );
        accessTokenSecret = mSettings.getString( "twitter_access_token_secret", null );  
        // Get Access Token and persist it  
        AccessToken at = new AccessToken(accessToken, accessTokenSecret);  
        // initialize Twitter4J  
        twitter4j = new TwitterFactory().getInstance();  
        twitter4j.setOAuthConsumer(
            getString(R.string.TWITTER_CONSUMER_KEY), 
            getString(R.string.TWITTER_CONSUMER_SECRET));
        twitter4j.setOAuthAccessToken(at);  
        
    }

    private void setupSearchButton() {
        Button searchButton = (Button) findViewById(R.id.twitter_searchbox_button);
        searchButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                EditText searchTextBox = (EditText) findViewById(R.id.twitter_searchbox_textbox);                
                searchTerm = searchTextBox.getText().toString();
                if (searchTerm.length() > 0)
                    searchTwitter(searchTerm);
                else
                    getHomeTimeline();
            }
        });
    }

    /**
     * Get the home timeline
     */
    private void getHomeTimeline() {
        new TwitterHometimelineTask().execute(this);
    }

    /**
     * Search Twitter
     * @param queryString the search term
     */
    private void searchTwitter(String queryString) {
        new TwitterSearchTask().execute(queryString);
    }

    /////////////////////////////////////////////////////////////////////////////

    /**
     * Define an generic Tweet/Status adapter for the twitter list
     */
    class FriendStatusAdapter extends BaseAdapter {
        //private Context context;
        private ArrayList<FriendStatus> friendsStatus;
        private int resourceId;
        
        public FriendStatusAdapter(Context context, ArrayList<FriendStatus> status, int resourceId) {
            //this.context = context;
            this.friendsStatus = status;
            this.resourceId = resourceId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (friendsStatus == null)
                return null;
            View rowView = convertView;
            if (rowView == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = vi.inflate(resourceId, null);
            }

            FriendStatus status = friendsStatus.get(position);

            // Get resources
            TextView nameTextView = (TextView) rowView.findViewById(R.id.twitterstatus_textview_name);
            if (nameTextView != null) {
                nameTextView.setText(status.getName());
            }

            TextView statusTextView = (TextView) rowView.findViewById(R.id.twitterstatus_textview_status);
            if (statusTextView != null) {
                statusTextView.setText(status.getStatus());
            }
            
            ImageView imageView = (ImageView) rowView.findViewById(R.id.twitterstatus_image);
            if (imageView != null) {
                String url = status.getImageUrl();
                imageView.setTag(url);
                imageDownloader.download(url, imageView);
                
                /*
                ImageDownloader.getDefaultInstance().displayImage(
                    TwitterList.this, url, imageView, -1, new Handler() {
                        @Override
                        public void handleMessage(Message message) {
                            if (mFriendStatusAdapter != null)
                                mFriendStatusAdapter.notifyDataSetChanged();
                        }
                    });
                 */
            }

            // Return row view
            return rowView;
        }

        public final int getCount() {
           if (friendsStatus == null)
               return 0;
            return friendsStatus.size();
        }

        public final FriendStatus getItem(int position) {
            if (friendsStatus == null)
                return null;
            return friendsStatus.get(position);
        }

        public final long getItemId(int position) {
            return position;
        }
    }
   
    /////////////////////////////////////////////////////////////////////////////
    
    /**
     * Background async task to retrieve the twitter hometimeline
     */
    class TwitterHometimelineTask extends AsyncTask<Context, Integer, Integer> {
        private ResponseList<twitter4j.Status> mStatuses;
        private ProgressDialog pd;

        protected void onPreExecute() {
            pd = ProgressDialog.show(
                    TwitterList.this, 
                    getString(R.string.please_wait),
                    getString(R.string.loading_twitter));
        }

        protected Integer doInBackground(Context... params) {
            try {
                mStatuses = twitter4j.getHomeTimeline();
            } catch (TwitterException e) {
                e.printStackTrace();
            }

            mFriendsStatus.clear();

            for (twitter4j.Status status : mStatuses) {
                FriendStatus fs = new FriendStatus();
                fs.setId(String.valueOf(status.getId()));
                fs.setName(status.getUser().getName());
                fs.setStatus(status.getText());
                fs.setImageUrl(status.getUser().getProfileImageURL().toString());
                mFriendsStatus.add(fs);
                dbAccess.insertFriendStatus(fs);
                System.out.println(status.getUser().getName() + ":" + status.getText());
            }
            return 0;
        }

        protected void onPostExecute(Integer result) {
            if (pd != null)
                pd.dismiss();
            if (mFriendStatusAdapter != null)
                mFriendStatusAdapter.notifyDataSetChanged();
        }
    }    

    /////////////////////////////////////////////////////////////////////////////
    
    /**
     * Background async task to search in twitter
     */
    class TwitterSearchTask extends AsyncTask<String, Integer, Integer> {
        private ProgressDialog pd;
        private List<Tweet> tweets;
        
        protected void onPreExecute() {
            pd = ProgressDialog.show(
                    TwitterList.this, 
                    getString(R.string.please_wait),
                    getString(R.string.searching_twitter));
        }

        protected Integer doInBackground(String... params) {
            if (params == null)
                return 0;
            String queryString = (String) params[0];
            if (queryString == null)
                return 0;
            if (queryString.length() == 0)
                return 0;
            
            // The factory instance is re-useable and thread safe.
            Twitter twitter = new TwitterFactory().getInstance();
            Query query = new Query(queryString);
            QueryResult result = null;
            try {
                result = twitter.search(query);
            } catch (TwitterException e) {
                e.printStackTrace();
            }

            tweets = result.getTweets();

            mFriendsStatus.clear();
            
            for (Tweet tweet : tweets) {
                FriendStatus fs = new FriendStatus();
                fs.setId(String.valueOf(tweet.getId()));
                fs.setName(tweet.getFromUser());
                fs.setStatus(tweet.getText());
                fs.setImageUrl(tweet.getProfileImageUrl());
                mFriendsStatus.add(fs);
                dbAccess.insertFriendStatus(fs);
                System.out.println(tweet.getFromUser() + ":" + tweet.getText());
            }
            return 0;
        }

        protected void onPostExecute(Integer result) {
            if (pd != null)
                pd.dismiss();
            if (mFriendStatusAdapter != null)
                mFriendStatusAdapter.notifyDataSetChanged();
        }
    }    
    
}
