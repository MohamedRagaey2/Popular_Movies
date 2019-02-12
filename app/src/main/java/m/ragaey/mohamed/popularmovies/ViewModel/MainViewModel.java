package m.ragaey.mohamed.popularmovies.ViewModel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.util.Log;

import java.util.List;

import m.ragaey.mohamed.popularmovies.database.AppDatabase;
import m.ragaey.mohamed.popularmovies.database.FavoriteEntry;

public class MainViewModel extends AndroidViewModel {

    // Constant for logging
    private static final String TAG = MainViewModel.class.getSimpleName();

    private LiveData<List<FavoriteEntry>> favorite;

    public MainViewModel(Application application) {
        super(application);
        AppDatabase database = AppDatabase.getInstance(this.getApplication());
        Log.d(TAG, "Actively retrieving the tasks from the DataBase");
        favorite = database.favoriteDao().loadAllFavorite();
    }

    public LiveData<List<FavoriteEntry>> getFavorite() {
        return favorite;
    }
}
