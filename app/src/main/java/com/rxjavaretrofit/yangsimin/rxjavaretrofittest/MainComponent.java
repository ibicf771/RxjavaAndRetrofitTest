package com.rxjavaretrofit.yangsimin.rxjavaretrofittest;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by yangsimin on 2017/8/12.
 */

import javax.inject.Singleton;

import dagger.Component;

    /**
     * Created by MH on 2016/7/14.
     */
    @Singleton
    @Component(modules = MainActivity.MainModule.class)  // 作为桥梁，沟通调用者和依赖对象库
    public interface MainComponent {
        //定义注入的方法
         void inject(MainActivity activity);

//        void inject(MainActvity actvity);
    }
