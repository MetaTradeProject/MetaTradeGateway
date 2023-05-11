package com.freesia.metatradegateway;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.LinkedList;

import org.bouncycastle.asn1.x9.X9IntegerConverter;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.math.ec.ECAlgorithms;
import org.bouncycastle.math.ec.ECPoint;

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

    public static String sign(String msg, String privateKey){
        byte[] signData = signTransaction(hex2Byte(msg), hex2Byte(privateKey));
        String str = byte2Hex(signData);
        return str;
    }

    private static byte[] derSign(byte[] rb, byte[] sb) throws Exception {
        int off = (2 + 2) + rb.length;
        int tot = off + (2 - 2) + sb.length;
        byte[] der = new byte[tot + 2];
        der[0] = 0x30;
        der[1] = (byte) (tot & 0xff);
        der[2 + 0] = 0x02;
        der[2 + 1] = (byte) (rb.length & 0xff);
        System.arraycopy(rb, 0, der, 2 + 2, rb.length);
        der[off + 0] = 0x02;
        der[off + 1] = (byte) (sb.length & 0xff);
        System.arraycopy(sb, 0, der, off + 2, sb.length);
        return der;
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
            
            return derSign(r, s);   
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
