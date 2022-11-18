package com.laxman.ipshare.models;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class DataModel extends ViewModel {

    private MutableLiveData<Boolean> webServer;
    private MutableLiveData<Boolean> dataChanged;

    public MutableLiveData<Boolean> getWebServer() {
        if (webServer == null) {
            webServer = new MutableLiveData<>();
        }
        return webServer;
    }

    public MutableLiveData<Boolean> getDataChanged() {
        if (dataChanged == null) {
            dataChanged = new MutableLiveData<>();
        }
        return dataChanged;
    }
}
