package com.freesia.metatradegateway;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.Signature;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.EllipticCurve;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.math.ec.ECCurve;

public class CryptoUtils {
    public static String getSHA256(String str) {
        MessageDigest messageDigest;
        String encodeStr = "";
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(str.getBytes(StandardCharsets.UTF_8));
            encodeStr = byte2Hex(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return encodeStr;
    }

    public static byte[] hex2Byte(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    private static String byte2Hex(byte[] bytes) {
        StringBuilder stringBuffer = new StringBuilder();
        String temp;
        for (byte aByte : bytes) {
            temp = Integer.toHexString(aByte & 0xFF);
            if (temp.length() == 1) {
                // 1得到一位的进行补0操作
                stringBuffer.append("0");
            }
            stringBuffer.append(temp);
        }
        return stringBuffer.toString();
    }

    public static PrivateKey loadPrivateKey(byte[] data) throws GeneralSecurityException {
        KeyFactory factory = KeyFactory.getInstance("ECDSA", "SC");
        ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec("secp256k1");
        ECCurve eccCurve = spec.getCurve();
        EllipticCurve ellipticCurve = EC5Util.convertCurve(eccCurve, spec.getSeed());
        java.security.spec.ECParameterSpec params = EC5Util.convertSpec(ellipticCurve, spec);
        ECPrivateKeySpec keySpec = new ECPrivateKeySpec(new BigInteger(1, data), params);
        return factory.generatePrivate(keySpec);
    }

    public static String sign(String msg, String privateKey){
        try {
            Signature signature;
            signature = Signature.getInstance("SHA256withECDSA");
            signature.initSign(loadPrivateKey(hex2Byte(privateKey)));
            signature.update(hex2Byte(msg));
            byte[] s = signature.sign();

            int i = 0;
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        

        byte[] signData = signTransaction(hex2Byte(msg), hex2Byte(privateKey));
        String str = byte2Hex(signData);
        return str;
    }

    private static byte[] signTransaction(byte[] data, byte[] privateKey) {
        try {
            Security.addProvider(new BouncyCastleProvider());
            ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec("secp256k1");

            ECDSASigner ecdsaSigner = new ECDSASigner();
            ECDomainParameters domain = new ECDomainParameters(spec.getCurve(), spec.getG(), spec.getN());
            ECPrivateKeyParameters privateKeyParms =
                new ECPrivateKeyParameters(new BigInteger(1, privateKey), domain);
            ParametersWithRandom params = new ParametersWithRandom(privateKeyParms);

            ecdsaSigner.init(true, params);

            BigInteger[] sig = ecdsaSigner.generateSignature(data);
            byte[] r = sig[0].toByteArray();
            if (r[0] == 0) {
                byte[] tmp = new byte[r.length - 1];
                System.arraycopy(r, 1, tmp, 0, tmp.length);
                r = tmp;
            }
            byte[] s = sig[0].toByteArray();
            if (s[0] == 0) {
                byte[] tmp = new byte[s.length - 1];
                System.arraycopy(s, 1, tmp, 0, tmp.length);
                s = tmp;
            }
            
            return r;  
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
