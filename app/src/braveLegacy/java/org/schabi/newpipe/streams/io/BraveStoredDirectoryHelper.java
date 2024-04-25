package org.schabi.newpipe.streams.io;

import android.os.Build;
import android.os.StatFs;
import android.system.ErrnoException;
import android.system.Os;
import android.system.StructStatVfs;

import java.io.FileDescriptor;

public final class BraveStoredDirectoryHelper {

    private BraveStoredDirectoryHelper() {

    }

    public static BraveStructStatVfs statvfs(final String path) throws ErrnoException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final StructStatVfs stat = Os.statvfs(path);
            return new BraveStructStatVfs(stat.f_bavail, stat.f_frsize);
        } else {
            final StatFs stat = new StatFs(path);
            return new BraveStructStatVfs(stat.getAvailableBlocksLong(),
                    stat.getBlockSizeLong()
            );
        }
    }

    public static BraveStructStatVfs fstatvfs(final FileDescriptor fd) throws ErrnoException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final StructStatVfs stat = Os.fstatvfs(fd);
            return new BraveStructStatVfs(stat.f_bavail, stat.f_frsize);
        } else {
            // TODO find a real solution for KitKat to determine the free fs on FileDescriptor
            // return just a fake value. '1' is just used to not exceed Long.MAX_VALUE as both
            // values are multiplied by the caller
            return new BraveStructStatVfs(Long.MAX_VALUE, 1);
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
        BraveStructStatVfs(final long f_bavail,
                           final long f_frsize) {
            this.f_bavail = f_bavail;
            this.f_frsize = f_frsize;
        }
    }
}
