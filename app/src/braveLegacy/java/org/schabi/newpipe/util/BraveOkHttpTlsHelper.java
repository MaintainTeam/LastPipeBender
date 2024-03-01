package org.schabi.newpipe.util;

import android.content.Context;
import android.os.Build;

import org.schabi.newpipe.App;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

import static org.schabi.newpipe.MainActivity.DEBUG;

public final class BraveOkHttpTlsHelper {

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

                final KeyStore customCAsKeystore = createKeystoreWithCustomCAsAndSystemCAs();
                final TrustManagerFactory trustManagerFactory =
                        getTrustManagerFactory(customCAsKeystore);
                final SSLContext context = SSLContext.getInstance("TLS");
                context.init(null, trustManagerFactory.getTrustManagers(), null);

                final SSLSocketFactory sslSocketFactory =
                        new BraveTLSSocketFactory(trustManagerFactory);
                builder.sslSocketFactory(sslSocketFactory,
                        (X509TrustManager) trustManagerFactory.getTrustManagers()[0]);
            } catch (final KeyManagementException | NoSuchAlgorithmException | KeyStoreException
                           | IOException | CertificateException e) {
                if (DEBUG) {
                    e.printStackTrace();
                }
            }
        }

        return builder;
    }

    public static TrustManagerFactory getTrustManagerFactory(
            final KeyStore keyStore)
            throws NoSuchAlgorithmException, KeyStoreException {

        final TrustManagerFactory trustManagerFactory = TrustManagerFactory
                .getInstance(TrustManagerFactory.getDefaultAlgorithm());

        // Tell TrustManager to trust the CAs in our KeyStore
        trustManagerFactory.init(keyStore);

        // only allow one TrustManager
        final TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
            throw new IllegalStateException("Unexpected default trust managers:"
                    + Arrays.toString(trustManagers));
        }

        return trustManagerFactory;
    }


    /**
     * Add our trusted CAs for rumble.com and framatube.org to keystore.
     *
     * @return custom CA keystore with our added CAs
     * @throws KeyStoreException
     * @throws CertificateException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    private static KeyStore createKeystoreWithCustomCAsAndSystemCAs()
            throws KeyStoreException, CertificateException,
            IOException, NoSuchAlgorithmException {

        final List<String> rawCertFiles = Arrays.asList("ca_digicert_global_g2", "ca_lets_encrypt");
        final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);
        for (final String rawCertFile : rawCertFiles) {
            final Certificate cert = readCertificateFromFile(rawCertFile);
            keyStore.setCertificateEntry(rawCertFile, cert);
        }

        addSystemCAsToKeystore(keyStore);

        return keyStore;
    }

    private static void addSystemCAsToKeystore(
            final KeyStore keyStore) throws NoSuchAlgorithmException, KeyStoreException {

        // Default TrustManager to get device trusted CA's
        final TrustManagerFactory defaultTrustManagerFactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        defaultTrustManagerFactory.init((KeyStore) null);

        final X509TrustManager trustManager =
                (X509TrustManager) defaultTrustManagerFactory.getTrustManagers()[0];
        int idx = 0;
        for (final Certificate cert : trustManager.getAcceptedIssuers()) {
            keyStore.setCertificateEntry(Integer.toString(idx), cert);
            idx++;
        }
    }

    private static Certificate readCertificateFromFile(
            final String rawFile)
            throws IOException, CertificateException {

        final Context context = App.getApp().getApplicationContext();
        final InputStream inputStream = context.getResources().openRawResource(
                context.getResources().getIdentifier(rawFile,
                        "raw", context.getPackageName()));

        final byte[] rawBytes = new byte[inputStream.available()];
        inputStream.read(rawBytes);
        inputStream.close();

        final CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return cf.generateCertificate(new ByteArrayInputStream(rawBytes));
    }
}
