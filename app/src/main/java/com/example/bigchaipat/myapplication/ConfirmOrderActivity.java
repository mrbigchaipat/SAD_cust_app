package com.example.bigchaipat.myapplication;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.KeyStore;
import java.security.Signature;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class ConfirmOrderActivity extends AppCompatActivity implements MyRecyclerViewAdapter.ItemClickListener {

    public static final String PREFS_NAME = "MyPrefsFile";

    String email;
    String user_id;
    String orderJwt;
    Double balance;
    int sequence_id;

    MyRecyclerViewAdapter adapter;
    LinearLayoutManager layoutManager;
    Button confirmBtn;

    JSONObject orderJsonObject;

    List<Menu> menus;
    Order order;

    TextView tvOrderId;
    TextView tvTotalAmount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_order);

        email = getIntent().getStringExtra("email");
        user_id = getIntent().getStringExtra("user_id");
        balance = getIntent().getDoubleExtra("balance", 0);
        sequence_id = getIntent().getIntExtra("sequence_id", 0);
        orderJwt = getIntent().getStringExtra("orderJwt");

        // data to populate the RecyclerView with
        try {
            String orders = getIntent().getExtras().getString("order");
            menus = new ArrayList<>();
            orderJsonObject = new JSONObject(orders);

            int orderId = orderJsonObject.getInt("id");
            double totalAmount = orderJsonObject.getDouble("total_price");

            tvOrderId = findViewById(R.id.tvOrderId);
            tvTotalAmount = findViewById(R.id.tvTotalAmount);

            tvOrderId.setText("Order ID: " + orderId);
            tvTotalAmount.setText("Total price: " + totalAmount + " Baht");

            for (int i = 0; i < orderJsonObject.getJSONArray("products").length(); i++) {
                //Log.d("product " + i + " name"  , orderJsonObject.getJSONArray("products").getJSONObject(i).getString("n"));
                //Log.d("product " + i + " price", orderJsonObject.getJSONArray("products").getJSONObject(i).getString("p"));
                Menu menu = new Menu(orderJsonObject.getJSONArray("products").getJSONObject(i).getString("n"),
                        orderJsonObject.getJSONArray("products").getJSONObject(i).getDouble("p"));
                menus.add(menu);
            }
            order = new Order(orderId, totalAmount, menus);
            order.calculateTotalAmount();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        // set up the RecyclerView
        RecyclerView recyclerView = findViewById(R.id.rvOrders);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new MyRecyclerViewAdapter(this, order);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        confirmBtn = findViewById(R.id.confirmBtn);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(ConfirmOrderActivity.this);
                builder.setMessage(R.string.confirm_order_message)
                        .setTitle(R.string.confirm_order_title);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        try {

                            // Do purchase
                            Purchase();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    @Override
    public void onItemClick(View view, int position) {
        Toast.makeText(this, "You Clicked " + adapter.getItem(position).getName() + " " + adapter.getItem(position).getPrice() + " on row number " + position, Toast.LENGTH_SHORT).show();
    }

    public void Purchase() throws JSONException {
        try {
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            KeyStore.Entry entry = ks.getEntry(email, null);
            Signature s = Signature.getInstance("SHA256withECDSA");
            s.initSign(((KeyStore.PrivateKeyEntry) entry).getPrivateKey());
            String message = ""+orderJsonObject.getDouble("total_price")+","+sequence_id;
            System.out.println("Sending Message: " + message);
            s.update(message.getBytes());
            byte[] signature = s.sign();
            RequestParams requestParams = new RequestParams();
            requestParams.add("user_id", user_id);
            requestParams.add("order_jwt", orderJwt);
            requestParams.add("signature", Base64.encodeToString(signature, 0));
            requestParams.add("sequence_id", sequence_id+"");

            System.out.println(message);
            System.out.println("user_id" + user_id);
            System.out.println("order_jwt" + orderJwt);
            System.out.println("signature" + Base64.encodeToString(signature, 0));
            System.out.println("sequence_id" + sequence_id);

            PaymentRestClient.postByUrl("http://203.159.32.54:80/orders", requestParams, new AsyncHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String respBodyStr = new String(responseBody);
                    System.out.println(respBodyStr);
                    try {
                        JSONObject jsonObject = new JSONObject(respBodyStr);
                        sequence_id = jsonObject.getInt("sequence_id");
                        balance = jsonObject.getDouble("balance");
                        Intent intent = new Intent(ConfirmOrderActivity.this, ScanQRActivity.class);
                        intent.putExtra("balance", balance);
                        intent.putExtra("sequence_id", sequence_id);
                        intent.putExtra("user_id", user_id);
                        intent.putExtra("email", email);
                        intent.putExtra("debugging_msg", respBodyStr);

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
