package com.freesia.metatradegateway;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CryptoUtilsTests {
    @Test
    public void TestSha256(){
        var ret = CryptoUtils.getSHA256("123");
        Assertions.assertEquals(ret, "a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3");
    }

    @Test
    public void TestSign(){
        var ret = CryptoUtils.sign("03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4", "8F72F6B29E6E225A36B68DFE333C7CE5E55D83249D3D2CD6332671FA445C4DD3");
        Assertions.assertEquals(ret, "b0745147017bd3458e6c08b8b2d194f4e16c96d59f42229394095e74e291b0165da374c7e8e11565bd428541a92c012c973917adf4d013fd15974d4bccd7a3e9");
    }
}
