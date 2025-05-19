package com.example.examevaluatorai;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;


public class ResultActivity extends AppCompatActivity {

    TextView textOCR, textSimilarity;
    Button btnBack;

    // NGROK URL'i buraya ekle
    private static final String NGROK_URL = "https://2695-81-213-45-228.ngrok-free.app";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        textOCR = findViewById(R.id.textOCR);
        textSimilarity = findViewById(R.id.textSimilarity);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        String ocrResult = getIntent().getStringExtra("ocr_result");
        String expectedText = getIntent().getStringExtra("expected_text");

        textOCR.setText("OCR: " + ocrResult);

        JSONObject body = new JSONObject();
        try {
            body.put("ocr_text", ocrResult);
            body.put("expected_text", expectedText);
        } catch (Exception e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                NGROK_URL + "/grade",
                body,
                response -> {
                    try {
                        double score = response.getDouble("similarity");
                        textSimilarity.setText("Benzerlik: %" + score);
                    } catch (Exception e) {
                        e.printStackTrace();
                        textSimilarity.setText("Benzerlik hesaplanamadı.");
                    }
                },
                error -> {
                    error.printStackTrace();
                    textSimilarity.setText("Sunucuya bağlanılamadı.");
                }
        );

        Volley.newRequestQueue(this).add(request);
    }
}
