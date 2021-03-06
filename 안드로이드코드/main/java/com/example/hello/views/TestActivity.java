package com.example.hello.views;

import static android.os.Environment.DIRECTORY_PICTURES;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.example.hello.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TestActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference databaseReference;

    final static int REQUEST_TAKE_PHOTO = 10;

    ImageView iv = null;
    ImageButton btn_takepic = null;
    Button btn_load = null;
    Button btn_cancel = null;
    EditText editText;
    private Uri filepath;

    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference conditionRef = mRootRef.child("name");
    DatabaseReference conditionValue = mRootRef.child("start_value");
    private String TAG;

    private MediaScanner mMediaScanner;
    private String imageFilePath;
    
    private ActivityResultLauncher<Uri> mGetContent;
    
    private void registerActivities() {
    
        mGetContent = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            result -> {
                if (Boolean.TRUE.equals(result)) {
                    image();
                }
            });
    }

    @Override
    protected void onCreate(Bundle vedInstanceState) {
        super.onCreate(vedInstanceState);
        setContentView(R.layout.activity_test);
        
        registerActivities();

        mMediaScanner = MediaScanner.getInstance(getApplicationContext());

        // ??????, ?????????, ????????? ?????? ?????????
        // ??????, ????????? ??? ??? ????????? ???
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                            PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "?????? ?????? ??????");
            } else {
                Log.d(TAG, "?????? ?????? ??????");
                ActivityCompat.requestPermissions(TestActivity.this, new String[]
                        {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }

        btn_takepic = (ImageButton) findViewById(R.id.camera);
        iv = (ImageView) findViewById(R.id.profileImg);
        editText = (EditText) findViewById(R.id.userEditUserName);

        // ????????? ?????? ??????
        btn_takepic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                capture();
            }
        });

        btn_load = (Button) findViewById(R.id.btn_load);
        btn_cancel = (Button) findViewById(R.id.btn_cancel);

        // ?????? ?????? ??????
        btn_load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                uploadFile();
            }
        });

        // ?????? ?????? ??????
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(TestActivity.this, MainActivity.class));
            }
        });

        database = FirebaseDatabase.getInstance();

        databaseReference = database.getReference("User");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Register", String.valueOf(error.toException()));
            }
        });
    }

    // ?????? ???????????? ?????? permission ????????? ??????
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult");
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permissson: " + permissions[0] + "was " + grantResults[0]);
        }
    }

    // ????????? ?????? ???????????? ???????????? ??????
    private File createImageFile() throws IOException {
        String name;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HHmmss", Locale.getDefault());
        Date curDate = new Date(System.currentTimeMillis());
        String num = format.format(curDate);

        if (editText.length() == 0) {
            name = "User";
        } else {
            name = editText.getText().toString();
        }
        String imageFileName = name;
        if (imageFileName.length() < 3) {
            imageFileName = "__"+imageFileName;
        }

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        imageFilePath = image.getAbsolutePath();
        return image;
    }

    // ?????? ??????
    public void capture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // ????????? ????????? ????????? ???????????????.
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (photoFile != null) {
                filepath = FileProvider.getUriForFile(getApplicationContext(), getPackageName(), photoFile);
                if (mGetContent != null)
                    mGetContent.launch(filepath);
            }
        }
    }

    private void uploadFile() {
        if (filepath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("????????? ???...");
            progressDialog.show();

            FirebaseStorage storage = FirebaseStorage.getInstance();
            FirebaseAuth auth = FirebaseAuth.getInstance();
            String name;
            if (editText.length() == 0) {
                name = auth.getCurrentUser().getDisplayName();
            } else {
                name = editText.getText().toString().trim();
            }
            conditionRef.setValue(name);
            conditionValue.setValue("True");
            String filename = name;
            // ?????? ?????? ?????? ??????
            StorageReference storageRef = storage.getReferenceFromUrl("gs://sample-e0621.appspot.com/").child("test/" + filename + ".jpg");
            final DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference().child("test/");
            final DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users/");
            Query usersFilteredByNickname =
                usersRef
                    .orderByChild("nickname")
                    .equalTo(name)
                    .limitToFirst(1);
            usersFilteredByNickname.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot user : snapshot.getChildren()) {
                            DataSnapshot userData = user.child("uid");
                            String uid = userData.getValue(String.class);
    
    
                            final DatabaseReference userPicturesRef = databaseRef.child(uid).child("profile");
                            // Firebase Realtime Database image metadata structure
                            // images/
                            // L {uid}/
                            //   L profile
                            //     L path: '/images/{uid}/profile.jpg' -> used to get hold of storage reference
                            //     L updated: '2022-04-05T01:10:30Z' -> used to get realtime updates after upload
                            //     L hash: ...
                            //   L ...
                            // L {uid2}/
                            //   L profile
                            //   L ...
                            // L {uid3}/
                            // L ...
    
                            storageRef.putFile(filepath)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    // ????????? ??????
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        // ??????????????? ??????
                                        progressDialog.dismiss();
                                        Toast.makeText(getApplicationContext(), "????????? ??????", Toast.LENGTH_SHORT).show();
                                        StorageMetadata meta = taskSnapshot.getMetadata();
                                        final String path = meta.getPath();
                                        final long created = meta.getCreationTimeMillis();
                                        final long updated = meta.getUpdatedTimeMillis();
                                        final String hash = meta.getMd5Hash();
                                        Log.d(TAG, "path " + path);
                                        Log.d(TAG, "created " + created);
                                        Log.d(TAG, "updated " + updated);
                                        Log.d(TAG, "hash " + hash);
                                        userPicturesRef.child("path").setValue(path);
                                        userPicturesRef.child("created").setValue(created);
                                        userPicturesRef.child("updated").setValue(updated);
                                        userPicturesRef.child("hash").setValue(hash);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    // ????????? ??????
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Toast.makeText(getApplicationContext(), "????????? ??????", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                    @SuppressWarnings("VisibleForTests")
                                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                        double progress = (100 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                                        progressDialog.setMessage("Uploaded " +
                    
                                            ((int) progress) + "%...");
                                    }
                                });
                        }
                    }
    
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
        
                    }
                }
            );
        } else {
            // ?????? ?????? ?????? ?????? ?????? ?????? ???
            Toast.makeText(getApplicationContext(), "????????? ?????? ???????????????.", Toast.LENGTH_SHORT).show();
        }
    }


    // ????????? ????????? ?????? ???????????? ????????? ??????
    private int exifOrientationToDegress(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    // ?????? ????????? ???????????? ????????? ???????????? ???????????? ??????
    private Bitmap rotate(Bitmap bitmap, float degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //check the permission
        
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            iv.setImageBitmap(imageBitmap);
        }
        
        //take pic
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            image();
        }

        //image choose
        if (requestCode == 0 && resultCode == RESULT_OK) {
            filepath = data.getData();
            Log.d(TAG, "uri:" + String.valueOf(filepath));
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filepath);
                iv.setImageBitmap(bitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void image() {
        Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath);
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(imageFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    
        int exifOrientation;
        int exifDegree;
    
        if (exif != null) {
            exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            exifDegree = exifOrientationToDegress(exifOrientation);
        } else {
            exifDegree = 0;
        }
    
        String result = "";
    
        String filename;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HHmmss", Locale.getDefault());
        Date curDate = new Date(System.currentTimeMillis());
        String num = format.format(curDate);
    
        if (editText.length() == 0) {
            filename = "User" + num;
        } else {
            filename = editText.getText().toString();
        }
    
        String strFolderName = Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES) + File.separator + "LockLock" + File.separator;
    
        String pathString = strFolderName + "/" + filename + ".jpg";
        result = pathString;
    
        Path path = Paths.get(pathString);
        
        try {
            Files.createDirectories(path);
            Files.createFile(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            result = "ve Error fOut";
        } catch (IOException e) {
            e.printStackTrace();
            result = "cannot create a file";
        }
        
        try (FileOutputStream fout = new FileOutputStream(path.toFile())) {
            // ????????? ?????? ?????? ????????? ??????
            rotate(bitmap, exifDegree).compress(Bitmap.CompressFormat.JPEG, 70, fout);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    
        // ?????? ????????? ????????? ????????? ?????? ?????? ??? ?????????
        mMediaScanner.mediaScanning(strFolderName + "/" + filename + ".jpg");
       
        iv.setImageBitmap(rotate(bitmap, exifDegree));
    }
}