package medrawd.is.awesome.ntsquiz.storage;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import java.io.IOException;

import medrawd.is.awesome.ntsquiz.legislation.Document;
import medrawd.is.awesome.ntsquiz.question.Question;

import static medrawd.is.awesome.ntsquiz.LoadingActivity.ACTION_LOADING_FAILED;
import static medrawd.is.awesome.ntsquiz.LoadingActivity.ACTION_LOADING_FINISHED;
import static medrawd.is.awesome.ntsquiz.LoadingActivity.ACTION_LOADING_UPDATE;
import static medrawd.is.awesome.ntsquiz.LoadingActivity.EXTRA_FILENAME;
import static medrawd.is.awesome.ntsquiz.LoadingActivity.EXTRA_STAGE;
import static medrawd.is.awesome.ntsquiz.legislation.Document.KODEKS_KARNY_FILENAME;
import static medrawd.is.awesome.ntsquiz.legislation.Document.ROZPORZADZENIE_EGZAMIN_FILENAME;
import static medrawd.is.awesome.ntsquiz.legislation.Document.ROZPORZĄDZENIE_DEPONOWANIE_BRONI_FILENAME;
import static medrawd.is.awesome.ntsquiz.legislation.Document.ROZPORZĄDZENIE_NOSZENIE_FILENAME;
import static medrawd.is.awesome.ntsquiz.legislation.Document.ROZPORZĄDZENIE_PRZEWOŻENIE_FILENAME;
import static medrawd.is.awesome.ntsquiz.legislation.Document.USTAWA_O_BRONI_I_AMUNICJI_FILENAME;
import static medrawd.is.awesome.ntsquiz.legislation.Document.WZORCOWY_REGULAMIN_STRZELNIC_FILENAME;
import static medrawd.is.awesome.ntsquiz.question.Question.QUESTIONS_FILENAME;

public class DataLoadingService extends IntentService {
    private static final String TAG = DataLoadingService.class.getSimpleName();

    private static final String ACTION_LOAD_DATA = "medrawd.is.awesome.ntsquiz.storage.action.LOAD_DATA";

    public DataLoadingService() {
        super("DataLoadingService");
    }

    public static void startActionLoadData(Context context) {
        Intent intent = new Intent(context, DataLoadingService.class);
        intent.setAction(ACTION_LOAD_DATA);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_LOAD_DATA.equals(action)) {
                loadData();
            }
        }
    }

    private void loadData() {
        try {
            broadcastLoadingUpdate("ładuję Ustawę o Broni i Amunicji");
            Document uoiba = Document.loadUoBiA(this);
            Log.i(TAG, uoiba.getParagraph());
        } catch (IOException e) {
            broadcastLoadingFailed(USTAWA_O_BRONI_I_AMUNICJI_FILENAME);
            return;
        }
        try {
            broadcastLoadingUpdate("ładuję Kodeks Karny");
            Document kk = Document.loadKodeksKarny(this);
            Log.i(TAG, kk.getParagraph());
        } catch (IOException e) {
            broadcastLoadingFailed(KODEKS_KARNY_FILENAME);
            return;
        }
        try {
            broadcastLoadingUpdate("ładuję Wzorcowy regulamin strzelnic");
            Document regulamin = Document.loadRegulaminStrzelnic(this);
            Log.i(TAG, regulamin.getParagraph());
        } catch (IOException e) {
            broadcastLoadingFailed(WZORCOWY_REGULAMIN_STRZELNIC_FILENAME);
            return;
        }
        try {
            broadcastLoadingUpdate("ładuję Rozporządzenie w sprawie przewożenia broni i amunicji środkami transportu publicznego");
            Document rozporzadzenie = Document.loadRozporzadzenieWsPrzewozenia(this);
            Log.i(TAG, rozporzadzenie.getParagraph());
        } catch (IOException e) {
            broadcastLoadingFailed(ROZPORZĄDZENIE_PRZEWOŻENIE_FILENAME);
            return;
        }
        try {
            broadcastLoadingUpdate("ładuję Rozporządzenie w sprawie szczegółowych zasad deponowania i niszczenia broni i amunicji w depozycie Policji, Żandarmerii Wojskowej lub organu celnego oraz stawki odpłatności za ich przechowywanie w depozycie ");
            Document rozporzadzenie = Document.loadRozporzadzenieWsDeponowania(this);
            Log.i(TAG, rozporzadzenie.getParagraph());
        } catch (IOException e) {
            broadcastLoadingFailed(ROZPORZĄDZENIE_DEPONOWANIE_BRONI_FILENAME);
            return;
        }
        try {
            broadcastLoadingUpdate("ładuję Rozporządzenie w sprawie przechowywania, noszenia oraz ewidencjonowania broni i amunicji");
            Document rozporzadzenie = Document.loadRozporzadzenieWsNoszeniaIprzechowywania(this);
            Log.i(TAG, rozporzadzenie.getParagraph());
        } catch (IOException e) {
            broadcastLoadingFailed(ROZPORZĄDZENIE_NOSZENIE_FILENAME);
            return;
        }
        try {
            broadcastLoadingUpdate("ładuję Rozporządzenie w sprawie egzaminu ze znajomości przepisów dotyczących posiadania broni oraz umiejętności posługiwania się bronią");
            Document rozporzadzenie = Document.loadRozporzadzenieWsEgzaminu(this);
            Log.i(TAG, rozporzadzenie.getParagraph());
        } catch (IOException e) {
            broadcastLoadingFailed(ROZPORZADZENIE_EGZAMIN_FILENAME);
            return;
        }
        try {
            broadcastLoadingUpdate("ładuję pytania");
            Question.loadQuestions(this);
        } catch (IOException e) {
            broadcastLoadingFailed(QUESTIONS_FILENAME);
            return;
        }
        broadcastLoadingFinished();
    }

    private void broadcastLoadingUpdate(String extra) {
        Intent updateIntent = new Intent(ACTION_LOADING_UPDATE);
        updateIntent.putExtra(EXTRA_STAGE, extra);

        sendBroadcast(updateIntent);
    }

    private void broadcastLoadingFinished() {
        sendBroadcast(new Intent(ACTION_LOADING_FINISHED));
    }

    private void broadcastLoadingFailed(String fileName) {
        Intent failedIntent = new Intent(ACTION_LOADING_FAILED);
        failedIntent.putExtra(EXTRA_FILENAME, fileName);
        sendBroadcast(failedIntent);
    }
}
