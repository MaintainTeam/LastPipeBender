package org.schabi.newpipe;

import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Interceptor that changes the read timeout for the request.
 * <p>
 * The value is read from a header which will be removed afterwards.
 */
public class BraveTimeoutInterceptor implements Interceptor {

    /**
     * Custom timeout header (will be removed before sending).
     */
    private static final String CUSTOM_TIMEOUT = "custom-timeout";

    /**
     * Get Wrapper around DownloaderImpl.get() that sets custom timeout via header.
     * <p>
     * The timeout is done via a extra http header and read in an
     * {@link BraveTimeoutInterceptor} and
     *
     * @param url     tha url you want to call
     * @param timeout the timeout you want to have for this request
     * @return the response
     * @throws IOException
     * @throws ReCaptchaException
     */
    public static Response get(
            final String url,
            final int timeout)
            throws IOException, ReCaptchaException {
        final Map<String, List<String>> headers = new HashMap<>();
        headers.put(CUSTOM_TIMEOUT, Collections.singletonList(String.valueOf(timeout)));
        return DownloaderImpl.getInstance().get(url, headers);
    }

    public static Optional<Interceptor> getInterceptor(
            final OkHttpClient.Builder builder) {
        return builder.interceptors().stream().filter(
                BraveTimeoutInterceptor.class::isInstance).findFirst();
    }

    @Override
    public okhttp3.Response intercept(final Chain chain) throws IOException {

        final Request request = chain.request();
        final String timeoutForThisRequest = request.header(CUSTOM_TIMEOUT);

        if (timeoutForThisRequest != null) {
            final int timeout = Integer.parseInt(timeoutForThisRequest);

            final Chain newChain = chain.withReadTimeout(timeout, TimeUnit.SECONDS)
                    .withConnectTimeout(timeout, TimeUnit.SECONDS)
                    .withWriteTimeout(timeout, TimeUnit.SECONDS);

            final Request requestWithTimeoutHeaderRemoved = newChain.request().newBuilder()
                    .removeHeader(CUSTOM_TIMEOUT)
                    .build();

            return newChain.proceed(requestWithTimeoutHeaderRemoved);
        }

        return chain.proceed(request);
    }
}
