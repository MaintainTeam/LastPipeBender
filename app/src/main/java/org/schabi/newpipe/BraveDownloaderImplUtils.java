package org.schabi.newpipe;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.schabi.newpipe.DownloaderImpl.USER_AGENT;

/**
 * Used for code that only exists in BraveNewPipe and is used
 * within the {@link DownloaderImpl}.
 */
public final class BraveDownloaderImplUtils {

    private BraveDownloaderImplUtils() {
    }

    // some servers eg rumble do not allow HEAD requests anymore (discovered 202300203)
    public static long getContentLengthViaGet(final String url) throws IOException {
        final HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setInstanceFollowRedirects(true);
        conn.setRequestProperty("User-Agent", USER_AGENT);
        conn.setRequestProperty("Accept", "*/*");
        conn.setRequestProperty("Accept-Encoding", "*");
        final String contentSize = conn.getHeaderField("Content-Length");
        conn.disconnect();
        return Long.parseLong(contentSize);
    }
}
