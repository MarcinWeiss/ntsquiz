package medrawd.is.awesome.ntsquiz.storage;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import medrawd.is.awesome.ntsquiz.R;
import medrawd.is.awesome.ntsquiz.legislation.Document;
import medrawd.is.awesome.ntsquiz.question.Question;

import static medrawd.is.awesome.ntsquiz.LoadingActivity.ACTION_DOWNLOADING_FINISHED;
import static medrawd.is.awesome.ntsquiz.LoadingActivity.ACTION_DOWNLOADING_UPDATE;
import static medrawd.is.awesome.ntsquiz.LoadingActivity.ACTION_INTERNET_CONNECTION_FAILED;
import static medrawd.is.awesome.ntsquiz.LoadingActivity.EXTRA_STAGE;

public class RemoteResourcesService extends IntentService {
    public static final String GOOGLE_URL = "http://www.google.com";
    public static final int CONNECT_TIMEOUT = 5000;
    public static final int HTTP_RESPONSE_OK = 200;
    public static final int MAX_FAILED_ATTEMPTS = 5;
    public static final String EXTRA_FILES_NAMES = "medrawd.is.awesome.ntsquiz.storage.action.FILES_NAMES";
    private static final String TAG = RemoteResourcesService.class.getSimpleName();
    private static final String ACTION_DOWNLOAD_RESOURCES = "medrawd.is.awesome.ntsquiz.storage.action.DOWNLOAD_RESOURCES";
    private static final String ACTION_UNPACK_BACKUP_RESOURCES = "medrawd.is.awesome.ntsquiz.storage.action.UNPACK_BACKUP_RESOURCES";
    private static final String ACTION_DOWNLOAD_RESOURCE = "medrawd.is.awesome.ntsquiz.storage.action.DOWNLOAD_RESOURCE";
    private static List<String> filesNames;
    private static int failedAttempts = 0;
    private FirebaseStorage mStorage = FirebaseStorage.getInstance();

    public RemoteResourcesService() {
        super("RemoteResourcesService");
    }

    public static void startToDownloadResources(Context context, String[] filesNames) {
        Intent intent = new Intent(context, RemoteResourcesService.class);
        intent.setAction(ACTION_DOWNLOAD_RESOURCES);
        intent.putExtra(EXTRA_FILES_NAMES, filesNames);
        context.startService(intent);
    }

    public static void unpackBackupsIfNeeded(Context context, String[] filesNames) {
        Intent intent = new Intent(context, RemoteResourcesService.class);
        intent.setAction(ACTION_UNPACK_BACKUP_RESOURCES);
        intent.putExtra(EXTRA_FILES_NAMES, filesNames);
        context.startService(intent);
    }

    public static void downloadResource(Context context) {
        Intent intent = new Intent(context, RemoteResourcesService.class);
        intent.setAction(ACTION_DOWNLOAD_RESOURCE);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (null != action) {
                if (ACTION_DOWNLOAD_RESOURCES.equals(action)) {
                    filesNames = new ArrayList<>(Arrays.asList(intent.getStringArrayExtra(EXTRA_FILES_NAMES)));
                    if(hasActiveInternetConnection()) {
                        broadcastDownloadingUpdate("sprawdzanie plików");
                        downloadFirst();
                    } else {
                        unpackBackups();
                    }
                } else if (ACTION_UNPACK_BACKUP_RESOURCES.equals(action)) {
                    filesNames = new ArrayList<>(Arrays.asList(intent.getStringArrayExtra(EXTRA_FILES_NAMES)));
                    broadcastDownloadingUpdate("przygotowywanie danych offline");
                    unpackBackups();
                } else if (ACTION_DOWNLOAD_RESOURCE.equals(action)) {
                    downloadFileIfNeeded();
                }
            }
        }
    }

    private void unpackBackups() {
        for(String fileName : filesNames){
            unpackBackupIfNeeded(fileName);
        }
        broadcastDownloadingFinished();
    }

    public boolean hasActiveInternetConnection() {
        broadcastDownloadingUpdate("sprawdzam połączenie z internetem");
        try {
            HttpURLConnection urlc = (HttpURLConnection) (new URL(GOOGLE_URL).openConnection());
            urlc.setRequestProperty("User-Agent", "Test");
            urlc.setRequestProperty("Connection", "close");
            urlc.setConnectTimeout(CONNECT_TIMEOUT);
            urlc.connect();
            return (urlc.getResponseCode() == HTTP_RESPONSE_OK);
        } catch (IOException e) {
            Log.e(TAG, "Error checking internet connection", e);
        }
        return false;
    }

    private void broadcastNoInternetConnection() {
        Intent failedIntent = new Intent(ACTION_INTERNET_CONNECTION_FAILED);
        sendBroadcast(failedIntent);
    }

    private void downloadFileIfNeeded() {
        final String filename = filesNames.get(0);
        Log.i(TAG, String.format("downloadFileIfNeeded %s", filename));
        if (isNetworkAvailable() && arePlayServicesInstalled()) {
            broadcastDownloadingUpdate("sprawdzanie plik " + filename);
            StorageReference fileRef = mStorage.getReference().child(filename);
            fileRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                @Override
                public void onSuccess(StorageMetadata storageMetadata) {
                    if (!isFilePresent(filename) || fileIsOutdated(storageMetadata, filename)) {
                        broadcastDownloadingUpdate("pobieram plik " + filename);
                        downloadFile(filename);
                    } else {
                        downloadNext();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    unpackBackupIfNeeded(filename);
                    downloadNext();
                }
            });
        } else {
            unpackBackupIfNeeded(filename);
            downloadNext();
        }
    }

    private void unpackBackupIfNeeded(String filename) {
        if(!isFilePresent(filename)){
            broadcastDownloadingUpdate("nie udało się pobrać pliku " + filename+ "\nzostanie użyty backup");
            try {
                unpackBackupFile(filename);
            } catch (IOException ex) {
                broadcastDownloadingUpdate("nie udało się rozpakować backupu pliku " + filename);
            }
        }
    }

    private void unpackBackupFile(String filename) throws IOException {
        String path = getApplicationContext().getFilesDir().getAbsolutePath() + "/" + filename;
        InputStream in = null;
        switch (filename){
            case (Document.KODEKS_KARNY_FILENAME):
                in = getResources().openRawResource(R.raw.kk);
                break;
            case (Question.QUESTIONS_FILENAME):
                in = getResources().openRawResource(R.raw.questions);
                break;
            case (Document.WZORCOWY_REGULAMIN_STRZELNIC_FILENAME):
                in = getResources().openRawResource(R.raw.regulamin);
                break;
            case (Document.ROZPORZĄDZENIE_DEPONOWANIE_BRONI_FILENAME):
                in = getResources().openRawResource(R.raw.deponowanie);
                break;
            case (Document.ROZPORZADZENIE_EGZAMIN_FILENAME):
                in = getResources().openRawResource(R.raw.egzamin);
                break;
            case (Document.ROZPORZĄDZENIE_NOSZENIE_FILENAME):
                in = getResources().openRawResource(R.raw.noszenie);
                break;
            case (Document.ROZPORZĄDZENIE_PRZEWOŻENIE_FILENAME):
                in = getResources().openRawResource(R.raw.przewozenie);
                break;
            case (Document.USTAWA_O_BRONI_I_AMUNICJI_FILENAME):
                in = getResources().openRawResource(R.raw.uobia);
                break;
            default:
                throw new RuntimeException();
        }
        File file = new File(path);
        file.createNewFile();
        FileOutputStream out = new FileOutputStream(path);
        byte[] buff = new byte[1024];
        int read = 0;
        try {
            while ((read = in.read(buff)) > 0) {
                out.write(buff, 0, read);
            }
        } finally {
            in.close();
            out.close();
        }
    }

    private void downloadNext() {
        if (filesNames.size() > 0) {
            filesNames.remove(0);
            downloadFirst();
        } else {
            broadcastDownloadingFinished();
        }
    }

    private boolean fileIsOutdated(StorageMetadata storageMetadata, String filename) {
        return getModificationDate(filename) < storageMetadata.getUpdatedTimeMillis();
    }

    private void downloadFirst() {
        failedAttempts = 0;
        if (filesNames.size() > 0) {
            downloadResource(getApplicationContext());
        } else {
            broadcastDownloadingFinished();
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

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void downloadFile(final String filename) {
        String path = getApplicationContext().getFilesDir().getAbsolutePath() + "/" + filename;
        File file = new File(path);
        StorageReference fileRef = mStorage.getReference().child(filename);
        fileRef.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Log.i(TAG, String.format("file %s downloaded", filename));
                broadcastDownloadingUpdate(String.format("plik %s został pmyślnie pobrany", filename));
                downloadNext();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                failedAttempts++;
                if (failedAttempts < MAX_FAILED_ATTEMPTS) {
                    Log.w(TAG, String.format("problem downloading %s try %d out of %d", filename, failedAttempts, MAX_FAILED_ATTEMPTS));
                    downloadFile(filename);
                } else {
                    Log.w(TAG, String.format("file %s could not be downloaded", filename));
                    broadcastDownloadingUpdate(String.format("nieudało się pobrać pliku %s", filename));
                    downloadNext();
                }
            }
        });
    }

    public boolean isFilePresent(String fileName) {
        String path = getApplicationContext().getFilesDir().getAbsolutePath() + "/" + fileName;
        File file = new File(path);
        if (file.exists()) {
            Log.w(TAG, String.format("file %s exists", fileName));
        } else {
            Log.w(TAG, String.format("file %s does not exists", fileName));
        }
        return file.exists();
    }

    public long getModificationDate(String fileName) {
        String path = getApplicationContext().getFilesDir().getAbsolutePath() + "/" + fileName;
        File file = new File(path);
        return file.lastModified();
    }


    private void broadcastDownloadingUpdate(String extra) {
        Intent updateIntent = new Intent(ACTION_DOWNLOADING_UPDATE);
        updateIntent.putExtra(EXTRA_STAGE, extra);

        sendBroadcast(updateIntent);
    }

    private void broadcastDownloadingFinished() {
        Log.i(TAG, "broadcastDownloadingFinished");
        sendBroadcast(new Intent(ACTION_DOWNLOADING_FINISHED));
    }
}
