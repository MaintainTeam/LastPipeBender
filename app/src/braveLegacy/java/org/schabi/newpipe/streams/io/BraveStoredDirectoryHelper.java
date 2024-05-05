package org.schabi.newpipe.streams.io;

import android.os.Build;
import android.system.ErrnoException;
import android.system.Os;

import java.io.FileDescriptor;

import androidx.annotation.RequiresApi;

public final class BraveStoredDirectoryHelper {

    private BraveStoredDirectoryHelper() {

    }

    public static BraveStructStatVfs statvfs(final String path) throws ErrnoException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new BraveStructStatVfs(Os.statvfs(path));
        } else {
            return new BraveStructStatVfs(com.github.evermindzz.osext.system.Os.statvfs(path));
        }
    }

    public static BraveStructStatVfs fstatvfs(final FileDescriptor fd) throws ErrnoException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new BraveStructStatVfs(Os.fstatvfs(fd));
        } else {
            return new BraveStructStatVfs(com.github.evermindzz.osext.system.Os.fstatvfs(fd));
        }
    }

    public static class BraveStructStatVfs {
        /**
         * @noinspection checkstyle:MemberName
         */
        public final long f_bavail;
        /**
         * @noinspection checkstyle:MemberName
         */
        public final long f_frsize;

        /**
         * @noinspection checkstyle:ParameterName
         */
        BraveStructStatVfs(final com.github.evermindzz.osext.system.StructStatVfs stat) {
            this.f_bavail = stat.f_bavail;
            this.f_frsize = stat.f_frsize;
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        BraveStructStatVfs(final android.system.StructStatVfs stat) {
            this.f_bavail = stat.f_bavail;
            this.f_frsize = stat.f_frsize;
        }
    }
}
