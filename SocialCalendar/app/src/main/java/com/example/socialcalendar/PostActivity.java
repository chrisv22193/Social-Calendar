package com.example.socialcalendar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class PostActivity extends AppCompatActivity {

    private ImageButton SelectPostImage;
    private Button PostButton;
    private EditText PostDescription;

    private FirebaseAuth mAuth;
    private StorageReference PostImagesReference;
    private DatabaseReference usersRef, PostRef;

    private static final int Gallery_Pick = 1;
    private Uri ImageUri;
    private String Description;

    private String saveCurrentDate, saveCurrentTime, postRandomName, downloadUrl, currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getUid();
        PostImagesReference = FirebaseStorage.getInstance().getReference();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostRef = FirebaseDatabase.getInstance().getReference().child("Post");

        SelectPostImage = (ImageButton) findViewById(R.id.select_post_image);
        PostButton = (Button) findViewById(R.id.post_button);
        PostDescription = (EditText) findViewById(R.id.post_description);

        SelectPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenGallery();
            }
        });

        PostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidatePostInfo();
            }
        });

    }

    private void ValidatePostInfo() {
        Description = PostDescription.getText().toString();

        if(ImageUri == null){
            Toast.makeText(this, "Please select an image to post", Toast.LENGTH_SHORT);
        }
        else if(TextUtils.isEmpty(Description)){
            Toast.makeText(this, "Please write your description for this post", Toast.LENGTH_SHORT);
        }
        else{
            StoringImageToFirebaseStorage();
        }
    }

    private void StoringImageToFirebaseStorage() {
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMMM-dd-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        postRandomName = saveCurrentDate + saveCurrentTime;


        StorageReference filePath = PostImagesReference.child("Post Images").child(ImageUri.getLastPathSegment() + postRandomName + ".jpg");

        filePath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    downloadUrl = task.getResult().getMetadata().getReference().getDownloadUrl().toString();
                    Toast.makeText(PostActivity.this,"Image uploaded successfully to storage", Toast.LENGTH_SHORT);

                    SavingPostInformationToDatabase();
                }
                else{
                    String message = task.getException().getMessage();
                    Toast.makeText(PostActivity.this, "Error occurred: " + message, Toast.LENGTH_SHORT);
                }
            }
        });
    }

    private void SavingPostInformationToDatabase() {
        usersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String userFullName = dataSnapshot.child("fullname").getValue().toString();
                    String userProfileImage = dataSnapshot.child("profileimage").getValue().toString();

                    HashMap postMap = new HashMap();
                        postMap.put("uid", currentUserID);
                        postMap.put("date", saveCurrentDate);
                        postMap.put("time", saveCurrentTime);
                        postMap.put("description", Description);
                        postMap.put("postimage", downloadUrl);
                        postMap.put("profileimage", userProfileImage);
                        postMap.put("fullname", userFullName);
                    PostRef.child(currentUserID + postRandomName).updateChildren(postMap).addOnCompleteListener(new OnCompleteListener() {
                         @Override
                         public void onComplete(@NonNull Task task) {
                             if(task.isSuccessful()){
                                 SendUserToMainActivity();
                                 Toast.makeText(PostActivity.this, "New post is updated successfully", Toast.LENGTH_SHORT);
                             }
                             else{
                                 Toast.makeText(PostActivity.this, "Error occurred while uploading your post", Toast.LENGTH_SHORT);
                             }
                         }
                     });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(PostActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void OpenGallery() {
        Intent galleryImageIntent = new Intent();
        galleryImageIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryImageIntent.setType("image/*");
        startActivityForResult(galleryImageIntent, Gallery_Pick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null){
            ImageUri = data.getData();
            SelectPostImage.setImageURI(ImageUri);
        }
    }
}