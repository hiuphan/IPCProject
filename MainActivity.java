package edu.utsa.cs3443.servera;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class MyServerService extends Service {

    private static final String TAG = "MyServerService";

    private final ArrayList<ICallback> callbacks = new ArrayList<>();
    private boolean isRunning = true;
    private Thread workerThread;

    // ==============================
    // AIDL Binder
    // ==============================
    private final IServer.Stub binder = new IServer.Stub() {

        @Override
        public void registerCallback(ICallback cb) {
            if (cb == null) {
                Log.e(TAG, "registerCallback: NULL callback → ignore");
                return;
            }

            try {
                // ⬅ Client tự nhận chính nó
                cb.registerCallback(cb);
            } catch (RemoteException e) {
                Log.e(TAG, "registerCallback: callback failed", e);
                return;
            }

            if (!callbacks.contains(cb)) {
                callbacks.add(cb);
                Log.d(TAG, "Callback added: " + cb);
            }

            Log.d(TAG, "Client Count = " + callbacks.size());
        }

        @Override
        public void unregisterCallback(ICallback cb) {
            if (cb == null) return;

            callbacks.remove(cb);

            Log.d(TAG, "Callback removed: " + cb);
            Log.d(TAG, "Client Count = " + callbacks.size());
        }

        @Override
        public int getClientCount() {
            return callbacks.size();
        }
    };

    // ==============================
    // Service Lifecycle
    // ==============================
    @Override
    public void onCreate() {
        super.onCreate();
        startWorkerLoop();
        Log.d(TAG, "Service created");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Client bound to service");
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        if (workerThread != null) workerThread.interrupt();
        Log.d(TAG, "Service destroyed");
    }

    // ==============================
    // Data Sender Loop
    // ==============================
    private void startWorkerLoop() {
        workerThread = new Thread(() -> {
            Random rand = new Random();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

            while (isRunning) {
                try {
                    Thread.sleep(5000); // 5 seconds

                    String timestamp = sdf.format(new Date());
                    int valueA = rand.nextInt(101);
                    int valueB = rand.nextInt(101);
                    float avg = (valueA + valueB) / 2f;

                    sendToAllClients(timestamp, valueA, valueB, avg);

                } catch (Exception e) {
                    Log.e(TAG, "Worker error", e);
                }
            }
        });

        workerThread.start();
    }

    private void sendToAllClients(String time, int a, int b, float avg) {
        for (ICallback cb : new ArrayList<>(callbacks)) {
            try {
                cb.onDataReceived(time, a, b, avg);
            } catch (RemoteException e) {
                Log.e(TAG, "Dead client removed", e);
                callbacks.remove(cb);
            }
        }
    }
}
