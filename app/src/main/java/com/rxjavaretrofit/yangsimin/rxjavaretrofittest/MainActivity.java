package com.rxjavaretrofit.yangsimin.rxjavaretrofittest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;


import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;


public class MainActivity extends AppCompatActivity {
    private static String TAG = "Rxjava";

    @Inject   //标明需要注入的对象
    Person person;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rxjavaFunc();

        requestUrl();

        retrofitAndRxjava();

        daggerTest();
    }

    //------------------------------------Retrofit------------------------------------------------//

    private void requestUrl() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.github.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        GitHub gitHubService = retrofit.create(GitHub.class);
        Call<List<Contributor>> call = gitHubService.contributors("square", "retrofit");


        //同步
//        try {
//            Response<List<Contributor>> response = call.execute(); // 同步

//            Log.d(TAG, "response:" + response.body().toString());
//            List<Contributor> list = response.body();
//            if (list != null) {
//                for (int i = 0; i < list.size(); i++) {
//                    Log.d(TAG, list.get(i).toString());
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


        //异步
        call.enqueue(new Callback<List<Contributor>>() {
            @Override
            public void onResponse(Call<List<Contributor>> call, Response<List<Contributor>> response) {
                List<Contributor> list = response.body();
                Log.d(TAG, response.body().toString());
                if (list != null) {
                    for (int i = 0; i < list.size(); i++) {
                        Log.d(TAG, list.get(i).toString());
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Contributor>> call, Throwable t) {

            }
        });
    }


    public class Contributor {
        public final String login;
        public final int contributions;

        public Contributor(String login, int contributions) {
            this.login = login;
            this.contributions = contributions;
        }

        @Override
        public String toString() {
            return "Contributor{" +
                    "login='" + login + '\'' +
                    ", contributions=" + contributions +
                    '}';
        }
    }

    public interface GitHub {
        @GET("/repos/{owner}/{repo}/contributors")
        Call<List<Contributor>> contributors(
                @Path("owner") String owner,
                @Path("repo") String repo);
    }



    //--------------------------------Rxjava--------------------------------------//
    private void rxjavaFunc(){
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                e.onNext(1);
                e.onNext(2);
                e.onComplete();
                Log.d(TAG, "observale thread " + Thread.currentThread().getName());
            }
        }).subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .map(new Function<Integer, String>() {
                    @Override
                    public String apply(Integer integer) throws Exception {
                        return integer.toString()+"ofoucrose";
                    }
                })
                .flatMap(new Function<String, ObservableSource<String>>() {
                    @Override
                    public ObservableSource<String> apply(String s) throws Exception {
                        ArrayList<String> list = new ArrayList<String>();
                        for (int i = 0; i <2 ; i++) {
                            list.add(s + i);
                        }
                        return Observable.fromIterable(list);

                    }
                })
                .concatMap(new Function<String, ObservableSource<String>>() {
                    @Override
                    public ObservableSource<String> apply(String s) throws Exception {
                        ArrayList<String> list = new ArrayList<String>();
                        for (int i = 0; i <2 ; i++) {
                            list.add(s + i);
                        }
                        return Observable.fromIterable(list);
                    }
                })
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        Log.d(TAG, "int " + s);
                        Log.d(TAG, "observer thread " + Thread.currentThread().getName());
                    }
                });







        Observable<Integer> observable1 = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                e.onNext(1);
                e.onNext(11);
                e.onNext(111);

            }
        }).subscribeOn(Schedulers.io());


        Observable<Integer> observiable2 = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                e.onNext(2);
                e.onNext(22);
            }
        }).subscribeOn(Schedulers.newThread());

        Observable.zip(observable1, observiable2, new BiFunction<Integer, Integer, String>(){
            @Override
            public String apply(Integer integer, Integer integer2) throws Exception {
                return "" + integer + integer2;
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                Log.d(TAG, "result " + s);
            }
        });


        Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(FlowableEmitter<Integer> e) throws Exception {
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);


            }
        }, BackpressureStrategy.ERROR).doOnNext(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                integer++;
            }
        }).filter(new Predicate<Integer>() {
            @Override
            public boolean test(Integer integer) throws Exception {
//                if (integer == 1) {
//                    return false;
//                }
                return true;
            }

        }).throttleLast(100, TimeUnit.MILLISECONDS)
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        s.request(3);
                    }

                    @Override
                    public void onNext(Integer integer) {
                        Log.d(TAG, "onNext: " + integer);
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.w(TAG, "onError: ", t);
                    }

                    @Override
                    public void onComplete() {
                        Log.w(TAG, "onComplete: ");
                    }
                });
    }


    //-----------------------------------dagger2-----------------------------------//

    private void daggerTest(){
        // 构造桥梁对象
        MainComponent component = DaggerMainComponent.builder().mainModule(new MainModule()).build();

        //注入
        component.inject(this);
    }

    @Module   //提供依赖对象的实例
    public class MainModule {

        @Provides
            // 关键字，标明该方法提供依赖对象
        Person providerPerson(){
            //提供Person对象
            return new Person();
        }
    }

    public class Person {

        public Person(){
            Log.i(TAG,"person create!!!");
        }
    }


    //---------------------------------retrofit & rxjava-------------------------------------------//

    private void retrofitAndRxjava() {

        Log.d(TAG, "retrofitAndRxjava " );

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.github.com")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        GitHubObservable gitHubObservable = retrofit.create(GitHubObservable.class);
        Observable<List<Contributor>> call = gitHubObservable.contributors("square", "retrofit");


        call.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Contributor>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        Log.d(TAG, "retrofitAndRxjava onSubscribe");
                    }

                    @Override
                    public void onNext(@NonNull List<Contributor> contributors) {
                        Log.d(TAG, "retrofitAndRxjava onNext");
                        if (contributors != null) {
                            for (int i = 0; i < contributors.size(); i++) {
                                Log.d(TAG, "retrofitAndRxjava " + contributors.get(i).toString());
                            }
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                        Log.d(TAG, "retrofitAndRxjava onError " + e.toString());
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "retrofitAndRxjava onComplete");
                    }
                });

    }

    public interface GitHubObservable {
        @GET("/repos/{owner}/{repo}/contributors")
        Observable<List<Contributor>> contributors(
                @Path("owner") String owner,
                @Path("repo") String repo);
    }


}
