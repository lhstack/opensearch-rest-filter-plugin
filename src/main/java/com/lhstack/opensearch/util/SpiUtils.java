package com.lhstack.opensearch.util;

import org.opensearch.common.io.Streams;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Description TODO
 * @Copyright: Copyright (c) 2022 ALL RIGHTS RESERVED.
 * @Author lhstack
 * @Date 2022/6/16 9:49
 * @Modify by
 */
public class SpiUtils {

    public static <T, R> Set<R> loadClassAsConvert(Class<T> clazz, ClassLoader classLoader, Function<Class<T>, R> apply) {
        return PrivilegedUtils.doPrivileged(() -> {
            try {
                Enumeration<URL> resources = classLoader.getResources(String.format("META-INF/%s", clazz.getCanonicalName()));
                return resolve(resources, clazz, apply);
            } catch (Exception e) {
                e.printStackTrace();
                return Collections.emptySet();
            }
        });
    }

    public static <T, R> Set<R> loadClassAsConvert(Class<T> clazz, Function<Class<T>, R> apply) {
        return PrivilegedUtils.doPrivileged(() -> {
            try {
                Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(String.format("META-INF/%s", clazz.getCanonicalName()));
                return resolve(resources, clazz, apply);
            } catch (Exception e) {
                e.printStackTrace();
                return Collections.emptySet();
            }
        });
    }

    private static <T, R> Set<R> resolve(Enumeration<URL> resources, Class<T> clazz, Function<Class<T>, R> apply) throws IOException {
        Iterator<URL> iterator = resources.asIterator();
        Set<String> classAllNames = new HashSet<>();
        while (iterator.hasNext()) {
            URL url = iterator.next();
            try (InputStream stream = url.openStream()) {
                List<String> classNames = Streams.readAllLines(stream);
                classAllNames.addAll(classNames);
            }
        }
        return classAllNames
                .stream()
                .map(clazzName -> {
                    try {
                        return Class.forName(clazzName);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .map(em -> apply.apply((Class<T>) em))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public static <T> Set<T> loadClassToInstance(Class<T> clazz, ClassLoader classLoader) {
        return loadClassAsConvert(clazz, classLoader, c -> {
            try {
                return clazz.cast(c.getConstructor().newInstance());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    public static <T> Set<T> loadClassToInstance(Class<T> clazz) {
        return loadClassAsConvert(clazz, c -> {
            try {
                return clazz.cast(c.getConstructor().newInstance());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    public static <T> Set<Class<T>> loadAllClass(Class<T> clazz, ClassLoader classLoader) {
        return loadClassAsConvert(clazz, classLoader, c -> c);
    }

}
