package com.ponyvillelive.pvlmobile.net;

import android.app.Application;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.ponyvillelive.pvlmobile.model.Convention;
import com.ponyvillelive.pvlmobile.model.Event;
import com.ponyvillelive.pvlmobile.model.NowPlayingMeta;
import com.ponyvillelive.pvlmobile.model.Show;
import com.ponyvillelive.pvlmobile.model.Station;
import com.ponyvillelive.pvlmobile.model.net.ArrayResponse;
import com.ponyvillelive.pvlmobile.model.net.MapResponse;
import com.ponyvillelive.pvlmobile.model.net.ObjectResponse;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;
import retrofit2.RxJavaCallAdapterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

/**
 * <p>The code contract for the Ponyville Live! PonyvilleAPI as documented on
 * <a href="http://docs.ponyvillelive.apiary.io/">Apiary</a></p>
 */
public interface PonyvilleAPI {


    @GET("nowplaying")
    Observable<MapResponse<String, NowPlayingMeta>> getNowPlaying();

    @GET("nowplaying/index/id/{id}")
    Observable<ObjectResponse<NowPlayingMeta>> getNowPlayingForStation(@Path("id") int id);

    @GET("station/list")
    Observable<ArrayResponse<Station>> getStationList();

    @GET("station/list/category/{category}")
    Observable<ArrayResponse<Station>> getStationList(@Path("category") String category);

    @GET("show/latest")
    Observable<ArrayResponse<Show>> getShows();

    @GET("show/index")
    Observable<ArrayResponse<Show>> getAllShows();

    @GET("show/index/id/{id}")
    Observable<ObjectResponse<Show>> getShow(@Path("id") String id);

    @GET("convention/list")
    Observable<ArrayResponse<Convention>> getConventionList();

    @GET("convention/view/id/{id}")
    Observable<ObjectResponse<Convention>> getConvention(@Path("id") String id);

    @GET("schedule")
    Observable<ArrayResponse<Event>> getFullSchedule();

    @GET("schedule/index/station/{shortcode}")
    Observable<ArrayResponse<Event>> getStationSchedule(@Path("shortcode") String shortcode);

    /**
     * <p>A builder class for {@link PonyvilleAPI}. A default builder will use
     * http://ponyvillelive.com/api as the PonyvilleAPI host</p>
     */
    class Builder {
        static private String hostUrl;
        static private OkHttpClient client;
        static private GsonConverterFactory converter;
        static private PonyvilleLiveConverter cleaner;
        static private RxJavaCallAdapterFactory adapter;
        static private PonyvilleAPI ponyvilleApi;

        public static final int CACHE_SIZE = 50 * 1024 * 1024; // 50MB

        public static PonyvilleAPI build(Application app) {
            if (ponyvilleApi != null) return ponyvilleApi;

            if(hostUrl == null) hostUrl = "http://ponyvillelive.com/api/";
            if(client == null) {
                File cacheDir = new File(app.getCacheDir(), "http");
                Cache cache = new Cache(cacheDir, CACHE_SIZE);
                client = new OkHttpClient.Builder()
                        .cache(cache)
                        .build();
            }
            if(converter == null) converter = GsonConverterFactory.create();
            if(cleaner == null) cleaner = PonyvilleLiveConverter.create();
            if(adapter == null) adapter = RxJavaCallAdapterFactory.create();

            ponyvilleApi = new Retrofit.Builder()
                    .baseUrl(hostUrl)
                    .addConverterFactory(cleaner)
                    .addCallAdapterFactory(adapter)
                    .client(client)
                    .build()
                    .create(PonyvilleAPI.class);
            Log.d("PVL", "PonyvilleAPI interface created: " + ponyvilleApi.toString());
            return ponyvilleApi;
        }
    }

    class PonyvilleLiveConverter extends Converter.Factory {

        private final Gson gson;

        public static PonyvilleLiveConverter create() {
            return create(new Gson());
        }
        public static PonyvilleLiveConverter create(Gson gson) {
            return new PonyvilleLiveConverter(gson);
        }

        private PonyvilleLiveConverter(Gson gson) {
            if (gson == null) throw new NullPointerException("gson == null");
            this.gson = gson;
        }

        @Override
        public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations,
                                                                Retrofit retrofit) {
            final TypeAdapter<?> adapter = gson.getAdapter(TypeToken.get(type));
            return new PonyvilleLiveCleaner<>(adapter);
        }
    }

    final class PonyvilleLiveCleaner<T> implements Converter<ResponseBody, T> {
        private final TypeAdapter<T> adapter;

        PonyvilleLiveCleaner(TypeAdapter<T> adapter) {
            this.adapter = adapter;
        }

        @Override public T convert(ResponseBody value) throws IOException {
            String result = value.string();
            String cleaned;
            cleaned = result.replace("\"current_song\":[],", "");
            try {
                return adapter.fromJson(cleaned);
            } finally {
                if (cleaned != null) {
                    cleaned = null;
                }
            }
        }
    }

}
