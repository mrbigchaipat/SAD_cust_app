package com.example.bigchaipat.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.bigchaipat.myapplication.DigitalSignatureHandler.KeyGenerator;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.cert.Certificate;

import cz.msebera.android.httpclient.Header;

public class LoginActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "MyPrefsFile";

    Button loginBtn;

    String email = "st119630@gmail.com";
    String password = "12345";
    String user_id;
    Double balance;
    int sequence_id =0;

    EditText suppliedEmail;
    EditText suppliedPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SharedPreferences settings = getSharedPreferences(LoginActivity.PREFS_NAME, 0);
        //Get "hasLoggedIn" value. If the value doesn't exist yet false is returned
        boolean hasLoggedIn = settings.getBoolean("hasLoggedIn", false);

        if(hasLoggedIn)
        {
            Double retrieveBalance = getDouble(settings,"balance", 0);
            String retrieveEmail = settings.getString("email","");
            String retrieveUserId = settings.getString("user_id", "");
            int retrieveSeqId = settings.getInt("sequence_id", 0);
            //Go directly to scan qr activity.
            Intent intent = new Intent(LoginActivity.this, ScanQRActivity.class);
            intent.putExtra("balance", retrieveBalance);
            intent.putExtra("sequence_id", retrieveSeqId);
            intent.putExtra("user_id", retrieveUserId);
            intent.putExtra("email", retrieveEmail);
            startActivity(intent);
            finish();
        }

        loginBtn = findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // verify the given credential

                // if success then check whether user has ever created key pair or not
                suppliedEmail = findViewById(R.id.email);
                suppliedPassword = findViewById(R.id.password);
                String suppliedEmailText = suppliedEmail.getText().toString();
                String suppliedPasswordText = suppliedPassword.getText().toString();

                if(suppliedEmailText.equals(email) && suppliedPasswordText.equals(password)) {
                    SharedPreferences settings = getSharedPreferences(LoginActivity.PREFS_NAME, 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean("hasLoggedIn", true);
                    editor.commit();
                }

                user_id = email.substring(0, email.indexOf("@"));
                boolean hasKey = false;
                try {
                    KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
                    ks.load(null);
                    hasKey = ks.containsAlias(email);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // if no key pairs then generate it
                if (!hasKey) {
                    System.out.println("Generate key pair ...");
                    KeyPair keyPair =  KeyGenerator.generateKey(email);
                    System.out.println("Successfully generating key pair ...");
                    RegisterPK(keyPair.getPublic().getEncoded());
                } else {
                    // redirect to scan qr activity
                    System.out.println("Key pair has been discovered for this user ...");
                    try {
                        KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
                        ks.load(null);
                        Certificate cert = ks.getCertificate(email);
                        System.out.println(cert.toString());
                        PublicKey publicKey = cert.getPublicKey();
                        System.out.println("Public key: " + publicKey.toString());
                        System.out.println("Public key encoded: " + Base64.encodeToString(publicKey.getEncoded(), 0));
                        byte[] publicKeyEncoded = publicKey.getEncoded();
                        RegisterPK(publicKeyEncoded);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void RegisterPK(byte[] publicKey){
        try {
            System.out.println("Register public key ...");
            RequestParams requestParams = new RequestParams();
            String pkEncoded = Base64.encodeToString(publicKey, 0);
            while(pkEncoded.indexOf('\n') != -1){
                pkEncoded = pkEncoded.substring(0,pkEncoded.indexOf('\n'))+pkEncoded.substring(pkEncoded.indexOf('\n')+1);
            }
            System.out.println(publicKey);
            requestParams.add("user_id", user_id);
            requestParams.add("public_key", pkEncoded);
            requestParams.add("Authorization", "YAHOsOO");
            System.out.println("public_key: " + pkEncoded );
            System.out.println("Authorization" + "YAHOOO");
            PaymentRestClient.postByUrl("http://203.159.32.54:8080/add_public_key", requestParams, new AsyncHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    System.out.println("Successfully registering public key ...");
                    String respBodyStr = new String(responseBody);
                    System.out.println(respBodyStr);
                    try {
                        JSONObject jsonObject = new JSONObject(respBodyStr);
                        balance = jsonObject.getDouble("balance");
                        sequence_id = jsonObject.getInt("sequence_id");


                        Intent intent = new Intent(LoginActivity.this, ScanQRActivity.class);
                        intent.putExtra("balance", balance);
                        intent.putExtra("sequence_id", sequence_id);
                        intent.putExtra("user_id", user_id);
                        intent.putExtra("email", email);
                        System.out.println("SEQUENCE_ID_CHECKPOINT_1: " + sequence_id);

                        SharedPreferences settings = getSharedPreferences(LoginActivity.PREFS_NAME, 0);
                        SharedPreferences.Editor editor = settings.edit();
                        putDouble(editor, "balance", balance);
                        editor.putInt("sequence_id", sequence_id);
                        editor.putString("user_id", user_id);
                        editor.putString("email", email);
                        editor.commit();


                        startActivity(intent);
                        finish();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    System.out.println("Failed");
                    String respBodyStr = new String(responseBody);
                    System.out.println(respBodyStr);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    SharedPreferences.Editor putDouble(final SharedPreferences.Editor edit, final String key, final double value) {
        return edit.putLong(key, Double.doubleToRawLongBits(value));
    }

    double getDouble(final SharedPreferences prefs, final String key, final double defaultValue) {
        return Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits(defaultValue)));
    }

}
