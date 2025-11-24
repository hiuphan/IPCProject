package com.example.servera;

interface IServer {
    int registerCallback(ICallback cb);
    void unregisterCallback(ICallback cb);
    int getRegisteredCallbackCount();
}

DataModel

package com.example.servera;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class DataModel implements Parcelable {

    private String timestamp;
    private int valueA;
    private int valueB;
    private float average;

    public DataModel(String timestamp, int valueA, int valueB, float average) {
        this.timestamp = timestamp;
        this.valueA = valueA;
        this.valueB = valueB;
        this.average = average;

        Log.d("DataModel", "Created: " + toString());
    }

    protected DataModel(Parcel in) {
        timestamp = in.readString();
        valueA = in.readInt();
        valueB = in.readInt();
        average = in.readFloat();
    }

    public static final Creator<DataModel> CREATOR = new Creator<DataModel>() {
        @Override
        public DataModel createFromParcel(Parcel in) {
            return new DataModel(in);
        }

        @Override
        public DataModel[] newArray(int size) {
            return new DataModel[size];
        }
    };

    public String getTimestamp() { return timestamp; }
    public int getValueA() { return valueA; }
    public int getValueB() { return valueB; }
    public float getAverage() { return average; }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(timestamp);
        parcel.writeInt(valueA);
        parcel.writeInt(valueB);
        parcel.writeFloat(average);
    }

    @Override
    public String toString() {
        return "{time=" + timestamp +
                ", A=" + valueA +
                ", B=" + valueB +
                ", avg=" + average + "}";
    }
}



DataGenerator
package com.example.servera;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class DataGenerator {

    private final Random random = new Random();
    private final SimpleDateFormat sdf =
            new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    public String generateTimestamp() {
        return sdf.format(new Date());
    }

    public int generateValueA() {
        return random.nextInt(101);
    }

    public int generateValueB() {
        return random.nextInt(101);
    }

    public float computeAverage(int a, int b) {
        return (a + b) / 2f;
    }
}


Service 
package com.example.servera;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.concurrent.CopyOnWriteArrayList;

public class DataBroadcastService extends Service {

    private static final String TAG = "ServerA_Service";

    private final Handler handler = new Handler();
    private boolean isRunning = false;

    private final DataGenerator generator = new DataGenerator();
    private final CopyOnWriteArrayList<ICallback> callbacks = new CopyOnWriteArrayList<>();

    // ================= AIDL BINDER =================
    private final IServer.Stub binder = new IServer.Stub() {

        @Override
        public int registerCallback(ICallback cb) {
            if (cb != null && !callbacks.contains(cb)) {
                callbacks.add(cb);
                Log.d(TAG, "Client registered. Count = " + callbacks.size());
            }
            return callbacks.size();
        }

        @Override
        public void unregisterCallback(ICallback cb) {
            if (cb != null && callbacks.contains(cb)) {
                callbacks.remove(cb);
                Log.d(TAG, "Client unregistered. Count = " + callbacks.size());
            }
        }

        @Override
        public int getRegisteredCallbackCount() {
            return callbacks.size();
        }
    };

    // ================= BROADCAST TASK =================
    private final Runnable broadcastTask = new Runnable() {
        @Override
        public void run() {

            String time = generator.generateTimestamp();
            int valueA = generator.generateValueA();
            int valueB = generator.generateValueB();
            float avg = generator.computeAverage(valueA, valueB);

            DataModel data = new DataModel(time, valueA, valueB, avg);

            Log.d(TAG, "Generated: " + data);

            for (ICallback cb : callbacks) {
                try {
                    Log.d(TAG, "Sending to callback: " + cb);
                    cb.onDataReceived(data);
                } catch (Exception e) {
                    Log.e(TAG, "Callback failed → removing client", e);
                    callbacks.remove(cb);
                }
            }

            handler.postDelayed(this, 5000);
        }
    };

    // ================= SERVICE LIFECYCLE =================
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service Created");
        isRunning = true;
        handler.postDelayed(broadcastTask, 2000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service Destroyed");
        isRunning = false;
        handler.removeCallbacks(broadcastTask);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Service Bound → returning binder");
        return binder;
    }
}


MainActivity 
package com.example.servera;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "ServerA_Main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnStart = findViewById(R.id.btnStart);
        Button btnStop = findViewById(R.id.btnStop);

        btnStart.setOnClickListener(v -> {
            Log.d(TAG, "Start Service Pressed");
            startService(new Intent(this, DataBroadcastService.class));
        });

        btnStop.setOnClickListener(v -> {
            Log.d(TAG, "Stop Service Pressed");
            stopService(new Intent(this, DataBroadcastService.class));
        });
    }
}
