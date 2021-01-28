

package com.midisheetmusic;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.*;

/** @class HelpActivity
 *  The HelpActivity displays the help.html file in the assets directory.
 */
public class HelpActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help);
        WebView view = (WebView) findViewById(R.id.help_webview);
        view.getSettings().setJavaScriptEnabled(false);
        view.loadUrl("file:///android_asset/help.html");
    }
}

