package com.example.bigchaipat.myapplication.DigitalSignatureHandler;

import java.security.PrivateKey;
import java.security.Signature;

public class SigningAlgorithm {

    public byte[] sign(PrivateKey pk, String data) {
        byte[] signature = null;
        try {
            Signature dsa = Signature.getInstance("SHA1withDSA", "SUN");
            dsa.initSign(pk);
            byte[] objAsBytes = data.getBytes("UTF-8");
            dsa.update(objAsBytes);
            signature = dsa.sign();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return signature;
    }
}
