package com.example.firebaseapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    //khai báo các thành phần giao diện
    Button mDangKyBtn, mDangNhapBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        //khởi tạo các thành phần giao diện
        mDangKyBtn = findViewById(R.id.register_btn);
        mDangNhapBtn = findViewById(R.id.login_btn);

        //xử lý khi nút đăng ký được nhấn
        mDangKyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //chuyển sang RegisterActivity
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));

            }
        });

        //xử lý khi nút đăng nhập được nhấn
        mDangNhapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //chuyển sang LoginActivity
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });
    }
}
