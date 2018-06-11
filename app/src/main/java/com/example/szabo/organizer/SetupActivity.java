package com.example.szabo.organizer;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class SetupActivity extends AppCompatActivity {

    private ImageButton mProfilePictureButton;
    private EditText mNameField;
    private static final int GALLERY_REQUEST = 1;
    private Uri profilePictureUri = null;
    private DatabaseReference mDatabaseUsers;
    private FirebaseAuth mAuth;
    private StorageReference mStorage;
    private ProgressDialog mProgress;
    private static final int RT_EMAIL = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("users");
        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference().child("pictures");
        mProfilePictureButton = (ImageButton) findViewById(R.id.profilePictureButton);
        mNameField = (EditText) findViewById(R.id.setupNameField);
        Button mFinishButton = (Button) findViewById(R.id.finishButton);
        mProgress = new ProgressDialog(this);

        mProfilePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);
            }
        });

        mFinishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSetupAccount();
            }
        });
    }

    private void startSetupAccount() {
        final String name = mNameField.getText().toString().trim();
        final String userId = mAuth.getCurrentUser().getUid();
        if (!TextUtils.isEmpty(name))
        {
            mProgress.setMessage(getResources().getString(R.string.finishingSetup));
            mProgress.show();
            StorageReference filePath;
            Uri lastUri;
            if (profilePictureUri==null)
            {
                Uri imageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                        "://" + getResources().getResourcePackageName(R.drawable.default_profile)
                        + '/' + getResources().getResourceTypeName(R.drawable.default_profile) + '/' + getResources().getResourceEntryName(R.drawable.default_profile) );
                filePath = mStorage.child(imageUri.getLastPathSegment());
                lastUri = imageUri;
            }
            else
            {
                filePath = mStorage.child(profilePictureUri.getLastPathSegment());
                lastUri = profilePictureUri;
            }

            filePath.putFile(lastUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    String email = mAuth.getCurrentUser().getEmail();
                    String downloadUri = taskSnapshot.getDownloadUrl().toString();
                    User user = new User(email,name, downloadUri,RT_EMAIL);
                    mDatabaseUsers.child(userId).setValue(user);
                    mProgress.dismiss();
                    Intent mainIntent = new Intent(SetupActivity.this, EventListActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainIntent);
                }
            });

        }
        else
        {
            Toast.makeText(this, getResources().getString(R.string.allField),Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK)
        {
            Uri imageUri = data.getData();
            profilePictureUri = imageUri;
            mProfilePictureButton.setImageURI(profilePictureUri);
        }
    }
}
