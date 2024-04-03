package org.schabi.newpipe;

import android.os.Build;

import org.conscrypt.Conscrypt;
import org.schabi.newpipe.util.BraveTLSSocketFactory;

import java.security.Security;

import androidx.multidex.MultiDexApplication;

public class BraveApp extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        Security.insertProviderAt(Conscrypt.newProvider(), 1);

        // enable TLS1.2/1.3 for <=kitkat devices, to fix download and play for
        // media.ccc.de, rumble, soundcloud, peertube sources
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            BraveTLSSocketFactory.setAsDefault();
        }
    }
}
