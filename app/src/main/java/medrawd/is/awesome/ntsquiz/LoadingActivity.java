package medrawd.is.awesome.ntsquiz;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

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
                    loadingDetails.setText(message);
                } else if (intent.getAction().equals(ACTION_DOWNLOADING_FINISHED)) {
                    DataLoadingService.startActionLoadData(getApplicationContext());
                } else if (intent.getAction().equals(ACTION_LOADING_FINISHED)) {
                    navigateToQuizActivity();
                } else if (intent.getAction().equals(ACTION_LOADING_FAILED)) {
                    String stringExtra = intent.getStringExtra(EXTRA_FILENAME);
                    new AlertDialog.Builder(getApplicationContext())
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Nieudało się załadować " + stringExtra)
                            .setMessage(String.format("Plik %s nie został poprawnie pobrany, sprawdź połączenie z internetem i spróbuj ponownie", stringExtra))
                            .setPositiveButton("Rozumiem", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }

                            })
                            .show();
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
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_LOADING_FINISHED);
        filter.addAction(ACTION_DOWNLOADING_FINISHED);
        filter.addAction(ACTION_LOADING_UPDATE);
        filter.addAction(ACTION_DOWNLOADING_UPDATE);
        filter.addAction(ACTION_LOADING_FAILED);
        registerReceiver(mReceiver, filter);
        mReceiverRegistered = true;

        mAuth = FirebaseAuth.getInstance();
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInAnonymously:onComplete:" + task.isSuccessful());
                        RemoteResourcesService.startToDownloadResources(getApplicationContext(), filenames);
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInAnonymously", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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
}
