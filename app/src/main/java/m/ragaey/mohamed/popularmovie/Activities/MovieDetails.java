package m.ragaey.mohamed.popularmovie.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import m.ragaey.mohamed.popularmovie.Adapters.ReviewsAdapter;
import m.ragaey.mohamed.popularmovie.Adapters.TrailersAdapter;
import m.ragaey.mohamed.popularmovie.Database.AppExecutors;
import m.ragaey.mohamed.popularmovie.Database.MovieDataBase;
import m.ragaey.mohamed.popularmovie.Models.Movie;
import m.ragaey.mohamed.popularmovie.Models.Review;
import m.ragaey.mohamed.popularmovie.Models.ReviewsResponse;
import m.ragaey.mohamed.popularmovie.Models.Trailer;
import m.ragaey.mohamed.popularmovie.Models.TrailersResponse;
import m.ragaey.mohamed.popularmovie.Network.ApiClient;
import m.ragaey.mohamed.popularmovie.Network.MovieAPI;
import m.ragaey.mohamed.popularmovie.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static m.ragaey.mohamed.popularmovie.Activities.MainActivity.BASE_BACKDROP_URL;

public class MovieDetails extends AppCompatActivity {

    private Movie movie = null;
    private Movie tempMovie = null;

    private TextView trailers_label = null;
    private TextView reviews_label = null;

    private List<Trailer> trailers = null;
    private List<Review> reviews = null;

    private TrailersAdapter trailersAdapter = null;
    private ReviewsAdapter reviewsAdapter = null;

    private RecyclerView trailersRecyclerView = null;
    private RecyclerView reviewsRecyclerView = null;
    private FloatingActionButton favorite_button = null;

    private MovieDataBase mDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        mDB = MovieDataBase.getInstance(getApplicationContext());

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("selectedMovie")) {
            movie = (Movie) intent.getSerializableExtra("selectedMovie");
        } else {
            Toast.makeText(this, "Error while loading movie details", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ImageView moviePoster =  findViewById(R.id.movie_poster);
        TextView movieTitle =  findViewById(R.id.movie_title);
        TextView movieRating =  findViewById(R.id.movie_rating);
        TextView movieReleaseDate =  findViewById(R.id.movie_release_date);
        TextView movieOverview =  findViewById(R.id.movie_overview);

        trailers_label = findViewById(R.id.trailers_label);
        reviews_label = findViewById(R.id.reviews_label);

        Uri posterUrl = null;
        String backdropPath = movie.getBackdropPath();
        if (backdropPath != null && !backdropPath.isEmpty())
            posterUrl = Uri.parse(BASE_BACKDROP_URL.concat(backdropPath));


        Glide.with(this).load(posterUrl).into(moviePoster);
        movieTitle.setText(movie.getTitle());
        movieRating.setText(String.valueOf(movie.getVoteAverage()));
        movieReleaseDate.setText(movie.getReleaseDate());
        movieOverview.setText(movie.getOverview());

        favorite_button = findViewById(R.id.favorite_button);

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                tempMovie = mDB.getDAO().getMovie(movie.getId());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (tempMovie != null) {
                            favorite_button.setImageDrawable(ContextCompat.getDrawable(MovieDetails.this, R.drawable.ic_star));
                        } else {
                            favorite_button.setImageDrawable(ContextCompat.getDrawable(MovieDetails.this, R.drawable.ic_star_border));
                        }
                    }
                });
            }
        });

        favorite_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {

                        if (tempMovie != null) {
                            mDB.getDAO().delete(movie);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    tempMovie = null;
                                    favorite_button.setImageDrawable(ContextCompat
                                            .getDrawable(MovieDetails.this, R.drawable.ic_star_border));
                                    Toast.makeText(getApplicationContext(), "Deleted", Toast.LENGTH_LONG).show();
                                }
                            });

                        } else {
                            mDB.getDAO().insert(movie);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    tempMovie = movie;
                                    favorite_button.setImageDrawable(ContextCompat
                                            .getDrawable(MovieDetails.this, R.drawable.ic_star));
                                    Toast.makeText(getApplicationContext(), "Added", Toast.LENGTH_LONG).show();
                                }
                            });

                        }
                    }
                });

            }
        });


        trailers = new ArrayList<>();
        reviews = new ArrayList<>();

        trailersAdapter = new TrailersAdapter(this);
        reviewsAdapter = new ReviewsAdapter(this);

        trailersRecyclerView = findViewById(R.id.trailers_recycler_view);
        trailersRecyclerView.setLayoutManager(new
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        trailersRecyclerView.setAdapter(trailersAdapter);

        reviewsRecyclerView = findViewById(R.id.reviews_recycler_view);
        reviewsRecyclerView.setLayoutManager(new
                LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        reviewsRecyclerView.setAdapter(reviewsAdapter);


        MovieAPI movieAPI = ApiClient.getClient().create(MovieAPI.class);

        Call<TrailersResponse> trailersResponseCall = movieAPI.getTrailers(movie.getId());
        Call<ReviewsResponse> reviewsResponseCall = movieAPI.getReviews(movie.getId());

        trailersResponseCall.enqueue(new Callback<TrailersResponse>() {
            @Override
            public void onResponse(Call<TrailersResponse> call, Response<TrailersResponse> response) {

                trailers.clear();
                assert response.body() != null;
                trailers = response.body().getResults();

                if (trailers == null || trailers.isEmpty()) {
                    trailers_label.setVisibility(View.GONE);
                    trailersRecyclerView.setVisibility(View.GONE);
                } else {
                    trailers_label.setVisibility(View.VISIBLE);
                    trailersRecyclerView.setVisibility(View.VISIBLE);

                    trailersAdapter.addNewTrailers(trailers);
                }

            }

            @Override
            public void onFailure(Call<TrailersResponse> call, Throwable t) {

            }
        });

        reviewsResponseCall.enqueue(new Callback<ReviewsResponse>() {
            @Override
            public void onResponse(Call<ReviewsResponse> call, Response<ReviewsResponse> response) {

                reviews.clear();
                assert response.body() != null;
                reviews = response.body().getResults();

                if (reviews == null || reviews.isEmpty()) {
                    reviews_label.setVisibility(View.GONE);
                    reviewsRecyclerView.setVisibility(View.GONE);
                } else {
                    reviews_label.setVisibility(View.VISIBLE);
                    reviewsRecyclerView.setVisibility(View.VISIBLE);

                    reviewsAdapter.addNewReviews(reviews);
                }

            }

            @Override
            public void onFailure(Call<ReviewsResponse> call, Throwable t) {

            }
        });

    }
}
