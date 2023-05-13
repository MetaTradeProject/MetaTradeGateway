package com.freesia.metatradegateway.jni;

import org.junit.jupiter.api.Test;


public class JniSignerTests{
    @Test
    public void TestSignTrade() {
        JniSigner signer = new JniSigner();
        String signature = signer.SignTrade("03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4", "8F72F6B29E6E225A36B68DFE333C7CE5E55D83249D3D2CD6332671FA445C4DD3");
        
    }
}