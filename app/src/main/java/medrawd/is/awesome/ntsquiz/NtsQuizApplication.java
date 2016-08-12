package medrawd.is.awesome.ntsquiz;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;

import medrawd.is.awesome.ntsquiz.legislation.Document;
import medrawd.is.awesome.ntsquiz.question.Question;

public class NtsQuizApplication extends Application {

    public static final String TAG = NtsQuizApplication.class.getSimpleName();
    public static final String ACTION_LOADING_UPDATE = "medrawd.is.awesome.ntsquiz.ACTION_LOADING_UPDATE";
    public static final String ACTION_LOADING_FINISHED = "medrawd.is.awesome.ntsquiz.ACTION_LOADING_FINISHED";
    public static final String EXTRA_LOADING_STAGE = "medrawd.is.awesome.ntsquiz.EXTRA_LOADING_STAGE";
    private boolean loadingFinished = false;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public void loadDataInBacground(){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                loadData();
            }
        };
        Thread t = new Thread(runnable);
        t.start();
    }

    private void loadData() {
        try {
            broadcastUpdate("ładuję Ustawę o Broni i Amunicji");
            Document uoiba = Document.loadUoBiA(this);
            Log.i(TAG, uoiba.getParagraph());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            broadcastUpdate("ładuję Kodeks Karny");
            Document kk = Document.loadKodeksKarny(this);
            Log.i(TAG, kk.getParagraph());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            broadcastUpdate("ładuję Wzorcowy regulamin strzelnic");
            Document regulamin = Document.loadRegulaminStrzelnic(this);
            Log.i(TAG, regulamin.getParagraph());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            broadcastUpdate("ładuję Rozporządzenie w sprawie przewożenia broni i amunicji środkami transportu publicznego");
            Document rozporzadzenie = Document.loadRozporzadzenieWsPrzewozenia(this);
            Log.i(TAG, rozporzadzenie.getParagraph());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            broadcastUpdate("ładuję Rozporządzenie w sprawie szczegółowych zasad deponowania i niszczenia broni i amunicji w depozycie Policji, Żandarmerii Wojskowej lub organu celnego oraz stawki odpłatności za ich przechowywanie w depozycie ");
            Document rozporzadzenie = Document.loadRozporzadzenieWsDeponowania(this);
            Log.i(TAG, rozporzadzenie.getParagraph());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            broadcastUpdate("ładuję Rozporządzenie w sprawie przechowywania, noszenia oraz ewidencjonowania broni i amunicji");
            Document rozporzadzenie = Document.loadRozporzadzenieWsNoszeniaIprzechowywania(this);
            Log.i(TAG, rozporzadzenie.getParagraph());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            broadcastUpdate("ładuję Rozporządzenie w sprawie egzaminu ze znajomości przepisów dotyczących posiadania broni oraz umiejętności posługiwania się bronią");
            Document rozporzadzenie = Document.loadRozporzadzenieWsEgzaminu(this);
            Log.i(TAG, rozporzadzenie.getParagraph());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            broadcastUpdate("ładuję pytania");
            Question.loadQuestions(this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        broadcastLoadingFinished();
        loadingFinished = true;
    }

    private void broadcastUpdate(String extra) {
        Intent updateIntent = new Intent(ACTION_LOADING_UPDATE);
        updateIntent.putExtra(EXTRA_LOADING_STAGE, extra);

        sendBroadcast(updateIntent);
    }

    private void broadcastLoadingFinished() {
        sendBroadcast(new Intent(ACTION_LOADING_FINISHED));
    }

    public boolean isLoadingFinished() {
        return loadingFinished;
    }
}
