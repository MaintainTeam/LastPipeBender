package org.schabi.newpipe;

import org.conscrypt.Conscrypt;

import java.security.Security;

import androidx.multidex.MultiDexApplication;

public class BraveApp extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        Security.insertProviderAt(Conscrypt.newProvider(), 1);
    }
}
