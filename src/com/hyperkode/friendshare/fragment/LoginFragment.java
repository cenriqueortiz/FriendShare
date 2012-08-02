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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
//import android.widget.ProgressBar;
import android.widget.Toast;

public class LoginFragment extends Fragment {
    private static LoginFragment instance;
    private Activity mThisActivity;
    private View mThisView = null;
    private ProgressBar titleProgressBar;

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

    /** newInstance method */
    public static LoginFragment newInstance() {
        if (instance == null) {
            instance = new LoginFragment();
        }
        return instance;
    }    

    /** Empty default constructor */
    public LoginFragment() {}
    
    @Override
    public View onCreateView(
            LayoutInflater inflater, 
            ViewGroup container,
            Bundle savedInstanceState) {
        mThisActivity = this.getActivity();
        return inflater.inflate(R.layout.login, container, false);
    }    
    
    
    /** Called when the activity is first created. */
    @Override
    public void onResume() {
        super.onResume();
        titleProgressBar = (ProgressBar) this.getActivity().findViewById(R.id.title_progressbar);
        mThisActivity = this.getActivity();
        mSettings = mThisActivity.getSharedPreferences(PREFS_NAME, 0);
        setupButtons();
        setupTwitterCheckBox();
    }

    private void setupButtons() {
        // Login button handler
        loginButton = (Button) mThisActivity.findViewById(R.id.loginbutton_continue);
        loginButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                EditText usernameText = (EditText) mThisActivity.findViewById(R.id.login_username_textbox);
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
                //Intent i = new Intent(mThis, LandingActivity.class);
                //mThis.startActivity(i);
                   
                /*
                FragmentManager fm = LoginFragment.this.getActivity().getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.add(R.id.main_view_containter, LoginFragment.this);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.addToBackStack(null);
                ft.commit();
                */
                
            }
        });

        // Cancel button handler
        cancelButton = (Button) mThisActivity.findViewById(R.id.loginbutton_cancel);
        cancelButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Exit screen
                mThis.finish();
            }
        });    
    }
    
    private void setupTwitterCheckBox() {
        cb = (CheckBox) mThisActivity.findViewById(R.id.login_twitter_checkbox);
        cb.setChecked(true);
        cb.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            }
        });
    }
    
    private void showMessage(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mThisActivity);
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
        if (titleProgressBar != null)
            titleProgressBar.setVisibility(View.VISIBLE);

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
        

        TwitterWebViewFragment twbf = TwitterWebViewFragment.newInstance();
        Bundle args = new Bundle();
        args.putString("URL", mRequestToken.getAuthenticationURL());
        twbf.setArguments(args);
        FragmentManager fragmentManager = LoginFragment.this.getActivity().getSupportFragmentManager();
        fragmentManager.putFragment(args, "LoginFragment", LoginFragment.this);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.main_view_containter, twbf);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        
    }
    
    public void setOAuthVerifierResult(String oauthVerifier) {
        AccessToken at = null;
        try {
            // Pair up our request with the response
            at = mTwitter.getOAuthAccessToken(mRequestToken, oauthVerifier);
            
            if (at != null) {
                mSettings.edit()
                    .putString( "twitter_access_token", at.getToken() )
                    .putString( "twitter_access_token_secret", at.getTokenSecret() )
                    .commit();
                
                LandingFragment landingFragment = LandingFragment.newInstance();
                Bundle args = new Bundle();
                FragmentManager fragmentManager = LoginFragment.this.getActivity().getSupportFragmentManager();
                //fragmentManager.putFragment(args, "LoginFragment", LoginFragment.this);
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.add(R.id.main_view_containter, landingFragment);
                fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                
            } else {
                showMessage("Twitter Authentication failed");
            }
        }
        catch (TwitterException e) {
            e.printStackTrace();
        }
        if (titleProgressBar != null)
            titleProgressBar.setVisibility(View.INVISIBLE);
    }
    
}
