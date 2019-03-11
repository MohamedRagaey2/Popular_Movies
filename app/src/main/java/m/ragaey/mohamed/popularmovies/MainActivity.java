package m.ragaey.mohamed.popularmovies;

import android.app.Activity;
import android.app.ProgressDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import m.ragaey.mohamed.popularmovies.Adapter.MoviesAdapter;
import m.ragaey.mohamed.popularmovies.Api.Client;
import m.ragaey.mohamed.popularmovies.Api.Service;
import m.ragaey.mohamed.popularmovies.Model.Movie;
import m.ragaey.mohamed.popularmovies.Model.MoviesResponse;
import m.ragaey.mohamed.popularmovies.ViewModel.MainViewModel;
import m.ragaey.mohamed.popularmovies.database.FavoriteEntry;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {


    private RecyclerView recyclerView;
    private MoviesAdapter adapter;
    private List<Movie> movieList;
    ProgressDialog pd;
    private SwipeRefreshLayout swipeContainer;
    private AppCompatActivity activity = MainActivity.this;

    public static final String LOG_TAG = MoviesAdapter.class.getName();

    private static String LIST_STATE = "list_state";
    private Parcelable savedRecyclerLayoutState;
    private static final String BUNDLE_RECYCLER_LAYOUT = "recycler_layout";
    private ArrayList<Movie> moviesInstance = new ArrayList<>();

    int cacheSize = 10 * 1024 * 1024; // 10 MiB

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (savedInstanceState != null) {
            moviesInstance = savedInstanceState.getParcelableArrayList(LIST_STATE);
            savedRecyclerLayoutState = savedInstanceState.getParcelable(BUNDLE_RECYCLER_LAYOUT);
            displayData();
        } else {
            initViews();
        }
    }

    private void displayData() {
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        adapter = new MoviesAdapter(this, moviesInstance);
        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        }
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        restoreLayoutManagerPosition();
        adapter.notifyDataSetChanged();
    }

    private void restoreLayoutManagerPosition() {
        if (savedRecyclerLayoutState != null) {
            recyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
        }
    }


    private void initViews() {
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        }

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        loadJSON();
        checkSortOrder();
    }

    private void initViews2() {
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        getAllFavorite();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putParcelableArrayList(LIST_STATE, moviesInstance);
        savedInstanceState.putParcelable(BUNDLE_RECYCLER_LAYOUT, recyclerView.getLayoutManager().onSaveInstanceState());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        moviesInstance = savedInstanceState.getParcelableArrayList(LIST_STATE);
        savedRecyclerLayoutState = savedInstanceState.getParcelable(BUNDLE_RECYCLER_LAYOUT);
        super.onRestoreInstanceState(savedInstanceState);
    }

    public Activity getActivity() {
        Context context = this;
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;

    }

    private void loadJSON(){
        String sortOrder = checkSortOrder();

        if (sortOrder.equals(this.getString(R.string.sort_most_popular))) {

            try {
                if (BuildConfig.TMDB_API_KEY.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please obtain API Key firstly from themoviedb.org", Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                    return;
                }

                Client Client = new Client();
                Service apiService =
                        m.ragaey.mohamed.popularmovies.Api.Client.getClient().create(Service.class);
                Call<MoviesResponse> call = apiService.getPopularMovies(BuildConfig.TMDB_API_KEY);
                call.enqueue(new Callback<MoviesResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<MoviesResponse> call, @NonNull Response<MoviesResponse> response) {
                        if (response.isSuccessful()) {Toast.makeText(MainActivity.this, "the value is " + moviesInstance.size(), Toast.LENGTH_SHORT).show();

                            if (response.body() != null) {
                                List<Movie> movies = response.body().getResults();
                                moviesInstance.clear();
                                moviesInstance.addAll(movies);
                                recyclerView.setAdapter(new MoviesAdapter(getApplicationContext(), movies));
                                recyclerView.smoothScrollToPosition(0);
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<MoviesResponse> call, @NonNull Throwable t) {
                        Log.d("Error", t.getMessage());
                        Toast.makeText(MainActivity.this, "Error Fetching Data!", Toast.LENGTH_SHORT).show();

                    }
                });
            } catch (Exception e) {
                Log.d("Error", e.getMessage());
                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
            }
        }else if (sortOrder.equals(this.getString(R.string.favorite))){
            initViews2();
        }else {

            try {
                if (BuildConfig.TMDB_API_KEY.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please obtain API Key firstly from themoviedb.org", Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                    return;
                }

                Client Client = new Client();
                Service apiService =
                        m.ragaey.mohamed.popularmovies.Api.Client.getClient().create(Service.class);
                Call<MoviesResponse> call = apiService.getTopRatedMovies(BuildConfig.TMDB_API_KEY);
                call.enqueue(new Callback<MoviesResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<MoviesResponse> call, @NonNull Response<MoviesResponse> response) {
                        assert response.body() != null;
                        List<Movie> movies = response.body().getResults();
                        moviesInstance.clear();
                        moviesInstance.addAll(movies);
                        recyclerView.setAdapter(new MoviesAdapter(getApplicationContext(), movies));
                        recyclerView.smoothScrollToPosition(0);
                    }

                    @Override
                    public void onFailure(@NonNull Call<MoviesResponse> call, @NonNull Throwable t) {
                        Log.d("Error", t.getMessage());
                        Toast.makeText(MainActivity.this, "Error Fetching Data!", Toast.LENGTH_SHORT).show();

                    }
                });
            } catch (Exception e) {
                Log.d("Error", e.getMessage());
                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isNetworkAvailable () {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    @Override
    public boolean onCreateOptionsMenu (Menu menu){

        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item){

        switch (item.getItemId()) {
            case R.id.menu_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onSharedPreferenceChanged (SharedPreferences sharedPreferences, String s){
        Log.d(LOG_TAG, "Preferences updated");
        checkSortOrder();
    }

    private String checkSortOrder () {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        return preferences.getString(
                this.getString(R.string.pref_sort_order_key),
                this.getString(R.string.sort_most_popular)
        );
    }

    private void getAllFavorite () {

        MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.getFavorite().observe(this, new Observer<List<FavoriteEntry>>() {
            @Override
            public void onChanged(@Nullable List<FavoriteEntry> imageEntries) {
                List<Movie> movies = new ArrayList<>();
                assert imageEntries != null;
                for (FavoriteEntry entry : imageEntries) {
                    Movie movie = new Movie();
                    movie.setId(entry.getMovieid());
                    movie.setOverview(entry.getOverview());
                    movie.setOriginalTitle(entry.getTitle());
                    movie.setPosterPath(entry.getPosterpath());
                    movie.setVoteAverage(entry.getUserrating());

                    movies.add(movie);
                }

                adapter.setMovies(movies);
            }
        });
    }
}

