package com.example.examevaluatorai;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.examevaluatorai.data.UserDatabaseHelper;

public class LoginActivity extends AppCompatActivity {

    EditText editUsername, editPassword;
    Button buttonLogin;
    TextView createLink;
    ImageView iconTogglePassword;
    boolean isPasswordVisible = false;

    UserDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        createLink = findViewById(R.id.createLink);
        iconTogglePassword = findViewById(R.id.iconTogglePassword);

        dbHelper = new UserDatabaseHelper(this);

        // ENTER ile geçiş: kullanıcı adı → şifre
        editUsername.setOnEditorActionListener((v, actionId, event) -> {
            editPassword.requestFocus();
            return true;
        });

        // ENTER ile giriş: şifre → login butonuna bas
        editPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL) {
                buttonLogin.performClick();
                return true;
            }
            return false;
        });

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

        buttonLogin.setOnClickListener(v -> {
            String username = editUsername.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            SQLiteDatabase db = dbHelper.getReadableDatabase();

            String[] columns = {UserDatabaseHelper.COL_ID};
            String selection = UserDatabaseHelper.COL_USERNAME + "=? AND " + UserDatabaseHelper.COL_PASSWORD + "=?";
            String[] selectionArgs = {username, password};

            Cursor cursor = db.query(UserDatabaseHelper.TABLE_NAME, columns, selection, selectionArgs, null, null, null);

            if (cursor.moveToFirst()) {
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show();
            }

            cursor.close();
        });

        createLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }
}
