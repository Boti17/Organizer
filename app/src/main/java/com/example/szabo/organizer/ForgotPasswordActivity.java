package com.example.szabo.organizer;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText mEmailField;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();
        mEmailField = (EditText) findViewById(R.id.emailField);

        Button mSendButton = (Button) findViewById(R.id.sendButton);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isNetworkConnected()) {
                    Toast.makeText(ForgotPasswordActivity.this, getResources().getString(R.string.checkInternet), Toast.LENGTH_LONG).show();
                }else {
                    String email = mEmailField.getText().toString();
                    mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(ForgotPasswordActivity.this, getResources().getString(R.string.emailSent), Toast.LENGTH_LONG).show();
                                Intent listIntent = new Intent(ForgotPasswordActivity.this, EventListActivity.class);
                                listIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(listIntent);
                            }
                            else
                            {
                                String exception = task.getException().getMessage();
                                if (exception.contains("email address is badly formatted"))
                                {
                                    Toast.makeText(ForgotPasswordActivity.this, getResources().getString(R.string.badEmail), Toast.LENGTH_LONG).show();
                                }
                                else if (exception.contains("There is no user record"))
                                {
                                    Toast.makeText(ForgotPasswordActivity.this, getResources().getString(R.string.noUser), Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }
}
