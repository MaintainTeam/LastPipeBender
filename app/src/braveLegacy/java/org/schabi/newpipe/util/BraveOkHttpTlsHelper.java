package org.schabi.newpipe.util;

import android.os.Build;
import android.util.Log;

import org.schabi.newpipe.BraveTag;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

import static org.schabi.newpipe.MainActivity.DEBUG;

public final class BraveOkHttpTlsHelper {

    private static final String TAG =
            new BraveTag().tagShort23(BraveOkHttpTlsHelper.class.getSimpleName());

    private BraveOkHttpTlsHelper() {
    }

    /**
     * Enable TLS 1.3 and 1.2 on Android Kitkat. This function is mostly taken
     * from the documentation of OkHttpClient.Builder.sslSocketFactory(_,_).
     *
     * The keystore part is inspired by https://stackoverflow.com/a/65395783/4116659
     * <p>
     * If there is an error, the function will safely fall back to doing nothing
     * and printing the error to the console.
     * </p>
     *
     * @param builder The HTTPClient Builder on which TLS is enabled on (will be modified in-place)
     * @return the same builder that was supplied. So the method can be chained.
     */
    public static OkHttpClient.Builder enableModernTLS(final OkHttpClient.Builder builder) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            try {

                final BraveTLSSocketFactory sslSocketFactory = BraveTLSSocketFactory.getInstance();
                final TrustManagerFactory trustManagerFactory = sslSocketFactory
                        .getTrustManagerFactory();

                final SSLContext context = SSLContext.getInstance("TLS");
                context.init(null, trustManagerFactory.getTrustManagers(), null);

                builder.sslSocketFactory(sslSocketFactory,
                        (X509TrustManager) trustManagerFactory.getTrustManagers()[0]);
            } catch (final KeyManagementException | NoSuchAlgorithmException e) {
                if (DEBUG) {
                    e.printStackTrace();
                    Log.e(TAG, "Unable to insert own {SSLSocket,TrustManager}Factory in OkHttp", e);
                }
            }
        }

        return builder;
    }
}
