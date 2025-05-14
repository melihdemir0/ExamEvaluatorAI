package com.example.examevaluatorai;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_PICK = 100;
    Button btnUploadImage, btnExit;
    EditText editReference;
    String expectedText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnUploadImage = findViewById(R.id.btnUploadImage);
        btnExit = findViewById(R.id.btnExit);
        editReference = findViewById(R.id.editReference);
        Button btnConfirm = findViewById(R.id.btnConfirm);

        btnUploadImage.setEnabled(false); // Başta devre dışı

        btnConfirm.setOnClickListener(v -> {
            expectedText = editReference.getText().toString().trim();
            if (expectedText.isEmpty()) {
                Toast.makeText(this, "Beklenen cevap boş olamaz", Toast.LENGTH_SHORT).show();
            } else {
                btnUploadImage.setEnabled(true);
                Toast.makeText(this, "Cevap onaylandı. Şimdi görseli seçebilirsiniz.", Toast.LENGTH_SHORT).show();
            }
        });

        btnUploadImage.setOnClickListener(v -> {
            if (expectedText == null || expectedText.isEmpty()) {
                Toast.makeText(this, "Lütfen beklenen cevabı onaylayın", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_IMAGE_PICK);
        });

        btnExit.setOnClickListener(v -> {
            finishAffinity();
            System.exit(0);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                sendImageToServer(bitmap, expectedText);  // OCR'a ve referansla birlikte gönder
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Resim alınamadı", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        byte[] imageBytes = outputStream.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private void sendImageToServer(Bitmap bitmap, String expectedText) {
        String url = "http://10.0.2.2:5000/predict";

        String base64Image = bitmapToBase64(bitmap);

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("image", base64Image);
        } catch (Exception e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                requestBody,
                response -> {
                    try {
                        String result = response.getString("text");

                        // 🔁 OCR sonucu ve beklenen cevabı yeni ekrana gönder
                        Intent intent = new Intent(this, ResultActivity.class);
                        intent.putExtra("ocr_result", result);
                        intent.putExtra("expected_text", expectedText);
                        startActivity(intent);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "OCR sonucu çözülemedi", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Sunucuya bağlanılamadı", Toast.LENGTH_SHORT).show();
                }
        );

        Volley.newRequestQueue(this).add(request);
    }
}
