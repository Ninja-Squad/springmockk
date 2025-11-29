package com.ninjasquad.springmockk;

import org.jspecify.annotations.Nullable;
import org.springframework.core.ResolvableType;
import org.springframework.util.Assert;

/**
 * Workaround for a nullability issue with ResolvableType
 * @author JB Nizet
 */
public class FixedResolvableType {
    public static ResolvableType forClassWithGenerics(Class<?> clazz, @Nullable Class<?>... generics) {
        return  ResolvableType.forClassWithGenerics(clazz, generics);
    }
}
