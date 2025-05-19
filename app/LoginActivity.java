package com.example.examevaluatorai;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.text.method.PasswordTransformationMethod;
import android.text.method.HideReturnsTransformationMethod;

import androidx.appcompat.app.AppCompatActivity;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.lifecycleScope;
import androidx.room.Room;

import com.example.examevaluatorai.data.AppDatabase;
import com.example.examevaluatorai.data.User;
import com.example.examevaluatorai.data.UserDao;

import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.GlobalScope;
import kotlinx.coroutines.launch;
import kotlinx.coroutines.withContext;

public class LoginActivity extends AppCompatActivity {

    EditText editEmail, editPassword;
    Button buttonLogin;
    TextView createLink;
    ImageView iconTogglePassword;
    boolean isPasswordVisible = false;

    AppDatabase db;
    UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        createLink = findViewById(R.id.createLink);
        iconTogglePassword = findViewById(R.id.iconTogglePassword);

        // Room DB init
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "my-db").build();
        userDao = db.userDao();

        // Şifreyi göster/gizle
        iconTogglePassword.setOnClickListener(v -> {
            if (isPasswordVisible) {
                editPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                iconTogglePassword.setImageResource(R.drawable.ic_eye_closed);
            } else {
                editPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                iconTogglePassword.setImageResource(R.drawable.ic_eye_open);
            }
            editPassword.setSelection(editPassword.length());
            isPasswordVisible = !isPasswordVisible;
        });

        // Giriş butonu
        buttonLogin.setOnClickListener(v -> {
            String username = editEmail.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show();
                return;
            }

            // Room üzerinden kullanıcı kontrolü
            GlobalScope.INSTANCE.launch(Dispatchers.getIO(), () -> {
                User user = userDao.login(username, password);
                runOnUiThread(() -> {
                    if (user != null) {
                        Toast.makeText(this, "Giriş başarılı!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Hatalı kullanıcı adı veya şifre", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });

        createLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }
}
