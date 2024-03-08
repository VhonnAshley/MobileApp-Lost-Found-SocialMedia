package com.example.ilostifind;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.ilostifind.Fragments.Community_Fragment;
import com.example.ilostifind.Fragments.Post_Fragment;
import com.example.ilostifind.Objects.Post;
import com.example.ilostifind.databinding.ActivityHomeBinding;
import com.example.ilostifind.databinding.ActivityWriteAPostBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.OutputStream;

public class Write_a_PostActivity extends AppCompatActivity {
    Button canbut,opengal, takeapic, post;

    TextView usernameV,statusV;

    Spinner catpost;

    EditText writedescT;

    ImageView pictureV;

    Uri pickedImgUri = null;

    Bitmap snapphoto = null;

    static int reqCode = 1;

    static int preqCode = 1;

    private static final int reqimgCode = 2;

    //Firebase Database
    DatabaseReference mypostDBref;

    //Firebase Authentication
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_a_post);

        //buttons
        canbut = findViewById(R.id.cancelwriteBut);
        opengal = findViewById(R.id.opengallbut);
        takeapic = findViewById(R.id.takeapic);
        post = findViewById(R.id.postBut);

        //TextView
        usernameV = findViewById(R.id.userwriteView);
        statusV = findViewById(R.id.pstat);

        //EditText
        writedescT = findViewById(R.id.textView9);

        //Imageview
        pictureV = findViewById(R.id.imageView4);

        //Spinner
        catpost = findViewById(R.id.category);

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        if (mUser != null){
            String name = mUser.getDisplayName();
            usernameV.setText(name);

        }

        //Post Functionality
        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                post.setVisibility(View.INVISIBLE);
                //access realtime database
                mypostDBref = FirebaseDatabase.getInstance("https://ilostifind-631c2-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Post");

                ///===========
                if (!writedescT.getText().toString().isEmpty()
                        && !catpost.getSelectedItem().toString().isEmpty()
                        && pickedImgUri != null){

                    //Accessing the Firebase Storage
                    StorageReference storageReference =  FirebaseStorage.getInstance().getReference().child("post_images");
                    StorageReference imageFilePath = storageReference.child(pickedImgUri.getLastPathSegment());


                    imageFilePath.putFile(pickedImgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override //Todo here is the shit u lookin for
                                public void onSuccess(Uri uri) {
                                    String imageDownloadLink = uri.toString();
                                    String catPostValue = catpost.getSelectedItem().toString();
                                    String pStatusValue;
                                    if (catPostValue.equals("iLost")) {
                                        pStatusValue = "Found by:";
                                    } else if (catPostValue.equals("iFound")) {
                                        pStatusValue = "Owned by:";
                                    } else {
                                        pStatusValue = ""; // default value if catpost is neither "iLost" nor "iFound"
                                    }

                                    //Create a post object
                                    Post post = new Post(catPostValue,
                                            writedescT.getText().toString(),
                                            imageDownloadLink,
                                            pStatusValue,
                                            mUser.getUid(),
                                            mUser.getDisplayName());

                                    addpost(post);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    showMessage(e.getMessage());
                                }
                            });
                        }
                    });
                }else{
                    showMessage("Please fill all input fields and choose an image");
                    post.setVisibility(View.VISIBLE);
                }
            }
        });

        //Open Gallery Functionality
        opengal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();

            }
        });

        //Cancel Function
        canbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();

            }
        });

        //Open Camera
        takeapic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 22) {
                    checkAndRequestForPermission();
                }
                else {
                    openCam();
                }
            }
        });


    }

    //Start of Method Declarations
    private void addpost(Post post) {

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://ilostifind-631c2-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference myRef = database.getReference("Post").push();

        String key = myRef.getKey();
        String cid = ""; //Clicker ID
        post.setPostid(key);
        post.setClickerID(cid);

        myRef.setValue(post).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                showMessage("Post added successfully.");
                onBackPressed();
            }
        });

    }

    //when the user picked an image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == reqCode && data != null) {
            // the user has to succesfully picked an image
            // we need to save its reference to a Uri variable
            pickedImgUri = data.getData();
            pictureV.setImageURI(pickedImgUri);
        }else if(resultCode == RESULT_OK && requestCode == reqimgCode && data != null){
            snapphoto = (Bitmap)data.getExtras().get("data");
            pickedImgUri = bitMapconvert(snapphoto);
            pictureV.setImageURI(pickedImgUri);


        }
    }

    private Uri bitMapconvert(Bitmap bitmap) {
        Uri uri = null;
        try {
            final ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Images.Media.TITLE, "title");
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            OutputStream outstream;
            outstream = getContentResolver().openOutputStream(uri);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outstream);
            outstream.close();
        } catch (Exception e) {
            System.err.println(e.toString());
        }
        return uri;
    }



    private void showMessage(String s) {
        Toast.makeText(Write_a_PostActivity.this, s, Toast.LENGTH_LONG).show();
    }

    private void openGallery() {
        Intent galintent = new Intent(Intent.ACTION_GET_CONTENT);
        galintent.setType("image/*");
        startActivityForResult(galintent,reqCode);
    }

    private void checkAndRequestForPermission() {
        if (ContextCompat.checkSelfPermission(Write_a_PostActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(Write_a_PostActivity.this, android.Manifest.permission.CAMERA)) {
                Toast.makeText(Write_a_PostActivity.this, "Please accept for required permission.",Toast.LENGTH_SHORT).show();
            }
            else {
                ActivityCompat.requestPermissions(Write_a_PostActivity.this,
                        new String []{Manifest.permission.CAMERA},
                        preqCode);
            }
        }
        else
            openCam();

    }

    private void openCam() {
        Intent camintent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(camintent, reqimgCode);

    }
}