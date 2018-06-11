package com.example.szabo.organizer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class ProfileActivity extends AppCompatActivity {

    private Context context;
    private static final int RT_EMAIL = 1;
    private int userRegisterType;
    private static final int GALLERY_REQUEST = 10;
    private DatabaseReference mDatabase;
    private String userId;
    private FirebaseAuth mAuth;
    private StorageReference mStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Bundle extras = getIntent().getExtras();
        userId = extras.getString("userId");
        context = this;
        final ImageView mProfilePicture = (ImageView) findViewById(R.id.profilePicture);
        final TextView mNameField = (TextView) findViewById(R.id.nameField);
        final TextView mContactField = (TextView) findViewById(R.id.contactField);
        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference().child("pictures");
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
        mDatabase.addValueEventListener(new ValueEventListener() {
            @SuppressLint("CheckResult")
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String picture = dataSnapshot.child("picture").getValue().toString();
                Glide
                        .with(context)
                        .load(picture)
                        .into(mProfilePicture);
                String name = dataSnapshot.child("name").getValue().toString();
                name = getResources().getString(R.string.userName)+": "+name;
                mNameField.setText(name);
                String contact = dataSnapshot.child("email").getValue().toString();
                contact = getResources().getString(R.string.contact)+" "+contact;
                mContactField.setText(contact);
                userRegisterType = Integer.parseInt(dataSnapshot.child("registerType").getValue().toString());
                showButtons();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK)
        {
            Uri imageUri = data.getData();
            StorageReference filePath = mStorage.child(imageUri.getLastPathSegment());
            filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    String downloadUri = taskSnapshot.getDownloadUrl().toString();
                    mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
                    mDatabase.child("picture").setValue(downloadUri);
                }
            });
        }
    }

    private void showButtons()
    {
        Button logOutButton = (Button) findViewById(R.id.logOutButton);
        Button changePictureButton = (Button) findViewById(R.id.changePictureButton);
        if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().getUid().equals(userId)) {
            logOutButton.setVisibility(View.VISIBLE);
            logOutButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAuth.signOut();
                    LoginManager.getInstance().logOut();
                    finish();
                }
            });
            if (userRegisterType == RT_EMAIL)
            {
                changePictureButton.setVisibility(View.VISIBLE);
                changePictureButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent galleryIntent = new Intent();
                        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                        galleryIntent.setType("image/*");
                        startActivityForResult(galleryIntent, GALLERY_REQUEST);
                    }
                });
            }
        }
    }
}
