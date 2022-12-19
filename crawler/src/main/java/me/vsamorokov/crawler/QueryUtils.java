package me.vsamorokov.crawler;

import javax.persistence.PersistenceException;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class QueryUtils {

    public static <T> T getOrCreate(Supplier<T> finder, Supplier<T> factory, UnaryOperator<T> saver) {
        T t;
        try {
            t = finder.get();
            if (t == null) {
                T newInstance = factory.get();
                return saver.apply(newInstance);
            }
            return t;
        } catch (PersistenceException e) {
            t = finder.get();
            if (t == null) {
                throw e;
            }
            return t;
        }
    }
}
