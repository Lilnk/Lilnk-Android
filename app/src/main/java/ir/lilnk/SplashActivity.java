package ir.lilnk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SplashActivity extends Activity {

    OkHttpClient client;
    public static String AlertResult;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        client = new OkHttpClient();

        Thread SplashDuration = new Thread() {

            @Override
            public void run() {
                try {
                    String serverResult = OkHTTP("https://lilnk.ir/run.html");
                    if (serverResult.equals("0")) {
                        AlertResult = OkHTTP("https://lilnk.ir/temp/alerts.php");
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    } else {
                        runOnUiThread(() -> new AlertDialog.Builder(SplashActivity.this)
                                .setTitle("خطا")
                                .setMessage(serverResult)
                                .setPositiveButton("خب", (dialog, which) -> finish())
                                .show());
                    }
                } catch (Exception ex) {
                    Log.i("Thread Exception", "Thread Err is : " + ex.getMessage());
                    runOnUiThread(() -> Toast.makeText(SplashActivity.this, "ارتباط با سرور ناموفق بود، لطفا دسترسی به اینترنت را بررسی کنید یا فیلترشکن خود را خاموش نمایید...", Toast.LENGTH_SHORT).show());
                }
            }
        };

        SplashDuration.start();
    }

    String OkHTTP(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}