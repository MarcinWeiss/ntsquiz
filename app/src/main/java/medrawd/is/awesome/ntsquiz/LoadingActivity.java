package medrawd.is.awesome.ntsquiz;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class LoadingActivity extends AppCompatActivity {

    private static final String TAG = LoadingActivity.class.getSimpleName();
    private TextView loadingDetails;
    private BroadcastReceiver mReceiver;
    private boolean mReceiverRegistered;

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
                if (intent.getAction().equals(NtsQuizApplication.ACTION_LOADING_UPDATE)) {
                    String message = intent.getStringExtra(NtsQuizApplication.EXTRA_LOADING_STAGE);
                    loadingDetails.setText(message);
                } else if (intent.getAction().equals(NtsQuizApplication.ACTION_LOADING_FINISHED)) {
                    navigateToQuizActivity();
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
        NtsQuizApplication application = (NtsQuizApplication) getApplication();
        if (application.isLoadingFinished()) {
            navigateToQuizActivity();
        } else {
            IntentFilter filter = new IntentFilter();
            filter.addAction(NtsQuizApplication.ACTION_LOADING_FINISHED);
            filter.addAction(NtsQuizApplication.ACTION_LOADING_UPDATE);
            registerReceiver(mReceiver, filter);
            mReceiverRegistered = true;
            application.loadDataInBacground();
        }
    }

    @Override
    protected void onPause() {
        if(mReceiverRegistered) {
            unregisterReceiver(mReceiver);
        }
        super.onPause();
    }

    private void navigateToQuizActivity() {
        Intent intent = new Intent(this, QuizActivity.class);
        startActivity(intent);
    }
}
