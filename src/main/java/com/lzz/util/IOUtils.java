package com.lzz.util;

/**
 * Created by gl49 on 2018/1/10.
 */

import java.io.Closeable;
import java.util.Collection;

public class IOUtils {
    public IOUtils() {
    }

    public static void closeQuietly(Closeable closeable) {
        try {
            if(closeable != null) {
                closeable.close();
            }
        } catch (Throwable ignored) {

        }

    }

    public static void closeQuietly(Closeable... closeables) {
        if(closeables != null) {
            for (Closeable closeable : closeables) {
                closeQuietly(closeable);
            }
        }

    }

    public static void closeQuietly(Collection<Closeable> closeables) {
        if(closeables != null) {
            for (Closeable closeable : closeables) {
                closeQuietly(closeable);
            }
        }

    }
}
