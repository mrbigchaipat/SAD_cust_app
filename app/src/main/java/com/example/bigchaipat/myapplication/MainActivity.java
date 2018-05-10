package com.example.bigchaipat.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.security.KeyStore;
import java.security.Signature;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity  {

    Button btnScan;
    Button btnTestSign;

    String email;
    String user_id;
    Double balance;
    int sequence_id;

    TextView tvUserId;
    TextView tvBalance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        email = getIntent().getStringExtra("email");
        user_id = getIntent().getStringExtra("user_id");
        balance = getIntent().getDoubleExtra("balance", 0);
        sequence_id = getIntent().getIntExtra("sequence_id", 0);

        tvUserId = findViewById(R.id.user_id);
        tvBalance = findViewById(R.id.balance);

        tvUserId.setText("Hello, "+user_id);
        tvBalance.setText("Your current balance is "+balance);

        btnScan = findViewById(R.id.gotoScan);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, ScanQRActivity.class);

                intent.putExtra("balance", balance);
                intent.putExtra("sequence_id", sequence_id);
                intent.putExtra("user_id", user_id);
                intent.putExtra("email", email);
                startActivity(intent);

            }
        });

        btnTestSign = findViewById(R.id.btnTestSign);
        btnTestSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("You clicked test sign button");
                testSign(email);
            }
        });
    }

    public void testSign(String email){
        try {
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            KeyStore.Entry entry = ks.getEntry(email, null);
            Signature s = Signature.getInstance("SHA256withECDSA");
            s.initSign(((KeyStore.PrivateKeyEntry) entry).getPrivateKey());
            String message = "simple message";
            s.update(message.getBytes());
            byte[] signature = s.sign();

            RequestParams requestParams = new RequestParams();
            System.out.println("Message: " + message);
            System.out.println("Signature: " + Base64.encodeToString(signature, 0));
            System.out.println("Public key: " + ((KeyStore.PrivateKeyEntry) entry).getCertificate().getPublicKey());
            System.out.println("Public key encoded: " + ((KeyStore.PrivateKeyEntry) entry).getCertificate().getPublicKey().getEncoded());
            requestParams.add("message", message);
            requestParams.add("signature", Base64.encodeToString(signature, 0));
            PaymentRestClient.postByUrl("http://203.159.32.43:8080/add_public_key", requestParams, new AsyncHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    System.out.println("Successfully registering public key ...");
                    String respBodyStr = new String(responseBody);
                    System.out.println(respBodyStr);
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
}
