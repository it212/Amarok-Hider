package deltazero.amarok;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;

import java.nio.file.Paths;

import deltazero.amarok.AppHider.AppHiderBase;
import deltazero.amarok.FileHider.FileHiderBase;
import deltazero.amarok.FileHider.ObfuscateFileHider;
import rikka.shizuku.ShizukuProvider;


public class Hider {

    private static final String TAG = "Hider";
    private static final HandlerThread hiderThread = new HandlerThread("HIDER_THREAD");
    private static final MutableLiveData<Boolean> isProcessing = new MutableLiveData<>(false);

    private final PrefMgr prefMgr;
    private final Context context;
    private final Handler threadHandler;

    private AppHiderBase appHider;
    private FileHiderBase fileHider;

    public MutableLiveData<Boolean> getIsProcessingLiveData() {
        return isProcessing;
    }

    public Hider(Context context) {
        this.context = context;
        prefMgr = new PrefMgr(context);

        // Init Background Handler
        if (hiderThread.getState() == Thread.State.NEW)
            hiderThread.start();
        threadHandler = new Handler(hiderThread.getLooper());

        // Enable shizukuProvider
        threadHandler.post(() -> ShizukuProvider.enableMultiProcessSupport(false));
    }

    public void hide() {
        threadHandler.post(() -> {
            isProcessing.postValue(true);
            syncHide();
            isProcessing.postValue(false);
            QuickHideService.stopService(context);
        });
    }

    public void unhide() {
        threadHandler.post(() -> {
            isProcessing.postValue(true);
            syncUnhide();
            isProcessing.postValue(false);
            QuickHideService.startService(context);
        });
    }

    public void forceUnhide() {
        if (Boolean.TRUE.equals(isProcessing.getValue())) {
            hiderThread.interrupt();
        }
        prefMgr.setIsHidden(true);
        unhide();
    }

    private void syncHide() {

        refreshHiders();

        try {
            appHider.hide(prefMgr.getHideApps());
            fileHider.hide(prefMgr.getHideFilePath());
        } catch (InterruptedException e) {
            Log.w(TAG, "Process 'hide' interrupted.");
            return;
        }

        prefMgr.setIsHidden(true);

        Log.i(TAG, "Process 'hide' finished.");
        Toast.makeText(context, R.string.hidden_toast, Toast.LENGTH_SHORT).show();
    }

    private void syncUnhide() {

        refreshHiders();

        try {
            appHider.unhide(prefMgr.getHideApps());
            fileHider.unhide(prefMgr.getHideFilePath());
        } catch (InterruptedException e) {
            Log.w(TAG, "Process 'unhide' interrupted.");
            return;
        }

        prefMgr.setIsHidden(false);

        Log.i(TAG, "Process 'unhide' finished.");
        Toast.makeText(context, R.string.unhidden_toast, Toast.LENGTH_SHORT).show();
    }

    private void refreshHiders() {
        appHider = prefMgr.getAppHider();
        fileHider = new ObfuscateFileHider(context);
    }

}
