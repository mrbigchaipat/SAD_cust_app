package com.example.bigchaipat.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.impl.TextCodec;

public class ScanQRActivity extends AppCompatActivity {

    Button scanQRBtn;
    String order;
    String orderJwt;
    String response_msg="";
    String send_msg="";

    String email;
    String user_id;
    Double balance;
    int sequence_id;

    TextView tvUserId;
    TextView tvBalance;
    TextView tvResponseMsg;
    TextView tvSendMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qr);

        email = getIntent().getStringExtra("email");
        user_id = getIntent().getStringExtra("user_id");
        balance = getIntent().getDoubleExtra("balance", 0);
        sequence_id = getIntent().getIntExtra("sequence_id", 0);
        if (getIntent().hasExtra("response_msg")) {
            tvResponseMsg = findViewById(R.id.response_msg);
            response_msg = getIntent().getStringExtra("response_msg");
            tvResponseMsg.setText("Response message: " + response_msg);
        }
        if (getIntent().hasExtra("send_msg")) {
            tvSendMsg.setText("Sent message: " + send_msg);
            send_msg = getIntent().getStringExtra("send_msg");
            tvSendMsg.setText("Sent message: " + send_msg);

        }

        tvUserId = findViewById(R.id.user_id);
        tvBalance = findViewById(R.id.balance);

        tvUserId.setText("Hello, "+user_id);
        tvBalance.setText("Your current balance is "+balance);

        scanQRBtn = findViewById(R.id.scanQR);

        scanQRBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    new IntentIntegrator(ScanQRActivity.this).initiateScan();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // Get the results:
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                //Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                Toast.makeText(this, "Scanned", Toast.LENGTH_SHORT).show();
                try {
                    Log.d("RESULT.GETCONTENT", result.getContents());
                    orderJwt = result.getContents();
                    order = Jwts.parser().setSigningKey(TextCodec.BASE64.encode("secret")).parseClaimsJws(orderJwt).getBody().getSubject();
                    Log.d("Order data: ", order);
                    //OK, we can trust this JWT

                } catch (SignatureException e) {
                    //don't trust the JWT!
                    e.printStackTrace();
                }
                Intent intent = new Intent(ScanQRActivity.this, ConfirmOrderActivity.class);

                intent.putExtra("balance", balance);
                intent.putExtra("sequence_id", sequence_id);
                intent.putExtra("user_id", user_id);
                intent.putExtra("email", email);
                intent.putExtra("order", order);
                intent.putExtra("orderJwt", orderJwt);
                System.out.println("SEQUENCE_ID_CHECKPOINT_2: " + sequence_id);
                startActivity(intent);
                finish();

            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }
}

