package medrawd.is.awesome.ntsquiz;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Date;

import medrawd.is.awesome.ntsquiz.storage.DataLoadingService;
import medrawd.is.awesome.ntsquiz.storage.RemoteResourcesService;

import static medrawd.is.awesome.ntsquiz.legislation.Document.KODEKS_KARNY_FILENAME;
import static medrawd.is.awesome.ntsquiz.legislation.Document.ROZPORZADZENIE_EGZAMIN_FILENAME;
import static medrawd.is.awesome.ntsquiz.legislation.Document.ROZPORZĄDZENIE_DEPONOWANIE_BRONI_FILENAME;
import static medrawd.is.awesome.ntsquiz.legislation.Document.ROZPORZĄDZENIE_NOSZENIE_FILENAME;
import static medrawd.is.awesome.ntsquiz.legislation.Document.ROZPORZĄDZENIE_PRZEWOŻENIE_FILENAME;
import static medrawd.is.awesome.ntsquiz.legislation.Document.USTAWA_O_BRONI_I_AMUNICJI_FILENAME;
import static medrawd.is.awesome.ntsquiz.legislation.Document.WZORCOWY_REGULAMIN_STRZELNIC_FILENAME;
import static medrawd.is.awesome.ntsquiz.question.Question.QUESTIONS_FILENAME;

public class LoadingActivity extends AppCompatActivity {
    public static final String ACTION_DOWNLOADING_UPDATE = "medrawd.is.awesome.ntsquiz.ACTION_DOWNLOADING_UPDATE";
    public static final String ACTION_DOWNLOADING_FINISHED = "medrawd.is.awesome.ntsquiz.ACTION_DOWNLOADING_FINISHED";
    public static final String ACTION_LOADING_UPDATE = "medrawd.is.awesome.ntsquiz.ACTION_LOADING_UPDATE";
    public static final String ACTION_LOADING_FINISHED = "medrawd.is.awesome.ntsquiz.ACTION_LOADING_FINISHED";
    public static final String ACTION_LOADING_FAILED = "medrawd.is.awesome.ntsquiz.ACTION_LOADING_FAILED";
    public static final String ACTION_INTERNET_CONNECTION_FAILED = "medrawd.is.awesome.ntsquiz.ACTION_INTERNET_CONNECTION_FAILED";
    public static final String EXTRA_STAGE = "medrawd.is.awesome.ntsquiz.EXTRA_STAGE";
    public static final String EXTRA_FILENAME = "medrawd.is.awesome.ntsquiz.EXTRA_FILENAME";
    private static final String UPDATE_PREFS = "updatePrefs";
    private static final String LAST_UPDATE = "lastUpdate";
    private static final int MILIS_IN_DAY = 86400000;
    private static final String TAG = LoadingActivity.class.getSimpleName();
    private TextView loadingDetails;
    private BroadcastReceiver mReceiver;
    private boolean mReceiverRegistered;
    private String[] filenames = new String[]{USTAWA_O_BRONI_I_AMUNICJI_FILENAME,
            ROZPORZĄDZENIE_DEPONOWANIE_BRONI_FILENAME, ROZPORZADZENIE_EGZAMIN_FILENAME,
            KODEKS_KARNY_FILENAME, ROZPORZĄDZENIE_NOSZENIE_FILENAME,
            ROZPORZĄDZENIE_PRZEWOŻENIE_FILENAME, WZORCOWY_REGULAMIN_STRZELNIC_FILENAME, QUESTIONS_FILENAME};
    private IntentFilter mFilter;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFilter = new IntentFilter();
        mFilter.addAction(ACTION_LOADING_FINISHED);
        mFilter.addAction(ACTION_DOWNLOADING_FINISHED);
        mFilter.addAction(ACTION_LOADING_UPDATE);
        mFilter.addAction(ACTION_DOWNLOADING_UPDATE);
        mFilter.addAction(ACTION_LOADING_FAILED);
        mFilter.addAction(ACTION_INTERNET_CONNECTION_FAILED);

        PreferenceManager.setDefaultValues(this, R.xml.pref_data_updates, true);
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, true);
        if (null != getIntent()) {
            Intent intent = getIntent();
            if (intent.hasExtra("action")) {
                switch (intent.getStringExtra("action")) {
                    case "updateData":
                        ((NtsQuizApplication) getApplication()).setQuestionsLoaded(false);
                        setLastUpdateTime(0);
                        SharedPreferences prefs = getSharedPreferences(MainActivity.ALL_QUESTIONS_PREFS_NAME, MODE_PRIVATE);
                        prefs.edit().putInt(MainActivity.START_INDEX_PREF_NAME, 0).commit();
                        break;
                }
            }
        }

        setContentView(R.layout.activity_loading);

        ImageView coin = (ImageView) findViewById(R.id.logo_coin);
        AnimatorSet anim = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.flipping);
        anim.setTarget(coin);
        anim.start();

        loadingDetails = (TextView) findViewById(R.id.loading_details);

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(ACTION_LOADING_UPDATE) ||
                        intent.getAction().equals(ACTION_DOWNLOADING_UPDATE)) {
                    String message = intent.getStringExtra(EXTRA_STAGE);
                    Log.d(TAG, "action state update " + message);
                    loadingDetails.setText(message);
                } else if (intent.getAction().equals(ACTION_DOWNLOADING_FINISHED)) {
                    Log.d(TAG, "action downloading finished");
                    updateLastUpdateTime();
                    loadingDetails.setText(R.string.downloading_finished_message);
                    startLoadingData();
                } else if (intent.getAction().equals(ACTION_LOADING_FINISHED)) {
                    Log.d(TAG, "action loading finished");
                    ((NtsQuizApplication) getApplication()).setQuestionsLoaded(true);
                    loadingDetails.setText(R.string.loading_finished_message);
                    navigateToQuizActivity();
                } else if (intent.getAction().equals(ACTION_LOADING_FAILED)) {
                    Log.d(TAG, "action loading failed");
                    String stringExtra = intent.getStringExtra(EXTRA_FILENAME);
                    String message = getString(R.string.file_download_failure_popup_message, stringExtra);
                    loadingDetails.setText(message);
                    android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(context);
                    alertDialogBuilder.setTitle(getString(R.string.file_download_failure_popup_title, stringExtra));
                    alertDialogBuilder
                            .setMessage(message)
                            .setCancelable(false)
                            .setPositiveButton(R.string.file_download_failure_popup_positive, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                    finish();
                                }
                            });
                    android.app.AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                } else if (intent.getAction().equals(ACTION_INTERNET_CONNECTION_FAILED)) {
                    Log.d(TAG, "no internet connection");
                    String message = getString(R.string.file_no_internet_connection_popup_message);
                    loadingDetails.setText(message);
                    android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(context);
                    alertDialogBuilder.setTitle(getString(R.string.file_no_internet_connection_popup_title));
                    alertDialogBuilder
                            .setMessage(message)
                            .setCancelable(false)
                            .setPositiveButton(R.string.file_download_failure_popup_positive, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                    finish();
                                }
                            });
                    android.app.AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
            }
        };
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        return super.onCreateView(parent, name, context, attrs);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (doQuestionsNeedLoading()) {
            Log.d(TAG, "last update " + String.valueOf(getLastUpdateTime()));
            registerReceiver();
            if (shouldLookForUpdates()) {
                signInAndDownloadFiles();
            } else {
                startLoadingData();
            }
        } else {
            navigateToQuizActivity();
        }
    }

    @Override
    protected void onPause() {
        if (mReceiverRegistered) {
            unregisterReceiver(mReceiver);
        }
        super.onPause();
    }

    private void startLoadingData() {
        DataLoadingService.startActionLoadData(getApplicationContext());
    }

    private boolean doQuestionsNeedLoading() {
        return !((NtsQuizApplication) getApplication()).areQuestionsLoaded();
    }

    private void signInAndDownloadFiles() {
        if(arePlayServicesInstalled()) {
            loadingDetails.setText(R.string.connecting_to_firebase_message);
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            mAuth.signInAnonymously().addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    loadingDetails.setText(R.string.connected_to_firebase_message);
                    RemoteResourcesService.startToDownloadResources(getApplicationContext(), filenames);
                }
            }).addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, e.getLocalizedMessage());
                    loadingDetails.setText(R.string.unable_to_connect_to_firebase_message);
                    RemoteResourcesService.unpackBackupsIfNeeded(getApplicationContext(), filenames);
                    startLoadingData();
                }
            });
        } else {
            RemoteResourcesService.unpackBackupsIfNeeded(getApplicationContext(), filenames);
        }
    }

    private boolean arePlayServicesInstalled(){
        try {
            int v = getApplicationContext().getPackageManager().getPackageInfo("com.google.android.gms", 0 ).versionCode;
            return v>0;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void registerReceiver() {
        registerReceiver(mReceiver, mFilter);
        mReceiverRegistered = true;
    }

    private void navigateToQuizActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private long getLastUpdateTime() {
        SharedPreferences prefs = getSharedPreferences(UPDATE_PREFS, MODE_PRIVATE);
        return prefs.getLong(LAST_UPDATE, 0L);
    }

    @SuppressLint("CommitPrefEdits")
    private void setLastUpdateTime(long time) {
        SharedPreferences.Editor editor = getSharedPreferences(UPDATE_PREFS, MODE_PRIVATE).edit();
        editor.putLong(LAST_UPDATE, time);
        editor.commit();
    }

    private void updateLastUpdateTime() {
        setLastUpdateTime(new Date().getTime());
    }

    private boolean isWifiConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return wifiInfo.isConnected();
    }


    private boolean shallUpdateOnlyIfWifiOn() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPref.getBoolean("only_wifi_update_pref", false);
    }

    private long getMinTimeBetweenUpdates() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        return Long.parseLong(sharedPref.getString("timebe_between_updates_pref", String.valueOf(MILIS_IN_DAY)));
    }

    private boolean isFirstStart() {
        return getLastUpdateTime() == 0;
    }

    private boolean shouldLookForUpdates() {
        return isFirstStart() || ((!shallUpdateOnlyIfWifiOn() || isWifiConnected()) && getLastUpdateTime() + getMinTimeBetweenUpdates() < new Date().getTime());
    }
}
