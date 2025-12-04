// package com.example.plugin;

// parcelable SafetyInfo;

// interface IPluginService {
//     void submitSafetyScore(int score);
//     SafetyInfo getSafetyInfo();
// }


// package com.example.plugin;

// parcelable SafetyInfo;


// package com.example.plugin;

// import android.os.Parcel;
// import android.os.Parcelable;

// public class SafetyInfo implements Parcelable {

//     public int current;
//     public int min;
//     public int avg;
//     public int max;

//     public SafetyInfo(int current, int min, int avg, int max) {
//         this.current = current;
//         this.min = min;
//         this.avg = avg;
//         this.max = max;
//     }

//     protected SafetyInfo(Parcel in) {
//         current = in.readInt();
//         min = in.readInt();
//         avg = in.readInt();
//         max = in.readInt();
//     }

//     public static final Creator<SafetyInfo> CREATOR = new Creator<SafetyInfo>() {
//         @Override
//         public SafetyInfo createFromParcel(Parcel in) {
//             return new SafetyInfo(in);
//         }

//         @Override
//         public SafetyInfo[] newArray(int size) {
//             return new SafetyInfo[size];
//         }
//     };

//     @Override
//     public void writeToParcel(Parcel dest, int flags) {
//         dest.writeInt(current);
//         dest.writeInt(min);
//         dest.writeInt(avg);
//         dest.writeInt(max);
//     }

//     @Override
//     public int describeContents() {
//         return 0;
//     }
// }


// public class PluginService extends Service {

//     private SafetyRepository repository;
//     private Executor executor = Executors.newSingleThreadExecutor();

//     @Override
//     public void onCreate() {
//         super.onCreate();
//         repository = new SafetyRepository(
//                 AppDatabase.getInstance(this).safetyDao()
//         );
//     }

//     private final IPluginService.Stub binder = new IPluginService.Stub() {

//         @Override
//         public void submitSafetyScore(int score) {
//             executor.execute(() -> repository.insert(score));
//         }

//         @Override
//         public SafetyInfo getSafetyInfo() {
//             SafetyRecord latest = repository.getCurrentMS();
//             SafetyRecord nearest = repository.getNearestAvailable();

//             Integer current = (latest != null && latest.safetyScore != null)
//                     ? latest.safetyScore
//                     : (nearest != null ? nearest.safetyScore : null);

//             int[] stats = repository.getStats();

//             return new SafetyInfo(
//                     current != null ? current : -1,
//                     stats != null ? stats[0] : -1,
//                     stats != null ? stats[1] : -1,
//                     stats != null ? stats[2] : -1
//             );
//         }
//     };

//     @Override
//     public IBinder onBind(Intent intent) {
//         return binder;
//     }
// }
// private IPluginService pluginService;

// private ServiceConnection conn = new ServiceConnection() {
//     @Override
//     public void onServiceConnected(ComponentName name, IBinder service) {
//         pluginService = IPluginService.Stub.asInterface(service);
//         loadData();
//     }

//     @Override
//     public void onServiceDisconnected(ComponentName name) {}
// };

// private void loadData() {
//     new Thread(() -> {
//         try {
//             SafetyInfo info = pluginService.getSafetyInfo();
//             runOnUiThread(() -> {
//                 tvCurrent.setText("MS(t): " + info.current);
//                 tvMin.setText("Min: " + info.min);
//                 tvAvg.setText("Avg: " + info.avg);
//                 tvMax.setText("Max: " + info.max);
//             });
//         } catch (Exception e) {
//             e.printStackTrace();
//         }
//     }).start();
// }
