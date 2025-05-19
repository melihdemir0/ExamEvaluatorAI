package com.example.examevaluatorai;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import com.android.volley.DefaultRetryPolicy;


public class MainActivity extends AppCompatActivity {

    // 📌 Ngrok ve yerel IP üzerinden bağlantı ayarları
    private static final boolean USE_NGROK = true;
    private static final String NGROK_URL = "https://2695-81-213-45-228.ngrok-free.app"; // ← kendi ngrok URL'ini yaz
    private static final String LOCAL_IP_URL = "http://192.168.1.23:5000";  // pc ip

    private static final int REQUEST_IMAGE_PICK = 100;
    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private static final int CAMERA_PERMISSION_CODE = 102;

    Button btnUploadImage, btnExit, btnTakePhoto;
    EditText editReference;

    String expectedText = "";
    private boolean isImageSelected = false;
    private boolean isAnswerConfirmed = false;
    private Bitmap selectedBitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("MainActivity", "MainActivity açıldı.");

        btnUploadImage = findViewById(R.id.btnUploadImage);
        btnExit = findViewById(R.id.btnExit);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        editReference = findViewById(R.id.editReference);

        btnUploadImage.setEnabled(false);
        btnTakePhoto.setEnabled(false);

        Button btnConfirm = findViewById(R.id.btnConfirm);
        btnConfirm.setOnClickListener(v -> {
            if (!isAnswerConfirmed) {
                // İlk tıklama: cevap kontrolü
                expectedText = editReference.getText().toString().trim();
                if (expectedText.isEmpty()) {
                    Toast.makeText(this, "Beklenen cevap boş olamaz", Toast.LENGTH_SHORT).show();
                    return;
                }
                btnUploadImage.setEnabled(true);
                btnTakePhoto.setEnabled(true);
                btnConfirm.setText("KARŞILAŞTIR");
                Toast.makeText(this, "Cevap onaylandı. Artık görsel seçebilirsiniz.", Toast.LENGTH_SHORT).show();
                isAnswerConfirmed = true;
            } else {
                // İkinci tıklama: dosya seçilip seçilmediği kontrolü
                if (!isImageSelected) {
                    Toast.makeText(this, "Lütfen dosya yükleyin veya fotoğraf çekin", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(this, "Kontrol ediliyor...", Toast.LENGTH_SHORT).show();

                // OCR API çağrısı başlatılıyor
                sendImageToServer(selectedBitmap, expectedText);
            }
        });

        btnUploadImage.setOnClickListener(v -> {
            if (!isAnswerConfirmed) {
                Toast.makeText(this, "Lütfen önce cevabı onaylayın", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_IMAGE_PICK);
        });

        btnTakePhoto.setOnClickListener(v -> {
            if (!isAnswerConfirmed) {
                Toast.makeText(this, "Lütfen önce cevabı onaylayın", Toast.LENGTH_SHORT).show();
                return;
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
            } else {
                openCamera();
            }
        });

        btnExit.setOnClickListener(v -> {
            finishAffinity();
            System.exit(0);
        });
    }
    private void openCamera() {
        Log.d("MainActivity", "openCamera() çağrıldı.");

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        PackageManager pm = getPackageManager();

        if (takePictureIntent.resolveActivity(pm) != null) {
            Log.d("MainActivity", "Kamera uygulaması mevcut: " + takePictureIntent.resolveActivity(pm).getPackageName());
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Log.e("MainActivity", "Cihazda kamera uygulaması bulunamadı.");
            Toast.makeText(this, "Kamera açılamıyor", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Kamera izni reddedildi", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("MainActivity", "onActivityResult çağrıldı. requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (resultCode == RESULT_OK && data != null) {
            Bitmap bitmap = null;

            if (requestCode == REQUEST_IMAGE_PICK) {
                Uri selectedImageUri = data.getData();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Resim alınamadı", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
                bitmap = (Bitmap) data.getExtras().get("data");
            }

            if (bitmap != null) {
                isImageSelected = true;
                selectedBitmap = bitmap;
                Toast.makeText(this, "Resim seçildi", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String bitmapToBase64(Bitmap bitmap) {
        // 1. Görseli yeniden boyutlandır (örneğin 800x800 ile sınırla)
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, 600, 600, true);

        // 2. Sıkıştır ve base64'e çevir
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        resized.compress(Bitmap.CompressFormat.JPEG, 80, outputStream); // %80 kalite
        byte[] imageBytes = outputStream.toByteArray();
        return android.util.Base64.encodeToString(imageBytes, android.util.Base64.NO_WRAP); // NO_WRAP önemli
    }

    private void sendImageToServer(Bitmap bitmap, String expectedText) {
        Log.d("OCR", "sendImageToServer() çağrıldı.");

        // NGROK ayarını kullan
        String url = (USE_NGROK ? NGROK_URL : LOCAL_IP_URL) + "/predict";

        Log.d("OCR", "Sunucuya istek gönderiliyor... URL: " + url);

        String base64Image = bitmapToBase64(bitmap);

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("image", base64Image);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("OCR", "JSON oluşturulurken hata: " + e.getMessage());
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                requestBody,
                response -> {
                    Log.d("OCR", "Yanıt alındı: " + response.toString());
                    try {
                        String result = response.getString("text");
                        Log.d("OCR", "Gelen text: " + result);
                        Log.d("OCR", "Beklenen text: " + expectedText);

                        Intent intent = new Intent(MainActivity.this, ResultActivity.class);
                        intent.putExtra("ocr_result", result);
                        intent.putExtra("expected_text", expectedText);
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("OCR", "Yanıt işlenemedi: " + e.getMessage());
                        Toast.makeText(this, "OCR sonucu çözülemedi", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Log.e("OCR", "Sunucuya bağlanılamadı: " + error.getMessage());
                    Toast.makeText(this, "Sunucuya bağlanılamadı", Toast.LENGTH_SHORT).show();
                }
        );

        RequestQueue queue = Volley.newRequestQueue(this);

        request.setRetryPolicy(new DefaultRetryPolicy(
                20000, // timeout (20 saniye)
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        queue.add(request);
    }

}
