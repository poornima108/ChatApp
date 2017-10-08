package com.example.poornima.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {
    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;
    private static final int GALLERY_PICK=1;
    private StorageReference mImageStorage;

    private CircleImageView mDisplayImage;
    private TextView mName;
    private TextView mstatus;
    private Button mStatusBtn;
    private Button mImageBtn;
    private ProgressDialog mProgressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mDisplayImage = (CircleImageView)findViewById(R.id.user_single_image);
        mName = (TextView)findViewById(R.id.settings_display_name);
        mstatus = (TextView)findViewById(R.id.settings_status);
        mStatusBtn=(Button)findViewById(R.id.settings_status_btn);
        mImageBtn=(Button)findViewById(R.id.settings_image_btn);
        mImageStorage= FirebaseStorage.getInstance().getReference();
        mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();
        String current_uid= mCurrentUser.getUid();


        mUserDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        mUserDatabase.keepSynced(true);
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name= dataSnapshot.child("name").getValue(String.class);
                final String image = dataSnapshot.child("image").getValue(String.class);
                String status = dataSnapshot.child("status").getValue(String.class);
                String thumb_image = dataSnapshot.child("thumb_image").getValue(String.class);
                mName.setText(name);
                mstatus.setText(status);
                if(!image.equals("default")) {
                   // Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.avatar).into(mDisplayImage);
                    Picasso.with(SettingsActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.avatar).into(mDisplayImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {

                            Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.avatar).into(mDisplayImage);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
mStatusBtn.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {

        String status_value= mstatus.getText().toString();
        Intent status_intent = new Intent(SettingsActivity.this,StatusActivity.class);
        status_intent.putExtra("status_value",status_value);
        startActivity(status_intent);

    }
});
        mImageBtn.setOnClickListener(new View.OnClickListener() {
           /* @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(SettingsActivity.this);
            }*/
            @Override
            public void onClick(View v) {
                Intent galleryIntent=new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent,"SELECT IMAGE"),GALLERY_PICK);
            }


        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode== RESULT_OK) {
        Uri imageUri= data.getData();

            CropImage.activity(imageUri)
                    .setAspectRatio(1,1)
                    .start(this);
           // Toast.makeText(SettingsActivity.this,imageUri,Toast.LENGTH_LONG).show();
    }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mProgressDialog=new ProgressDialog(SettingsActivity.this);
                mProgressDialog.setTitle("Uploading image..");
                mProgressDialog.setMessage("Please wait while we process and upload the image !");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                Uri resultUri = result.getUri();
                final File thumb_filePath =new File(resultUri.getPath());
                String current_user_id=mCurrentUser.getUid();
                byte[] thumb_byte = new byte[0];
                try {
                    Bitmap thumb_bitmap = new Compressor(this).
                            setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_filePath);
                    ByteArrayOutputStream baos=new ByteArrayOutputStream();
                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
                    thumb_byte =baos.toByteArray();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                StorageReference filepath= mImageStorage.child("profile_images").child(current_user_id+".jpg");
                final StorageReference thumb_filepath= mImageStorage.child("profile_images").child("thumbs").child(current_user_id+".jpg");
                final byte[] finalThumb_byte = thumb_byte;
                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                   if(task.isSuccessful()){
                       final String download_url =task.getResult().getDownloadUrl().toString();

                       thumb_filepath.putBytes(finalThumb_byte).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                           @Override
                             public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {
                               String thumb_downloadUrl = thumb_task.getResult().getDownloadUrl().toString();
                               if(thumb_task.isSuccessful()) {

                                   Map update_hashMap = new HashMap();
                                   update_hashMap.put("image",download_url);
                                    update_hashMap.put("thumb_image",thumb_downloadUrl);

                                   mUserDatabase.updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                       @Override
                                       public void onComplete(@NonNull Task<Void> task) {
                                           if (task.isSuccessful()) {
                                               mProgressDialog.dismiss();
                                               Toast.makeText(SettingsActivity.this, "Success uploading", Toast.LENGTH_LONG).show();
                                           }
                                       }
                                   });
                               } else {
                                   Toast.makeText(SettingsActivity.this, "Error uploading thumbnail", Toast.LENGTH_LONG).show();
                                   mProgressDialog.dismiss();
                               }
                           }
                       });

                   }else{
                       Toast.makeText(SettingsActivity.this,"Error uploading the image",Toast.LENGTH_LONG).show();
                       mProgressDialog.dismiss();
                   }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(10);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }
}
