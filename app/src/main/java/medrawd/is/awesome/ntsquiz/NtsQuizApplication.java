package medrawd.is.awesome.ntsquiz;

import android.app.Application;

public class NtsQuizApplication extends Application {

    public static final String TAG = NtsQuizApplication.class.getSimpleName();
    private boolean questionsLoaded = false;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public boolean areQuestionsLoaded() {
        return questionsLoaded;
    }

    public void setQuestionsLoaded(boolean questionsLoaded) {
        this.questionsLoaded = questionsLoaded;
    }
}
