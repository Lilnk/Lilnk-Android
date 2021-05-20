package ir.lilnk;

import android.annotation.SuppressLint;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import net.glxn.qrgen.android.QRCode;

import java.io.IOException;
import java.util.ArrayList;

import ir.lilnk.Helpers.CopyToClipboard;
import ir.lilnk.Helpers.Preferences;
import ir.lilnk.History.DataAdapter;
import ir.lilnk.History.SQL_MyDatabaseHelper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements ApiService.OnIPInfoReceived {

    private static final String TAG = "LilnkTAG";

    public static String LongURL;
    private ApiService apiService;
    String ShortLink;
    EditText txtLongURL;
    TextView TVStats;
    //--------------------
    LayoutInflater inflater;
    View myView;
    //---------------------
    OkHttpClient client;
    //---------------------
    LinearLayout CustomAlertDialog;
    TextView txtAlert;
    //---------------------
    RelativeLayout btnGO;
    TextView Go_MainContent;
    LinearLayout Go_LoadingContent;
    //---------------------
    RecyclerView recyclerView;
    SQL_MyDatabaseHelper myDatabaseHelper;
    ArrayList<String> ListShortLink, ListLongLink;
    DataAdapter dataAdapter;
    LottieAnimationView lottieAnimationView;
    public static boolean QRS = false;

    void initViews() {
        txtLongURL = findViewById(R.id.inputURL);
        TVStats = findViewById(R.id.txtStats);
        CustomAlertDialog = findViewById(R.id.LayoutAlert);
        txtAlert = findViewById(R.id.txtAlert);
        btnGO = findViewById(R.id.btnGO);
        Go_MainContent = findViewById(R.id.Go_MainContent);
        Go_LoadingContent = findViewById(R.id.Go_LoadingContent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        client = new OkHttpClient();
        Preferences.initialize(getApplicationContext());

        // Receive text from share
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null)
            if ("text/plain".equals(type))
                handleSendText(intent); // Handle text being sent

        findViewById(R.id.TitleLayout).setOnClickListener(v -> {
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(200);
        });

        findViewById(R.id.btnWebsite).setOnClickListener(v ->
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://lilnk.ir"))));

        findViewById(R.id.AD_CardView).setOnClickListener(v -> {
            if (Statics.Market == 'M')
                To_Myket("myket://download/Arash.MyCreditCard");
            if(Statics.Market == 'B')
            {
                Intent B_intent = new Intent(Intent.ACTION_VIEW);
                B_intent.setData(Uri.parse("bazaar://details?id=Arash.MyCreditCard"));
                B_intent.setPackage("com.farsitel.bazaar");
                startActivity(B_intent);
            }
        });

        btnGO.setOnClickListener(v -> {
            if (txtLongURL.length() > 2)
                if (txtLongURL.getText().toString().contains("http://") || txtLongURL.getText().toString().contains("https://"))
                    if (!txtLongURL.getText().toString().contains("lilnk.ir")) {
                        LongURL = txtLongURL.getText().toString();
                        apiService = new ApiService(MainActivity.this);

                        btnGO.setEnabled(false);
                        Go_MainContent.setVisibility(View.GONE);
                        Go_LoadingContent.setVisibility(View.VISIBLE);

                        DoWork();
                    } else ToastError("لینک نامعتبر است!");
                else ToastError("لینک نامعتبر است!");
            else ToastError("لینک را وارد کنید.");
        });

        findViewById(R.id.btnInfo).setOnClickListener(v -> Info_BTMSheetDialog());
        findViewById(R.id.btnHistory).setOnClickListener(v -> BTM_History());

        findViewById(R.id.fab_Paste).setOnClickListener(v -> {
            try {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                String txt = clipboard.getText().toString();

                if (txt.toLowerCase().contains("http://") || txt.toLowerCase().contains("https://") && !txt.toLowerCase().contains("lilnk.ir"))
                    txtLongURL.setText(clipboard.getText().toString());
                else ToastError("لینک نامعتبر است!");
            } catch (Exception ex) {
                ToastError("متاسفم، موردی رخ داده...");
                ex.printStackTrace();
            }
        });

        CustomAlertDialog.setOnClickListener(v -> CustomAlertDialog.setVisibility(View.GONE));
    }

    void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            txtLongURL.setText(sharedText);
        }
    }

    final private String Key_History_QR_Switch = "History_QR_Switch";

    public void BTM_History() {
        try {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.btm_history, null);
            lottieAnimationView = view.findViewById(R.id.animationView);

            @SuppressLint("UseSwitchCompatOrMaterialCode")
            Switch aSwitch = view.findViewById(R.id.SwitchQRSource);
            aSwitch.setChecked(Preferences.getInstance().getBoolean(Key_History_QR_Switch));
            QRS = Preferences.getInstance().getBoolean(Key_History_QR_Switch);

            aSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                QRS = isChecked;
                dataAdapter.notifyDataSetChanged();
                Preferences.getInstance().setBoolean(Key_History_QR_Switch, isChecked);
            });

            recyclerView = view.findViewById(R.id.RecyclerView);
            myDatabaseHelper = new SQL_MyDatabaseHelper(view.getContext());

            ListShortLink = new ArrayList<>();
            ListLongLink = new ArrayList<>();

            DisplayDataInArray();
            dataAdapter = new DataAdapter(view.getContext(), ListShortLink, ListLongLink);

            recyclerView.addItemDecoration(new DividerItemDecoration(view.getContext(), DividerItemDecoration.VERTICAL));

            recyclerView.setAdapter(dataAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

            bottomSheetDialog.setContentView(view);
            bottomSheetDialog.show();

        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    Cursor cursor;

    void DisplayDataInArray() {
        cursor = myDatabaseHelper.ReadAllData();
        if (cursor.getCount() == 0) {
            lottieAnimationView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lottieAnimationView.setVisibility(View.GONE);
            while (cursor.moveToNext()) {
                ListShortLink.add(cursor.getString(0));
                ListLongLink.add(cursor.getString(1));
            }
        }
    }


    private void DoWork() {
        apiService.getIPData(MainActivity.this);
    }


    @Override
    public void OnReceived(Lilink lilink) {
        if (lilink != null) {
            ShortLink = Lilink.getUrl();
            inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            myView = inflater.inflate(R.layout.output_btmsheet_design, null);
            EditText shortURL = myView.findViewById(R.id.txtShortLink);
            shortURL.setText(ShortLink);
            SQL_MyDatabaseHelper My_DB = new SQL_MyDatabaseHelper(getApplicationContext());
            if (!My_DB.IsItemExist(shortURL.getText().toString()))
                My_DB.AddToDatabase(shortURL.getText().toString(), txtLongURL.getText().toString());
            BTM_Output();

        } else ToastError("خطا در دریافت اطلاعات!");

        Go_LoadingContent.setVisibility(View.GONE);
        Go_MainContent.setVisibility(View.VISIBLE);
        btnGO.setEnabled(true);
    }

    private void BTM_Output() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MainActivity.this);

        TextView buttonCopy = myView.findViewById(R.id.TVbtnCopy);
        buttonCopy.setOnClickListener(v -> {
            CopyToClipboard.setClipboard(this, ShortLink);
            buttonCopy.setText("کپی شد! ");
        });

        myView.findViewById(R.id.TVbtnClose).setOnClickListener(v -> {
            bottomSheetDialog.cancel();
        });

        myView.findViewById(R.id.TVbtnShare).setOnClickListener(v -> {
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String shareBody = "این لینک رو حتما چک کن: " + "\n" + ShortLink + "\n\n کوتاه شده با لیلینک";
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(sharingIntent, "Share via"));
            bottomSheetDialog.cancel();
        });

        ImageView QR_IV = myView.findViewById(R.id.QR_ImageView);
        Bitmap myBitmap = QRCode.from(ShortLink).withSize(512, 512).bitmap();
        QR_IV.setClipToOutline(true);
        QR_IV.setImageBitmap(myBitmap);

        TextView TV_ClickToCopy = myView.findViewById(R.id.TV_ClickToCopy);
        myView.findViewById(R.id.RL_SaveQR).setOnClickListener(v -> {
            try {
                MediaStore.Images.Media.insertImage(getContentResolver(), myBitmap, "Lilnk", ShortLink);
                TV_ClickToCopy.setText("ذخیره شد!");
            } catch (Exception ex) {
                TV_ClickToCopy.setText("برنامه قادر به ذخیره بارکد نبود...");
            }
        });

        @SuppressLint("UseSwitchCompatOrMaterialCode")
        Switch aSwitch = myView.findViewById(R.id.SwitchSource);
        aSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                TV_ClickToCopy.setText("برای ذخیره بارکد کلیک کنید.");
                QR_IV.setImageBitmap(QRCode.from(txtLongURL.getText().toString()).withSize(512, 512).bitmap());
            } else {
                TV_ClickToCopy.setText("برای ذخیره بارکد کلیک کنید.");
                QR_IV.setImageBitmap(QRCode.from(ShortLink).withSize(512, 512).bitmap());
            }
        });

        bottomSheetDialog.setContentView(myView);
        bottomSheetDialog.show();
    }

    private void Info_BTMSheetDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MainActivity.this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View myView = inflater.inflate(R.layout.info_btmsheet_design, null);

        myView.findViewById(R.id.inGithubLayout).setOnClickListener(view ->
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://ArashAzizi.ir"))));

        if (Statics.Market == 'M') {
            myView.findViewById(R.id.Btn_Comment).setOnClickListener(view -> To_Myket("myket://comment?id=" + myView.getContext().getApplicationContext().getPackageName()));
            myView.findViewById(R.id.BtnOtherApps).setOnClickListener(view -> To_Myket("myket://developer/" + myView.getContext().getPackageName()));
        }
        if (Statics.Market == 'B') {
            myView.findViewById(R.id.Btn_Comment).setOnClickListener(v -> ToComments_Bazaar());
            myView.findViewById(R.id.BtnOtherApps).setOnClickListener(v -> ToApps_Bazaar());
        }

        bottomSheetDialog.setContentView(myView);
        bottomSheetDialog.show();
    }

    public void ToastError(String Text) {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_toast, findViewById(R.id.ToastLayout));
        TextView txtToast = view.findViewById(R.id.txtToast);
        final Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(view);
        txtToast.setText(Text);
        toast.show();
    }


    @Override
    protected void onStart() {
        try {
            if (!SplashActivity.AlertResult.equals("0")) {
                CustomAlertDialog.setVisibility(View.VISIBLE);
                txtAlert.setText(SplashActivity.AlertResult);
            }
        } catch (Exception ex) {
            Log.e("Thread Exception", "Thread Err is : " + ex.getMessage());
        }
        super.onStart();
    }

    String OkHTTP(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    public void To_Myket(String MyketURL) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(MyketURL));
        startActivity(intent);
    }

    public void ToComments_Bazaar() {
        Intent intent = new Intent(Intent.ACTION_EDIT);
        intent.setData(Uri.parse("bazaar://details?id=" + this.getPackageName()));
        intent.setPackage("com.farsitel.bazaar");
        startActivity(intent);
    }

    public void ToApps_Bazaar() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("bazaar://collection?slug=by_author&aid=" + "974269112380"));
        intent.setPackage("com.farsitel.bazaar");
        startActivity(intent);
    }
}