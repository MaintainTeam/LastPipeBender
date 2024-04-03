package org.schabi.newpipe.util;

import android.content.Context;
import android.util.Log;

import org.schabi.newpipe.BraveApp;
import org.schabi.newpipe.BraveTag;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import static org.schabi.newpipe.MainActivity.DEBUG;

/**
 * This helper class basically init the TrustManagerFactory with our custom CA's.
 * <p>
 * The CA's are for rumble.com and framatube.org
 */
public class BraveTrustManagerFactoryHelper {
    private static final String TAG =
            new BraveTag().tagShort23(BraveTrustManagerFactoryHelper.class.getSimpleName());
    TrustManagerFactory trustManagerFactory;

    public BraveTrustManagerFactoryHelper() {

        try {
            final KeyStore customCAsKeystore = createKeystoreWithCustomCAsAndSystemCAs();
            trustManagerFactory =
                    addOurKeystoreToTrustManagerFactory(customCAsKeystore);
        } catch (final NoSuchAlgorithmException | KeyStoreException
                       | IOException | CertificateException e) {
            if (DEBUG) {
                e.printStackTrace();
                Log.e(TAG, "Unable to create TrustManagerFactory with own CA's", e);
            }
        }
    }

    public TrustManagerFactory getTrustManagerFactory() {
        return trustManagerFactory;
    }

    private TrustManagerFactory addOurKeystoreToTrustManagerFactory(
            final KeyStore keyStore)
            throws NoSuchAlgorithmException, KeyStoreException {

        final TrustManagerFactory managerFactory = TrustManagerFactory
                .getInstance(TrustManagerFactory.getDefaultAlgorithm());

        // Tell TrustManager to trust the CAs in our KeyStore
        managerFactory.init(keyStore);

        // only allow one TrustManager
        final TrustManager[] trustManagers = managerFactory.getTrustManagers();
        if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
            throw new IllegalStateException("Unexpected default trust managers:"
                    + Arrays.toString(trustManagers));
        }

        return managerFactory;
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
    private KeyStore createKeystoreWithCustomCAsAndSystemCAs()
            throws KeyStoreException, CertificateException,
            IOException, NoSuchAlgorithmException {

        final List<String> rawCertFiles = Arrays.asList("ca_digicert_global_g2",
                "ca_lets_encrypt_root" /*, "ca_lets_encrypt"*/);
        final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);
        for (final String rawCertFile : rawCertFiles) {
            final Certificate cert = readCertificateFromFile(rawCertFile);
            keyStore.setCertificateEntry(rawCertFile, cert);
        }

        addSystemCAsToKeystore(keyStore);

        return keyStore;
    }

    private void addSystemCAsToKeystore(
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

    private Certificate readCertificateFromFile(
            final String rawFile)
            throws IOException, CertificateException {

        final Context context = BraveApp.getAppContext();
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
