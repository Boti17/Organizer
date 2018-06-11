package com.example.szabo.organizer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.internal.ImageRequest;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText mLoginEmailField;
    private EditText mLoginPasswordField;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers;
    private ProgressDialog mProgress;
    private static final int RC_SIGN_IN = 1;
    private GoogleApiClient mGoogleApiClient;
    private LoginButton mFacebookButton;
    private CallbackManager callbackManager;
    private AccessToken facebookAccessToken;
    private static final int RT_EMAIL = 1;
    private static final int RT_GOOGLE = 2;
    private static final int RT_FACEBOOK = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("users");
        mDatabaseUsers.keepSynced(true);
        mLoginEmailField = (EditText) findViewById(R.id.loginEmailField);
        mLoginPasswordField = (EditText) findViewById(R.id.loginPasswordField);
        Button mLoginButton = (Button) findViewById(R.id.loginButton);
        mProgress = new ProgressDialog(this);

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLogin();
            }
        });

        Button mNewAccountButton = (Button) findViewById(R.id.newAccountButton);
        mNewAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNewIntent(RegisterActivity.class);
            }
        });

        Button mForgotPasswordButton = (Button) findViewById(R.id.forgotPasswordButton);
        mForgotPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNewIntent(ForgotPasswordActivity.class);
            }
        });

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(LoginActivity.this, getResources().getString(R.string.googleSignInFailed), Toast.LENGTH_LONG).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        SignInButton mGoogleButton = (SignInButton) findViewById(R.id.googleButton);
        mGoogleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        mFacebookButton = (LoginButton) findViewById(R.id.facebookButton);
        initializeFacebookLogin();
    }

    private void checkLogin() {
        final String email = mLoginEmailField.getText().toString().trim();
        String password = mLoginPasswordField.getText().toString().trim();
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password))
        {
            mProgress.setMessage(getResources().getString(R.string.checkingLogin));
            mProgress.show();
            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful())
                    {
                        mProgress.dismiss();
                        checkUserExists(email, "", null, RT_EMAIL);
                    }
                    else
                    {
                        mProgress.dismiss();
                        String exception = task.getException().getMessage();
                        if (exception.contains("The given password is invalid"))
                        {
                            Toast.makeText(LoginActivity.this, getResources().getString(R.string.invalidPassword), Toast.LENGTH_LONG).show();
                        }
                        else if (exception.contains("The password is invalid"))
                        {
                            Toast.makeText(LoginActivity.this, getResources().getString(R.string.wrongPassword), Toast.LENGTH_LONG).show();
                        }
                        else if (exception.contains("email address is badly formatted"))
                        {
                            Toast.makeText(LoginActivity.this, getResources().getString(R.string.badEmail), Toast.LENGTH_LONG).show();
                        }
                        else if (exception.contains("There is no user record"))
                        {
                            Toast.makeText(LoginActivity.this, getResources().getString(R.string.noUser), Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            Toast.makeText(LoginActivity.this, getResources().getString(R.string.loginFailed), Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });
        }
        else
        {
            Toast.makeText(this, getResources().getString(R.string.allField),Toast.LENGTH_LONG).show();
        }
    }

    private void checkUserExists(final String email, final String name, final Uri pictureUri, final int registerType) {
        if (mAuth.getCurrentUser() != null) {
            final String userId = mAuth.getCurrentUser().getUid();
            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild(userId)) {
                        mDatabaseUsers.child(userId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (registerType != RT_EMAIL && (!dataSnapshot.child("name").getValue().equals(name) || !dataSnapshot.child("picture").getValue().equals(pictureUri.toString())))
                                {
                                    User user = new User(email, name, pictureUri.toString(), registerType);
                                    mDatabaseUsers.child(userId).setValue(user);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        startNewIntent(EventListActivity.class);
                    } else {
                        if (registerType == RT_EMAIL && name.equals("") && pictureUri==null) {
                            startNewIntent(SetupActivity.class);
                        }
                        else
                        {
                            User user = new User(email, name, pictureUri.toString(), RT_FACEBOOK);
                            mDatabaseUsers.child(userId).setValue(user);
                            startNewIntent(EventListActivity.class);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            mProgress.setMessage(getResources().getString(R.string.checkingLogin));
            mProgress.show();
            if (result.isSuccess())
            {
                GoogleSignInAccount account = result.getSignInAccount();
                fireBaseAuthWithGoogle(account);
            }
            else
            {
                Toast.makeText(LoginActivity.this, getResources().getString(R.string.googleSignInFailed), Toast.LENGTH_LONG).show();
                mProgress.dismiss();
            }
        }
        else
        {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void fireBaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String email = mAuth.getCurrentUser().getEmail();
                            String name = mAuth.getCurrentUser().getDisplayName();
                            Uri profilePictureUri = mAuth.getCurrentUser().getPhotoUrl();
                            profilePictureUri = Uri.parse(profilePictureUri.toString().replace("/s96-c/","/s300-c/"));
                            mProgress.dismiss();
                            checkUserExists(email, name,profilePictureUri, RT_GOOGLE);
                        } else {
                            Toast.makeText(LoginActivity.this, getResources().getString(R.string.googleSignInFailed), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void initializeFacebookLogin()
    {
        callbackManager = CallbackManager.Factory.create();
        mFacebookButton.setReadPermissions("email","public_profile");
        mFacebookButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                facebookAccessToken = loginResult.getAccessToken();
                handleFacebookAccessToken(facebookAccessToken);
            }

            @Override
            public void onCancel() {
                Toast.makeText(LoginActivity.this, getResources().getString(R.string.cancelled), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(LoginActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mProgress.setMessage(getResources().getString(R.string.checkingLogin));
        mProgress.show();
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful())
                {
                    String email = mAuth.getCurrentUser().getEmail();
                    String name = mAuth.getCurrentUser().getDisplayName();
                    int dimensionPixelSize = getResources().getDimensionPixelSize(com.example.szabo.organizer.R.dimen.com_facebook_profilepictureview_preset_size_large);
                    Uri profilePictureUri = ImageRequest.getProfilePictureUri(Profile.getCurrentProfile().getId(), dimensionPixelSize , dimensionPixelSize );
                    mProgress.dismiss();
                    checkUserExists(email,name,profilePictureUri,RT_FACEBOOK);
                } else {
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.fbSignInFailed), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void startNewIntent(Class activityClass)
    {
        Intent intent = new Intent(LoginActivity.this, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
