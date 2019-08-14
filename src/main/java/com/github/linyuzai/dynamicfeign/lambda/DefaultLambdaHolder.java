package com.github.linyuzai.dynamicfeign.lambda;

import com.github.linyuzai.dynamicfeign.mapper.DynamicFeignClientMapper;

public class DefaultLambdaHolder {
    public static boolean addMethodUrl(LambdaNoneHolder lambda, String url) {
        SerializedLambda serializedLambda = SerializedLambda.resolve(lambda);
        return DynamicFeignClientMapper.addMethodUrl(serializedLambda.getImplClass(), serializedLambda.getImplMethodName(), url);
    }

    public static boolean addMethodUrl(LambdaNoneVoidHolder lambda, String url) {
        SerializedLambda serializedLambda = SerializedLambda.resolve(lambda);
        return DynamicFeignClientMapper.addMethodUrl(serializedLambda.getImplClass(), serializedLambda.getImplMethodName(), url);
    }

    public static boolean addMethodUrl(LambdaMapHolder lambda, String url) {
        SerializedLambda serializedLambda = SerializedLambda.resolve(lambda);
        return DynamicFeignClientMapper.addMethodUrl(serializedLambda.getImplClass(), serializedLambda.getImplMethodName(), url);
    }

    public static boolean addMethodUrl(LambdaMapVoidHolder lambda, String url) {
        SerializedLambda serializedLambda = SerializedLambda.resolve(lambda);
        return DynamicFeignClientMapper.addMethodUrl(serializedLambda.getImplClass(), serializedLambda.getImplMethodName(), url);
    }

    public static boolean addMethodUrl(LambdaStringHolder lambda, String url) {
        SerializedLambda serializedLambda = SerializedLambda.resolve(lambda);
        return DynamicFeignClientMapper.addMethodUrl(serializedLambda.getImplClass(), serializedLambda.getImplMethodName(), url);
    }

    public static boolean addMethodUrl(LambdaStringVoidHolder lambda, String url) {
        SerializedLambda serializedLambda = SerializedLambda.resolve(lambda);
        return DynamicFeignClientMapper.addMethodUrl(serializedLambda.getImplClass(), serializedLambda.getImplMethodName(), url);
    }

    public static boolean addMethodUrl(LambdaStringArrayHolder lambda, String url) {
        SerializedLambda serializedLambda = SerializedLambda.resolve(lambda);
        return DynamicFeignClientMapper.addMethodUrl(serializedLambda.getImplClass(), serializedLambda.getImplMethodName(), url);
    }

    public static boolean addMethodUrl(LambdaStringArrayVoidHolder lambda, String url) {
        SerializedLambda serializedLambda = SerializedLambda.resolve(lambda);
        return DynamicFeignClientMapper.addMethodUrl(serializedLambda.getImplClass(), serializedLambda.getImplMethodName(), url);
    }

    public static <T> boolean removeMethodUrl(LambdaNoneHolder<T, ?> lambda) {
        SerializedLambda serializedLambda = SerializedLambda.resolve(lambda);
        return DynamicFeignClientMapper.removeMethodUrl(serializedLambda.getImplClass(), serializedLambda.getImplMethodName());
    }

    public static <T> boolean removeMethodUrl(LambdaNoneVoidHolder<T> lambda) {
        SerializedLambda serializedLambda = SerializedLambda.resolve(lambda);
        return DynamicFeignClientMapper.removeMethodUrl(serializedLambda.getImplClass(), serializedLambda.getImplMethodName());
    }

    public static <T> boolean removeMethodUrl(LambdaMapHolder<T, ?> lambda) {
        SerializedLambda serializedLambda = SerializedLambda.resolve(lambda);
        return DynamicFeignClientMapper.removeMethodUrl(serializedLambda.getImplClass(), serializedLambda.getImplMethodName());
    }

    public static <T> boolean removeMethodUrl(LambdaMapVoidHolder<T> lambda) {
        SerializedLambda serializedLambda = SerializedLambda.resolve(lambda);
        return DynamicFeignClientMapper.removeMethodUrl(serializedLambda.getImplClass(), serializedLambda.getImplMethodName());
    }

    public static <T> boolean removeMethodUrl(LambdaStringHolder<T, ?> lambda) {
        SerializedLambda serializedLambda = SerializedLambda.resolve(lambda);
        return DynamicFeignClientMapper.removeMethodUrl(serializedLambda.getImplClass(), serializedLambda.getImplMethodName());
    }

    public static <T> boolean removeMethodUrl(LambdaStringVoidHolder<T> lambda) {
        SerializedLambda serializedLambda = SerializedLambda.resolve(lambda);
        return DynamicFeignClientMapper.removeMethodUrl(serializedLambda.getImplClass(), serializedLambda.getImplMethodName());
    }

    public static <T> boolean removeMethodUrl(LambdaStringArrayHolder<T, ?> lambda) {
        SerializedLambda serializedLambda = SerializedLambda.resolve(lambda);
        return DynamicFeignClientMapper.removeMethodUrl(serializedLambda.getImplClass(), serializedLambda.getImplMethodName());
    }

    public static <T> boolean removeMethodUrl(LambdaStringArrayVoidHolder<T> lambda) {
        SerializedLambda serializedLambda = SerializedLambda.resolve(lambda);
        return DynamicFeignClientMapper.removeMethodUrl(serializedLambda.getImplClass(), serializedLambda.getImplMethodName());
    }
}
