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
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class TwitterWebViewActivity extends Activity {
    public static final int TWITTER_AUTH = 01;

    private Intent mIntent;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.twitter_webview);
        WebView webView = (WebView) findViewById(R.id.twitter_webview);
        WebSettings webSettings = webView.getSettings();        
        webSettings.setSavePassword(false);
        webSettings.setSaveFormData(false);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(false);           
      
        mIntent = getIntent();
        String url = (String)mIntent.getExtras().get("URL");
        webView.setWebViewClient( new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains(getString(R.string.TWITTER_CALLBACK_URL))){
                    Uri uri = Uri.parse(url);
                    String oauthVerifier = uri.getQueryParameter( "oauth_verifier" );
                    mIntent.putExtra( "oauth_verifier", oauthVerifier );
                    setResult( RESULT_OK, mIntent );
                    finish();
                    return true;
                }
                return false;
            }
        });
        webView.loadUrl(url);
    }    
    
}



