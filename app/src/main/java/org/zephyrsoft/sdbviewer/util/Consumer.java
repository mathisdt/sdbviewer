package org.zephyrsoft.sdbviewer.util;

/**
 * Poor man's replacement for Comsumer from API 24 / Java 8.
 */
public interface Consumer<T> {

        /**
         * Performs this operation on the given argument.
         *
         * @param t the input argument
         */
        void accept(T t);

}
