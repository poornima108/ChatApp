package com.example.poornima.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    private TextInputLayout mName;
    private TextInputLayout mEmail;
    private TextInputLayout mPassword;
    private Button mCreateBtn;
    private Toolbar mToolbar;
    private ProgressDialog mRegProgress;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseAuth.AuthStateListener mAuthListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mToolbar=(Toolbar) findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRegProgress =new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        mName = (TextInputLayout) findViewById(R.id.reg_name);
        mEmail = (TextInputLayout) findViewById(R.id.reg_email);
        mPassword = (TextInputLayout) findViewById(R.id.reg_password);
        mCreateBtn = (Button) findViewById(R.id.reg_create_btn);

        mCreateBtn.setOnClickListener(new View.OnClickListener() {
                                          @Override
                                          public void onClick(View v) {
                                              String name=mName.getEditText().getText().toString();
                                              String email=mEmail.getEditText().getText().toString();
                                              String password=mPassword.getEditText().getText().toString();
                                                if(!TextUtils.isEmpty(name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){
                                                    mRegProgress.setTitle("Registering user");
                                                    mRegProgress.setMessage("Please wait while we create your account !");
                                                    mRegProgress.setCanceledOnTouchOutside(false);
                                                    mRegProgress.show();
                                                    register_user(name,email,password);
                                                }else{
                                                    Toast.makeText(RegisterActivity.this,"Cannot sign in please check the form and try again !",Toast.LENGTH_SHORT).show();
                                                }
                                          }
                                      }

        );
    }

    private void register_user(final String name, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
            if(task.isSuccessful()){
                FirebaseUser current_user=FirebaseAuth.getInstance().getCurrentUser();
                String uid=current_user.getUid();
                mDatabase=FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
                HashMap<String,String> userMap=new HashMap<>();
                userMap.put("name",name);
                userMap.put("status","I am online");
                userMap.put("image","default");
                userMap.put("thumb_image","default");
                mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            mRegProgress.dismiss();
                            Intent mainintent = new Intent(RegisterActivity.this,StartActivity.class);
                            mainintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(mainintent);
                            finish();
                        }
                    }
                });

            }else{
                mRegProgress.hide();
                Toast.makeText(RegisterActivity.this,"Cannot sign in please check the form and try again !",Toast.LENGTH_SHORT).show();
            }
            }
                                                                                    }

        );
    }
}
