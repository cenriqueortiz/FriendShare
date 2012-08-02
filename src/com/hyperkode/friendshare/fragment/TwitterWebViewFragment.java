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

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class TwitterWebViewFragment extends Fragment {
    public static final int TWITTER_AUTH = 01;
    private static TwitterWebViewFragment instance;
    private Activity mThisActivity;
    private View mThisView = null;
    private Intent mIntent;
    private LoginFragment loginFragment;

    public static TwitterWebViewFragment newInstance() {
        if (instance == null) {
            instance = new TwitterWebViewFragment();
        }
        return instance;
    }    

    public TwitterWebViewFragment() {}
    
    @Override
    public View onCreateView(
            LayoutInflater inflater, 
            ViewGroup container,
            Bundle savedInstanceState) {
        mThisActivity = this.getActivity();
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.twitter_webview, container, false);
    }    
    
    
    
    public void onResume() {
        super.onResume();
        String url = null;
        FragmentManager fragmentManager = TwitterWebViewFragment.this.getActivity().getSupportFragmentManager();
        Bundle args = this.getArguments();
        if (args != null) {
            url = args.getString("URL");
            loginFragment = (LoginFragment) fragmentManager.getFragment(args, "LoginFragment");
        }
        
        WebView webView = (WebView) mThisActivity.findViewById(R.id.twitter_webview);
        WebSettings webSettings = webView.getSettings();        
        webSettings.setSavePassword(false);
        webSettings.setSaveFormData(false);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(false);           
      
        webView.setWebViewClient( new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains(getString(R.string.TWITTER_CALLBACK_URL))){
                    Uri uri = Uri.parse(url);
                    String oauthVerifier = uri.getQueryParameter( "oauth_verifier" );
                    if (loginFragment != null) {
                        loginFragment.setOAuthVerifierResult(oauthVerifier);
                    }
                    return true;
                }
                return false;
            }
        });
        webView.loadUrl(url);
    }    
    
}



