package medrawd.is.awesome.ntsquiz;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;

import medrawd.is.awesome.ntsquiz.legislation.Document;
import medrawd.is.awesome.ntsquiz.question.Question;

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
