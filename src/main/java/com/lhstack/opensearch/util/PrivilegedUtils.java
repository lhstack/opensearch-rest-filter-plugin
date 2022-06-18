package com.lhstack.opensearch.util;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.function.Supplier;

/**
 * @Description TODO
 * @Author lhstack
 * @Date 2022/6/15 7:06
 * @Modify By
 */
public class PrivilegedUtils {

    public static <T> T doPrivileged(Supplier<T> supplier) {
        return AccessController.doPrivileged((PrivilegedAction<T>) supplier::get);
    }
}
