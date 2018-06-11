package com.example.szabo.organizer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private EditText mEmailField;
    private EditText mPasswordField;
    private EditText mPasswordAgainField;
    private FirebaseAuth mAuth;
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mEmailField = (EditText) findViewById(R.id.emailField);
        mPasswordField = (EditText) findViewById(R.id.passwordField);
        mPasswordAgainField = (EditText) findViewById(R.id.passwordAgainField);
        Button mRegisterButton = (Button) findViewById(R.id.registerButton);

        mProgress = new ProgressDialog(this);

        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRegister();
            }
        });
    }

    private void startRegister() {
        String email = mEmailField.getText().toString().trim();
        String password = mPasswordField.getText().toString().trim();
        String passwordAgain = mPasswordAgainField.getText().toString().trim();

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(passwordAgain))
        {
            if (password.equals(passwordAgain)) {
                mProgress.setMessage(getResources().getString(R.string.signingUp));
                mProgress.show();
                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mProgress.dismiss();
                            Intent setupIntent = new Intent(RegisterActivity.this, SetupActivity.class);
                            setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(setupIntent);
                        }
                        else
                        {
                            mProgress.dismiss();
                            String exception = task.getException().getMessage();
                            if (exception.contains("password is invalid"))
                            {
                                Toast.makeText(RegisterActivity.this, getResources().getString(R.string.invalidPassword), Toast.LENGTH_LONG).show();
                            }
                            if (exception.contains("email address is already in use"))
                            {
                                Toast.makeText(RegisterActivity.this, getResources().getString(R.string.usedEmail), Toast.LENGTH_LONG).show();
                            }
                            if (exception.contains("email address is badly formatted"))
                            {
                                Toast.makeText(RegisterActivity.this, getResources().getString(R.string.badEmail), Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
            }
            else
            {
                Toast.makeText(this, getResources().getString(R.string.differentPasswords),Toast.LENGTH_LONG).show();
            }
        }
        else
        {
            Toast.makeText(this, getResources().getString(R.string.allField),Toast.LENGTH_LONG).show();
        }
    }
}
