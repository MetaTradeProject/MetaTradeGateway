package com.freesia.metatradegateway.jni;

public class JniSigner{
    static {
        String path = JniSigner.class.getClassLoader().getResource("//").getPath();
        System.load(path + "libsigner.so");
    }
    public native String SignTrade(String hash, String privateKey);
}