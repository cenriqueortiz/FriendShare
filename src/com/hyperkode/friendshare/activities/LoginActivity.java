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

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
//import android.widget.ProgressBar;
import android.widget.Toast;

public class LoginActivity extends Activity {
    // Prefs
    private final String PREFS_NAME = "Prefs";
    private SharedPreferences mSettings;
    
    //private ProgressBar titleProgressBar;
    private Activity mThis;
    private CheckBox cb;
    private Button loginButton;
    private Button cancelButton;
    private Twitter mTwitter;
    private RequestToken mRequestToken;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mThis = this;
        mSettings = getSharedPreferences(PREFS_NAME, 0);
        setupScreen();
        setupButtons();
        setupTwitterCheckBox();
    }

    /**
     * Setup the screen
     */
    private void setupScreen() {
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.login);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
        //titleProgressBar = (ProgressBar) findViewById(R.id.title_progressbar);
    }

    private void setupButtons() {
        // Login button handler
        loginButton = (Button) findViewById(R.id.loginbutton_continue);
        loginButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                EditText usernameText = (EditText) findViewById(R.id.login_username_textbox);
                String un = usernameText.getText().toString();

                if (cb != null && cb.isChecked()) {
                    // do Twitter login
                    authTwitter();
                    return;
                }
                
                if (un == null || un.length() == 0) {
                    Toast toast = Toast.makeText(mThis, mThis.getString(R.string.enter_username), Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL, 0, 0);
                    toast.show();       
                    return;
                }

                //EditText passwordText = (EditText) findViewById(R.id.login_password_textbox);
                //String pw = passwordText.getText().toString();
                Intent i = new Intent(mThis, LandingActivity.class);
                mThis.startActivity(i);
                            
            }
        });

        // Cancel button handler
        cancelButton = (Button) findViewById(R.id.loginbutton_cancel);
        cancelButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Exit screen
                mThis.finish();
            }
        });    
    }
    
    private void setupTwitterCheckBox() {
        cb = (CheckBox) findViewById(R.id.login_twitter_checkbox);
        cb.setChecked(true);
        cb.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            }
        });
    }
    
    private void showMessage(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg).setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        //
        builder.create().show();
    }

    private void authTwitter() {
        // Twitter mTwitter and RequestToken mRequestToken
        // are private members of this activity
        mTwitter = new TwitterFactory().getInstance();
        mRequestToken = null;
        mTwitter.setOAuthConsumer(
                getString(R.string.TWITTER_CONSUMER_KEY), 
                getString(R.string.TWITTER_CONSUMER_SECRET));
        String callbackURL = getString(R.string.TWITTER_CALLBACK_URL);
        try {
            mRequestToken = mTwitter.getOAuthRequestToken(callbackURL);
        } catch (TwitterException e) {
            e.printStackTrace();
        }
        Intent i = new Intent(mThis, TwitterWebViewActivity.class);
        i.putExtra("URL", mRequestToken.getAuthenticationURL());
        startActivityForResult(i, TwitterWebViewActivity.TWITTER_AUTH);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            String oauthVerifier = (String) data.getExtras().get("oauth_verifier");
            AccessToken at = null;
            try {
                // Pair up our request with the response
                at = mTwitter.getOAuthAccessToken(mRequestToken, oauthVerifier);
                
                if (at != null) {
                    mSettings.edit()
                        .putString( "twitter_access_token", at.getToken() )
                        .putString( "twitter_access_token_secret", at.getTokenSecret() )
                        .commit();
                    Intent i = new Intent(mThis, LandingActivity.class);
                    mThis.startActivity(i);
                } else {
                    showMessage("Twitter Authentication failed");
                }
            }
            catch (TwitterException e) {
                e.printStackTrace();
            }
        }        
    }
    
}
