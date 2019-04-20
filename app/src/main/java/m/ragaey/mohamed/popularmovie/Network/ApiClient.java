package m.ragaey.mohamed.popularmovie.Network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static m.ragaey.mohamed.popularmovie.Activities.MainActivity.BASE_URL;

public final class ApiClient {

    private static Retrofit retrofit = null;

    public static Retrofit getClient(){
        if (retrofit == null){
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

}
