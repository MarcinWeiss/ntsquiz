package medrawd.is.awesome.ntsquiz;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
    public static final String EXTRA_STAGE = "medrawd.is.awesome.ntsquiz.EXTRA_STAGE";
    public static final String EXTRA_FILENAME = "medrawd.is.awesome.ntsquiz.EXTRA_FILENAME";

    private static final String TAG = LoadingActivity.class.getSimpleName();
    public static final String UPDATE_PREFS = "updatePrefs";
    public static final String LAST_UPDATE = "lastUpdate";
    public static final int MILIS_IN_DAY = 86400000;
    private TextView loadingDetails;
    private BroadcastReceiver mReceiver;
    private boolean mReceiverRegistered;
    private FirebaseAuth mAuth;
    private String[] filenames = new String[]{USTAWA_O_BRONI_I_AMUNICJI_FILENAME,
            ROZPORZĄDZENIE_DEPONOWANIE_BRONI_FILENAME, ROZPORZADZENIE_EGZAMIN_FILENAME,
            KODEKS_KARNY_FILENAME, ROZPORZĄDZENIE_NOSZENIE_FILENAME,
            ROZPORZĄDZENIE_PRZEWOŻENIE_FILENAME, WZORCOWY_REGULAMIN_STRZELNIC_FILENAME,
            QUESTIONS_FILENAME};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                    DataLoadingService.startActionLoadData(getApplicationContext());
                } else if (intent.getAction().equals(ACTION_LOADING_FINISHED)) {
                    Log.d(TAG, "action loading finished");
                    ((NtsQuizApplication) getApplication()).setQuestionsLoaded(true);
                    navigateToQuizActivity();
                } else if (intent.getAction().equals(ACTION_LOADING_FAILED)) {
                    Log.d(TAG, "action loading failed");
                    String stringExtra = intent.getStringExtra(EXTRA_FILENAME);
                    android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(context);
                    alertDialogBuilder.setTitle(String.format("Nieudało się załadować %s", stringExtra));
                    alertDialogBuilder
                            .setMessage(String.format("Plik %s nie został poprawnie pobrany, sprawdź połączenie z internetem i spróbuj ponownie", stringExtra))
                            .setCancelable(false)
                            .setPositiveButton("zamknij", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                    finish();
                                }
                            });

                    // create alert dialog
                    android.app.AlertDialog alertDialog = alertDialogBuilder.create();

                    // show it
                    alertDialog.show();
                }
            }
        };
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        View view = super.onCreateView(parent, name, context, attrs);
        return view;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!((NtsQuizApplication) getApplication()).areQuestionsLoaded()) {
            Log.d(TAG, "last update " + String.valueOf(getLastUpdateTime()));
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_LOADING_FINISHED);
            filter.addAction(ACTION_DOWNLOADING_FINISHED);
            filter.addAction(ACTION_LOADING_UPDATE);
            filter.addAction(ACTION_DOWNLOADING_UPDATE);
            filter.addAction(ACTION_LOADING_FAILED);
            registerReceiver(mReceiver, filter);
            mReceiverRegistered = true;

            if (getLastUpdateTime() + MILIS_IN_DAY < new Date().getTime()) {
                mAuth = FirebaseAuth.getInstance();
                mAuth.signInAnonymously()
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Log.d(TAG, "signInAnonymously:onComplete:" + task.isSuccessful());
                                if (task.isSuccessful()) {
                                    RemoteResourcesService.startToDownloadResources(getApplicationContext(), filenames);
                                } else {
                                    DataLoadingService.startActionLoadData(getApplicationContext());
                                }
                            }
                        });
            } else {
                DataLoadingService.startActionLoadData(getApplicationContext());
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

    private void navigateToQuizActivity() {
        Intent intent = new Intent(this, QuizActivity.class);
        startActivity(intent);
    }

    private long getLastUpdateTime() {
        SharedPreferences prefs = getSharedPreferences(UPDATE_PREFS, MODE_PRIVATE);
        return prefs.getLong(LAST_UPDATE, 0L);
    }

    private void updateLastUpdateTime() {
        SharedPreferences.Editor editor = getSharedPreferences(UPDATE_PREFS, MODE_PRIVATE).edit();
        editor.putLong(LAST_UPDATE, new Date().getTime());
        editor.commit();
    }
}
