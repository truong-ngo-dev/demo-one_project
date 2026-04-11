package vn.truongngo.apartcom.one.lib.abac.utils;

import java.util.Collection;
import java.util.Objects;

/**
 * Collection utility
 * @author Truong Ngo
 */
public class CollectionUtils {

    public static boolean isNullOrEmpty(Collection<?> c) {
        return Objects.isNull(c) || c.isEmpty();
    }
}
