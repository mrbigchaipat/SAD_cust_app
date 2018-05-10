package com.example.bigchaipat.myapplication.DigitalSignatureHandler;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.security.KeyPair;
import java.security.KeyPairGenerator;

public class KeyGenerator {

    public static KeyPair generateKey(String email) {

        KeyPair kp = null;

        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore");
            kpg.initialize(new KeyGenParameterSpec.Builder(
                    email,
                    KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY)
                    .setDigests(KeyProperties.DIGEST_SHA256,
                            KeyProperties.DIGEST_SHA512)
                    .build());
            kp = kpg.generateKeyPair();


        } catch (Exception e) {
            e.printStackTrace();
        }

        return kp;
    }

}
