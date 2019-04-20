package m.ragaey.mohamed.popularmovie.Network;

import m.ragaey.mohamed.popularmovie.Models.MoviesResponse;
import m.ragaey.mohamed.popularmovie.Models.ReviewsResponse;
import m.ragaey.mohamed.popularmovie.Models.TrailersResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface MovieAPI {

    @GET("movie/popular?api_key=8ecd3f11acce577f1fcbc110a44a054b")
    Call<MoviesResponse> getPopularMovies();

    @GET("movie/top_rated?api_key=8ecd3f11acce577f1fcbc110a44a054b")
    Call<MoviesResponse> getTopRatedMovies();

    @GET("movie/{id}/videos?api_key=8ecd3f11acce577f1fcbc110a44a054b")
    Call<TrailersResponse> getTrailers(@Path("id") int id);

    @GET("movie/{id}/reviews?api_key=8ecd3f11acce577f1fcbc110a44a054b")
    Call<ReviewsResponse> getReviews(@Path("id") int id);
}
