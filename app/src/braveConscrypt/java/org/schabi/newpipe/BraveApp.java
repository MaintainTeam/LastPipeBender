package org.schabi.newpipe;

import android.app.Application;

import org.conscrypt.Conscrypt;

import java.security.Security;

public class BraveApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Security.insertProviderAt(Conscrypt.newProvider(), 1);
    }
}
