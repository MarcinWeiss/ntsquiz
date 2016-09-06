package medrawd.is.awesome.ntsquiz;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.GoogleApiAvailability;

public class NtsQuizApplication extends Application {

    public static final String TAG = NtsQuizApplication.class.getSimpleName();
    private boolean questionsLoaded = false;

    @Override
    public void onCreate() {
        super.onCreate();
        //showGooglePlayServicesStatus();
    }

    public boolean areQuestionsLoaded() {
        return questionsLoaded;
    }

    public void setQuestionsLoaded(boolean questionsLoaded) {
        this.questionsLoaded = questionsLoaded;
    }

    private void showGooglePlayServicesStatus() {
        GoogleApiAvailability apiAvail = GoogleApiAvailability.getInstance();
        int errorCode = apiAvail.isGooglePlayServicesAvailable(this);
        String msg = "Play Services: " + apiAvail.getErrorString(errorCode);
        Log.d(TAG, msg);
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

}
