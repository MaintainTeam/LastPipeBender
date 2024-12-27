package org.schabi.newpipe.views;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.*;
import androidx.appcompat.app.AppCompatActivity;
import org.schabi.newpipe.R;

public class YouTubeLoginWebViewActivity extends AppCompatActivity {
    WebView webView;
    String cookies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_webview);

        WebView webView = findViewById(R.id.login_webview);
        this.webView = webView;
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUserAgentString("Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.120 Mobile Safari/537.36");

        webView.setWebViewClient(new MyWebViewClient());
        webView.loadUrl("https://www.youtube.com/signin");
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (url.equals("https://m.youtube.com/?noapp=1") || url.equals("https://m.youtube.com/")) {
                setCookies(CookieManager.getInstance().getCookie(url));
                webView.loadUrl("https://music.youtube.com/watch?v=09839DpTctU");
            }
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            String url = request.getUrl().toString();
            // Filter specific requests
            if (url.contains("googlevideo.com/videoplayback")) {
                Uri uri = Uri.parse(url);
                String pot = uri.getQueryParameter("pot");
                Intent intent = new Intent();
                intent.putExtra("cookies", cookies);
                intent.putExtra("pot", pot);
                setResult(RESULT_OK, intent);
                finish();
            }
            // Return null to allow the WebView to load the request as usual
            return null;
        }
    }

//    private void showNonDismissableDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle(getString(R.string.continue_title));
//        builder.setMessage(getString(R.string.youtube_login_instruction));
//        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                webView.loadUrl("https://music.youtube.com/watch?v=09839DpTctU");
//            }
//        });
//        AlertDialog dialog = builder.create();
//        dialog.setCancelable(false); // This makes the dialog non-dismissable
//        dialog.setCanceledOnTouchOutside(false); // Prevents dismissal when touching outside
//        dialog.show();
//    }

    public void setCookies(String cookies) {
        this.cookies = cookies;
    }

    public String getCookies() {
        return cookies;
    }
}
