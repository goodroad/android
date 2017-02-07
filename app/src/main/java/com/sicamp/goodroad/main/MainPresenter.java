package com.sicamp.goodroad.main;

public class MainPresenter implements MainContract.Present {
    private MainContract.View mMainView;

    public MainPresenter(MainContract.View mainView) {
        this.mMainView = mainView;


    }
}
