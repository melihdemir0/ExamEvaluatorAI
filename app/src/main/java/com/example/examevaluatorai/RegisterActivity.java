package com.example.examevaluatorai;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.database.sqlite.SQLiteDatabase;

import com.example.examevaluatorai.data.UserDatabaseHelper;
import android.util.Log;


public class RegisterActivity extends AppCompatActivity {

    EditText editUsername, editPassword, editEmail;
    ImageView iconTogglePassword;
    Button buttonRegister;
    TextView signInLink;
    boolean isPasswordVisible = false;

    UserDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
        );
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        setContentView(R.layout.activity_register);

        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        editEmail = findViewById(R.id.editEmail);
        buttonRegister = findViewById(R.id.buttonRegister);
        signInLink = findViewById(R.id.signInLink);
        iconTogglePassword = findViewById(R.id.iconTogglePassword);

        dbHelper = new UserDatabaseHelper(this);

        iconTogglePassword.setOnClickListener(v -> {
            if (isPasswordVisible) {
                editPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                iconTogglePassword.setImageResource(R.drawable.ic_eye_closed);
            } else {
                editPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                iconTogglePassword.setImageResource(R.drawable.ic_eye_open);
            }
            editPassword.setSelection(editPassword.length());
            isPasswordVisible = !isPasswordVisible;
        });

        buttonRegister.setOnClickListener(v -> {
            String username = editUsername.getText().toString().trim();
            String password = editPassword.getText().toString().trim();
            String email = editEmail.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(UserDatabaseHelper.COL_USERNAME, username);
            values.put(UserDatabaseHelper.COL_EMAIL, email);
            values.put(UserDatabaseHelper.COL_PASSWORD, password);

            long result = db.insert(UserDatabaseHelper.TABLE_NAME, null, values);
            if (result != -1) {
                Log.d("DB", "User inserted: username=" + username + ", email=" + email + ", password=" + password);
                Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Failed to create account", Toast.LENGTH_SHORT).show();
            }
        });

        signInLink.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}
