package edu.utsa.cs3443.servera;

import edu.utsa.cs3443.servera.IDataCallback;
import edu.utsa.cs3443.servera.IMyServer;
import edu.utsa.cs3443.servera.DataModel;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class MyServerService extends Service {

    private static final String TAG = "MyServerService";

    // Thread-safe list đăng ký callback từ client
    private final CopyOnWriteArrayList<IDataCallback> callbacks = new CopyOnWriteArrayList<>();

    // AIDL binder chính
    private final IMyServer.Stub binder = new IMyServer.Stub() {
        @Override
        public void registerCallback(IDataCallback cb) {
            if (cb != null && !callbacks.contains(cb)) {
                callbacks.add(cb);
                Log.d(TAG, "Client registered: " + cb);
            }
        }

        @Override
        public void unregisterCallback(IDataCallback cb) {
            if (cb != null) {
                callbacks.remove(cb);
                Log.d(TAG, "Client unregistered: " + cb);
            }
        }
    };

    private Thread workerThread;
    private boolean isRunning = true;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");
        startDataLoop();
    }

    private void startDataLoop() {
        workerThread = new Thread(() -> {
            Random random = new Random();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

            while (isRunning) {
                try {
                    Thread.sleep(5000); // gửi mỗi 5s

                    // Generate data
                    String time = sdf.format(new Date());
                    int valueA = random.nextInt(101);  // 0 → 100
                    int valueB = random.nextInt(101);  // 0 → 100
                    float average = (valueA + valueB) / 2.0f;

                    DataModel data = new DataModel(time, valueA, valueB, average);

                    Log.d(TAG, "Generated Data → " +
                            "time=" + time +
                            ", A=" + valueA +
                            ", B=" + valueB +
                            ", avg=" + average);

                    sendDataToAllClients(data);

                } catch (InterruptedException e) {
                    Log.e(TAG, "Worker thread interrupted", e);
                }
            }
        });

        workerThread.start();
    }

    private void sendDataToAllClients(DataModel data) {
        for (IDataCallback cb : callbacks) {
            try {
                cb.onDataReceived(data);
            } catch (RemoteException e) {
                Log.e(TAG, "Callback failed → removing client", e);
                callbacks.remove(cb); // gỡ client chết
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed");
        isRunning = false;
        if (workerThread != null) workerThread.interrupt();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Service bound");
        return binder;
    }
}
