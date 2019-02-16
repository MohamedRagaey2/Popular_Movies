package m.ragaey.mohamed.popularmovies.database;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

@Database(entities = {FavoriteEntry.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract FavoriteDao favoriteDao();
    public  static AppDatabase Instance;
    public static AppDatabase getInstance(Context context)
    {
        if (Instance == null)
        {
            Instance =Room.databaseBuilder(context.getApplicationContext()
                    ,AppDatabase.class,"AppDatabase")
                    .addCallback(roomCallback)
                    .build();
        }
        return Instance;
    }

    private static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback()
    {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            new PopulateDbAsync(Instance).execute();
        }

        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            new PopulateDbAsync(Instance).execute();
        }
    };

    private static class PopulateDbAsync extends AsyncTask <Void, Void, Void>
    {
        private FavoriteDao favoriteDao;

        private PopulateDbAsync(AppDatabase db)
        {
            favoriteDao = db.favoriteDao();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            favoriteDao.insertFavorite(new FavoriteEntry("title","userrating","posterpath","overview"));
            return null;
        }
    }
}