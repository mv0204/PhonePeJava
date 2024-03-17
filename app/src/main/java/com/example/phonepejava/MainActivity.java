package com.example.phonepejava;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.phonepe.intent.sdk.api.B2BPGRequest;
import com.phonepe.intent.sdk.api.B2BPGRequestBuilder;
import com.phonepe.intent.sdk.api.PhonePe;
import com.phonepe.intent.sdk.api.PhonePeInitException;
import com.phonepe.intent.sdk.api.models.PhonePeEnvironment;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.security.MessageDigest;

public class MainActivity extends AppCompatActivity {
    private Button payBtn;
    private String merchantId = "PHONEPEPGUAT8";
    private String merchantTransactionId = "transaction_123";
    private String apiEndPoint = "/pg/v1/pay";
    private String salt = "aeed1568-1a76-4fa4-9f47-3e1c81232660";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        payBtn = findViewById(R.id.payBtn);

        PhonePe.init(this, PhonePeEnvironment.SANDBOX, merchantId, null);

        JSONObject data = new JSONObject();
        try {
            data.put("merchantTransactionId", merchantTransactionId);

        data.put("merchantId", merchantId);
        data.put("merchantUserId", "90223250");
        data.put("amount", 1000);
        data.put("mobileNumber", "9999999999");
        data.put("callbackUrl", "https://papayacoders.in");

        JSONObject paymentInstrument = new JSONObject();
        paymentInstrument.put("type", "UPI_INTENT");
        paymentInstrument.put("targetApp", "com.phonepe.simulator");
        data.put("paymentInstrument", paymentInstrument);

        JSONObject deviceContext = new JSONObject();
        deviceContext.put("deviceOS", "ANDROID");
        data.put("deviceContext", deviceContext);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        String payloadBase64 = android.util.Base64.encodeToString(
                data.toString().getBytes(Charset.defaultCharset()), android.util.Base64.NO_WRAP
        );

        String checksum = sha256(payloadBase64 + apiEndPoint + salt) + "###1";

        B2BPGRequest b2BPGRequest = new B2BPGRequestBuilder()
                .setData(payloadBase64)
                .setChecksum(checksum)
                .setUrl(apiEndPoint)
                .build();

        payBtn.setOnClickListener(view -> {
            Log.d("PAPAYACODERS", "payload: " + payloadBase64);
            Log.d("PAPAYACODERS", "checksum: " + checksum);
            Log.d("PAPAYACODERS", "xVerify: " +
                    sha256("/pg/v1/status/" + merchantId + "/" + merchantTransactionId + salt) + "###1"
            );

            try {
                Intent intent = PhonePe.getImplicitIntent(MainActivity.this, b2BPGRequest, "com.phonepe.simulator");
                if (intent != null) {
                    startActivityForResult(intent, 1);
                }
            } catch (PhonePeInitException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            Log.d("PAPAYACODERS", "onActivityResult: " + data);
            Log.d("PAPAYACODERS", "onActivityResult: " + data.getData());

            /*This callback indicates only about completion of UI flow.
            Inform your server to make the transaction
            status call to get the status. Update your app with the
            success/failure status.*/
        }
    }

    private String sha256(String input) {
        byte[] bytes = input.getBytes(Charset.forName("UTF-8"));
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(bytes);
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}

