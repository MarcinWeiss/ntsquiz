package medrawd.is.awesome.ntsquiz.storage;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static medrawd.is.awesome.ntsquiz.LoadingActivity.ACTION_DOWNLOADING_FINISHED;
import static medrawd.is.awesome.ntsquiz.LoadingActivity.ACTION_DOWNLOADING_UPDATE;
import static medrawd.is.awesome.ntsquiz.LoadingActivity.EXTRA_STAGE;

public class RemoteResourcesService extends IntentService {
    public static final int MAX_FAILED_ATTEMPTS = 3;
    public static final String EXTRA_FILES_NAMES = "medrawd.is.awesome.ntsquiz.storage.action.FILES_NAMES";
    private static final String TAG = RemoteResourcesService.class.getSimpleName();
    private static final String ACTION_DOWNLOAD_RESOURCES = "medrawd.is.awesome.ntsquiz.storage.action.DOWNLOAD_RESOURCES";
    private static final String ACTION_DOWNLOAD_RESOURCE = "medrawd.is.awesome.ntsquiz.storage.action.DOWNLOAD_RESOURCE";
    private static List<String> filesNames;
    ;
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

    public static void downloadResource(Context context) {
        Intent intent = new Intent(context, RemoteResourcesService.class);
        intent.setAction(ACTION_DOWNLOAD_RESOURCE);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_DOWNLOAD_RESOURCES.equals(action)) {
                filesNames = new ArrayList<>(Arrays.asList(intent.getStringArrayExtra(EXTRA_FILES_NAMES)));
                broadcastDownloadingUpdate("sprawdzanie plików");
                downladFirst();
            } else if (ACTION_DOWNLOAD_RESOURCE.equals(action)) {
                downloadFileIfNeeded();
            }
        }
    }

    private void downloadFileIfNeeded() {
        final String filename = filesNames.get(0);
        Log.i(TAG, String.format("downloadFileIfNeeded %s", filename));
        if (isNetworkAvailable()) {
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
                    downloadNext();
                }
            });
        } else {
            downloadNext();
        }
    }

    private void downloadNext() {
        filesNames.remove(0);
        downladFirst();
    }

    private boolean fileIsOutdated(StorageMetadata storageMetadata, String filename) {
        return getModificationDate(filename) < storageMetadata.getUpdatedTimeMillis();
    }

    private void downladFirst() {
        failedAttempts = 0;
        if (filesNames.size() > 0) {
            downloadResource(getApplicationContext());
        } else {
            broadcastDownloadingFinished();
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
