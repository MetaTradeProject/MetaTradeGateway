package com.freesia.metatradegateway;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CryptoUtilsTests {
    @Test
    public void TestSha256(){
        var ret = CryptoUtils.getSHA256("123");
        Assertions.assertEquals(ret, "a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3");
    }
}
