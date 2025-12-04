// @Entity(tableName = "safety_records")
// public class SafetyRecord {

//     @PrimaryKey(autoGenerate = true)
//     public int id;

//     public long timestamp;

//     @Nullable
//     public Integer safetyScore;   // MS(t), có thể null

//     public SafetyRecord(long timestamp, @Nullable Integer safetyScore) {
//         this.timestamp = timestamp;
//         this.safetyScore = safetyScore;
//     }
// }


// @Dao
// public interface SafetyDao {

//     @Query("SELECT * FROM safety_records ORDER BY timestamp DESC LIMIT 1")
//     SafetyRecord getLatest();

//     @Query("SELECT * FROM safety_records WHERE safetyScore IS NOT NULL ORDER BY timestamp DESC LIMIT 1")
//     SafetyRecord getLatestAvailable();

//     @Insert(onConflict = OnConflictStrategy.REPLACE)
//     void insert(SafetyRecord record);

//     @Query("SELECT * FROM safety_records WHERE safetyScore IS NOT NULL")
//     List<SafetyRecord> getAllAvailable();
// }

//

// @Database(entities = {SafetyRecord.class}, version = 1)
// public abstract class AppDatabase extends RoomDatabase {

//     private static volatile AppDatabase INSTANCE;

//     public abstract SafetyDao safetyDao();

//     public static AppDatabase getInstance(Context context) {
//         if (INSTANCE == null) {
//             synchronized (AppDatabase.class) {
//                 if (INSTANCE == null) {
//                     INSTANCE = Room.databaseBuilder(
//                             context.getApplicationContext(),
//                             AppDatabase.class,
//                             "plugin-db"
//                     ).build();
//                 }
//             }
//         }
//         return INSTANCE;
//     }
// }

// public class SafetyRepository {

//     private final SafetyDao dao;

//     public SafetyRepository(SafetyDao dao) {
//         this.dao = dao;
//     }

//     public void insert(Integer score) {
//         dao.insert(new SafetyRecord(System.currentTimeMillis(), score));
//     }

//     public SafetyRecord getCurrentMS() {
//         return dao.getLatest();
//     }

//     public SafetyRecord getNearestAvailable() {
//         return dao.getLatestAvailable();
//     }

//     public int[] getStats() {
//         List<SafetyRecord> list = dao.getAllAvailable();
//         if (list.isEmpty()) return null;

//         int min = Integer.MAX_VALUE;
//         int max = Integer.MIN_VALUE;
//         int sum = 0;
//         int count = 0;

//         for (SafetyRecord r : list) {
//             if (r.safetyScore != null) {
//                 int s = r.safetyScore;
//                 sum += s;
//                 count++;
//                 min = Math.min(min, s);
//                 max = Math.max(max, s);
//             }
//         }

//         int avg = sum / count;
//         return new int[]{min, avg, max};
//     }
// }
