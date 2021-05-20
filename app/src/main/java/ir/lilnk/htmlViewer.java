package ir.lilnk;

import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class htmlViewer extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_htmlview);

        WebView WebX = findViewById(R.id.WebX);
        SwipeRefreshLayout SRL = findViewById(R.id.SwipeRefreshLayout);
        SRL.setRefreshing(true);
        WebX.loadUrl("file:///android_asset/version.html");
        WebX.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        WebX.setWebViewClient(new WebViewClient());
        WebX.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress == 100) {
                    SRL.setRefreshing(false);
                    SRL.setEnabled(false);
                }
            }
        });

        findViewById(R.id.TV_Exit).setOnClickListener(v -> finish());
    }
}