package org.schabi.newpipe.util;

import static org.schabi.newpipe.extractor.utils.Utils.isBlank;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;

import com.squareup.picasso.Cache;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;
import com.squareup.picasso.Transformation;

import org.schabi.newpipe.R;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import okhttp3.OkHttpClient;

public final class PicassoHelper {
    public static final String PLAYER_THUMBNAIL_TAG = "PICASSO_PLAYER_THUMBNAIL_TAG";
    private static final String PLAYER_THUMBNAIL_TRANSFORMATION_KEY
            = "PICASSO_PLAYER_THUMBNAIL_TRANSFORMATION_KEY";

    private PicassoHelper() {
    }

    private static Cache picassoCache;
    private static OkHttpClient picassoDownloaderClient;

    // suppress because terminate() is called in App.onTerminate(), preventing leaks
    @SuppressLint("StaticFieldLeak")
    private static Picasso picassoInstance;

    private static boolean shouldLoadImages;

    public static void init(final Context context) {
        picassoCache = new LruCache(10 * 1024 * 1024);
        picassoDownloaderClient = new OkHttpClient.Builder()
                .cache(new okhttp3.Cache(new File(context.getExternalCacheDir(), "picasso"),
                        50 * 1024 * 1024))
                // this should already be the default timeout in OkHttp3, but just to be sure...
                .callTimeout(15, TimeUnit.SECONDS)
                .build();

        picassoInstance = new Picasso.Builder(context)
                .memoryCache(picassoCache) // memory cache
                .downloader(new OkHttp3Downloader(picassoDownloaderClient)) // disk cache
                .defaultBitmapConfig(Bitmap.Config.RGB_565)
                .build();
    }

    public static void terminate() {
        picassoCache = null;
        picassoDownloaderClient = null;

        if (picassoInstance != null) {
            picassoInstance.shutdown();
            picassoInstance = null;
        }
    }

    public static void clearCache(final Context context) throws IOException {
        picassoInstance.shutdown();
        picassoCache.clear(); // clear memory cache
        final okhttp3.Cache diskCache = picassoDownloaderClient.cache();
        if (diskCache != null) {
            diskCache.delete(); // clear disk cache
        }
        init(context);
    }

    public static void cancelTag(final Object tag) {
        picassoInstance.cancelTag(tag);
    }

    public static void setIndicatorsEnabled(final boolean enabled) {
        picassoInstance.setIndicatorsEnabled(enabled); // useful for debugging
    }

    public static void setShouldLoadImages(final boolean shouldLoadImages) {
        PicassoHelper.shouldLoadImages = shouldLoadImages;
    }

    public static boolean getShouldLoadImages() {
        return shouldLoadImages;
    }


    public static RequestCreator loadAvatar(final String url) {
        return loadImageDefault(url, R.drawable.buddy);
    }

    public static RequestCreator loadThumbnail(final String url) {
        return loadImageDefault(url, R.drawable.dummy_thumbnail);
    }

    public static RequestCreator loadBanner(final String url) {
        return loadImageDefault(url, R.drawable.channel_banner);
    }

    public static RequestCreator loadPlaylistThumbnail(final String url) {
        return loadImageDefault(url, R.drawable.dummy_thumbnail_playlist);
    }

    public static RequestCreator loadSeekbarThumbnailPreview(final String url) {
        return picassoInstance.load(url);
    }


    public static RequestCreator loadScaledDownThumbnail(final Context context, final String url) {
        // scale down the notification thumbnail for performance
        return PicassoHelper.loadThumbnail(url)
                .tag(PLAYER_THUMBNAIL_TAG)
                .transform(new Transformation() {
                    @Override
                    public Bitmap transform(final Bitmap source) {
                        final float notificationThumbnailWidth = Math.min(
                                context.getResources()
                                        .getDimension(R.dimen.player_notification_thumbnail_width),
                                source.getWidth());

                        final Bitmap result = Bitmap.createScaledBitmap(
                                source,
                                (int) notificationThumbnailWidth,
                                (int) (source.getHeight()
                                        / (source.getWidth() / notificationThumbnailWidth)),
                                true);

                        if (result == source) {
                            // create a new mutable bitmap to prevent strange crashes on some
                            // devices (see #4638)
                            final Bitmap copied = Bitmap.createScaledBitmap(
                                    source,
                                    (int) notificationThumbnailWidth - 1,
                                    (int) (source.getHeight() / (source.getWidth()
                                            / (notificationThumbnailWidth - 1))),
                                    true);
                            source.recycle();
                            return copied;
                        } else {
                            source.recycle();
                            return result;
                        }
                    }

                    @Override
                    public String key() {
                        return PLAYER_THUMBNAIL_TRANSFORMATION_KEY;
                    }
                });
    }

    @Nullable
    public static Bitmap getImageFromCacheIfPresent(final String imageUrl) {
        // URLs in the internal cache finish with \n so we need to add \n to image URLs
        return picassoCache.get(imageUrl + "\n");
    }

    public static void loadNotificationIcon(final String url,
                                            final Consumer<Bitmap> bitmapConsumer) {
        loadImageDefault(url, R.drawable.ic_newpipe_triangle_white)
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(final Bitmap bitmap, final Picasso.LoadedFrom from) {
                        bitmapConsumer.accept(bitmap);
                    }

                    @Override
                    public void onBitmapFailed(final Exception e, final Drawable errorDrawable) {
                        bitmapConsumer.accept(null);
                    }

                    @Override
                    public void onPrepareLoad(final Drawable placeHolderDrawable) {
                        // Nothing to do
                    }
                });
    }


    private static RequestCreator loadImageDefault(final String url, final int placeholderResId) {
        if (!shouldLoadImages || isBlank(url)) {
            return picassoInstance
                    .load((String) null)
                    .placeholder(placeholderResId) // show placeholder when no image should load
                    .error(placeholderResId);
        } else {
            return picassoInstance
                    .load(url)
                    .error(placeholderResId); // don't show placeholder while loading, only on error
        }
    }
}
