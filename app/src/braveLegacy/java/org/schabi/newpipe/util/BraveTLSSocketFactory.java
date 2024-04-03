package org.schabi.newpipe.util;

import android.util.Log;

import org.schabi.newpipe.BraveTag;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;


/**
 * This is an extension of the SSLSocketFactory which enables TLS 1.2 and 1.3.
 * Created for usage on Android 4.1-4.4 devices, which haven't enabled those by default.
 */
public final class BraveTLSSocketFactory extends SSLSocketFactory {

    private static final String TAG =
            new BraveTag().tagShort23(BraveTLSSocketFactory.class.getSimpleName());

    private static BraveTLSSocketFactory instance = null;

    private final SSLSocketFactory internalSSLSocketFactory;
    private final BraveTrustManagerFactoryHelper trustManagerFactoryHelper;

    private BraveTLSSocketFactory()
            throws NoSuchAlgorithmException, KeyManagementException {
        trustManagerFactoryHelper = new BraveTrustManagerFactoryHelper();
        final SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, trustManagerFactoryHelper.getTrustManagerFactory()
                .getTrustManagers(), null);
        internalSSLSocketFactory = context.getSocketFactory();
    }

    public static BraveTLSSocketFactory getInstance()
            throws NoSuchAlgorithmException, KeyManagementException {
        if (instance != null) {
            return instance;
        }
        instance = new BraveTLSSocketFactory();
        return instance;
    }

    public static void setAsDefault() {
        try {
            HttpsURLConnection.setDefaultSSLSocketFactory(getInstance());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            Log.e(TAG, "Unable to setAsDefault", e);
        }
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return internalSSLSocketFactory.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return internalSSLSocketFactory.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket() throws IOException {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket());
    }

    @Override
    public Socket createSocket(final Socket s, final String host, final int port,
                               final boolean autoClose) throws IOException {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(s, host, port, autoClose));
    }

    @Override
    public Socket createSocket(final String host, final int port) throws IOException {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(host, port));
    }

    @Override
    public Socket createSocket(final String host, final int port, final InetAddress localHost,
                               final int localPort) throws IOException {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(
                host, port, localHost, localPort));
    }

    @Override
    public Socket createSocket(final InetAddress host, final int port) throws IOException {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(host, port));
    }

    @Override
    public Socket createSocket(final InetAddress address, final int port,
                               final InetAddress localAddress, final int localPort)
            throws IOException {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(
                address, port, localAddress, localPort));
    }

    private Socket enableTLSOnSocket(final Socket socket) {
        if (socket instanceof SSLSocket) {
            ((SSLSocket) socket).setEnabledProtocols(new String[]{"TLSv1.2", "TLSv1.3"});
        }
        return socket;
    }

    public TrustManagerFactory getTrustManagerFactory() {
        return trustManagerFactoryHelper.getTrustManagerFactory();
    }
}
