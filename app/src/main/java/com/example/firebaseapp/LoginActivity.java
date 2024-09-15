package com.example.firebaseapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    //Views
    EditText mEmailEt, mPasswordEt;
    TextView notHaveAccntTv, mRecoverPassTv;
    Button mLoginBtn;

    //Firebase
    private FirebaseAuth mAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    //Progress dialog
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //ActionBar và tiêu đề
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Đăng nhập");

        //Kích hoạt nút quay lại
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        //Cấu hình Đăng nhập Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        //Khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();

        //Khởi tạo các View
        mEmailEt = findViewById(R.id.emailEt);
        mPasswordEt = findViewById(R.id.passwordEt);
        notHaveAccntTv = findViewById(R.id.nothave_accountTv);
        mRecoverPassTv = findViewById(R.id.recoverPassTv);
        mLoginBtn = findViewById(R.id.login_btn);

        //Click nút đăng nhập
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Lấy dữ liệu nhập vào
                String email = mEmailEt.getText().toString();
                String passw = mPasswordEt.getText().toString().trim();

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    //Mẫu email không hợp lệ, thiết lập lỗi
                    mEmailEt.setError("Email không hợp lệ");
                    mEmailEt.setFocusable(true);
                } else {
                    //Mẫu email hợp lệ
                    loginUser(email, passw);
                }
            }
        });

        //Click textview "Chưa có tài khoản?"
        notHaveAccntTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                finish();
            }
        });

        //Click textview "Quên mật khẩu"
        mRecoverPassTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRecoverPasswordDialog();
            }
        });

        //Khởi tạo dialog tiến trình
        pd = new ProgressDialog(this);
    }

    private void showRecoverPasswordDialog() {
        //AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Khôi phục mật khẩu");

        //Thiết lập bố cục LinearLayout
        LinearLayout linearLayout = new LinearLayout(this);

        //Các view trong dialog
        EditText emailEt = new EditText(this);
        emailEt.setHint("Email");
        emailEt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        //Thiết lập chiều rộng tối thiểu của EditText để phù hợp với một chuỗi chữ bất kể kích thước văn bản
        emailEt.setMinEms(16);

        linearLayout.addView(emailEt);
        linearLayout.setPadding(10, 10, 10, 10);

        builder.setView(linearLayout);

        //Nút khôi phục
        builder.setPositiveButton("Khôi phục", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Lấy email nhập vào
                String email = emailEt.getText().toString().trim();
                beginRecovery(email);
            }
        });

        //Nút hủy
        builder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        //Hiển thị dialog
        builder.create().show();
    }

    private void beginRecovery(String email) {
        //Hiển thị dialog tiến trình
        pd.setMessage("Đang gửi email khôi phục...");
        pd.show();

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        pd.dismiss();
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Email đã được gửi", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginActivity.this, "Thất bại...", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        //Lấy và hiển thị thông báo lỗi
                        Toast.makeText(LoginActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loginUser(String email, String passw) {
        //Hiển thị dialog tiến trình
        pd.setMessage("Đang đăng nhập...");
        pd.show();

        mAuth.signInWithEmailAndPassword(email, passw).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    //Đóng dialog tiến trình
                    pd.dismiss();

                    //Đăng nhập thành công
                    HashMap<Object, String> hashMap = new HashMap<>();
                    FirebaseUser user = mAuth.getCurrentUser();
                    firebaseDatabase = FirebaseDatabase.getInstance();
                    databaseReference = firebaseDatabase.getReference("Users");

                    //Lấy email và uid của người dùng từ Firebase Auth
                    Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
                    query.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                String name = "" + ds.child("name").getValue();
                                String phone = "" + ds.child("phone").getValue();
                                String image = "" + ds.child("image").getValue();
                                String cover = "" + ds.child("cover").getValue();

                                Log.d("Name_image_check", "name" + name + "image" + image);
                                hashMap.put("name", name);
                                hashMap.put("onlineStatus", "online");
                                hashMap.put("typingTo", "noOne");
                                hashMap.put("phone", phone);
                                hashMap.put("image", image);
                                hashMap.put("cover", cover);

                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                String email = user.getEmail();
                                String uid = user.getUid();

                                hashMap.put("email", email);
                                hashMap.put("uid", uid);
                                hashMap.put("onlineStatus", "online");
                                hashMap.put("typingTo", "noOne");

                                DatabaseReference reference = database.getReference("Users");
                                //Lưu dữ liệu vào HashMap
                                reference.child(uid).setValue(hashMap);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });

                    //Người dùng đã đăng nhập, chuyển sang activity Dashboard
                    startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                    finish();
                } else {
                    //Đóng dialog tiến trình
                    pd.dismiss();
                    Toast.makeText(LoginActivity.this, "Đăng nhập thất bại.", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Đóng dialog tiến trình
                pd.dismiss();
                //Lỗi, lấy và hiển thị thông báo lỗi
                Toast.makeText(LoginActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); //Quay về activity trước
        return super.onSupportNavigateUp();
    }
}
