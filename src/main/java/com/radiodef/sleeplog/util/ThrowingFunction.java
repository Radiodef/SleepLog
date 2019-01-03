package com.radiodef.sleeplog.util;

public interface ThrowingFunction<T, U, E extends Throwable> {
    U apply(T t) throws E;
}