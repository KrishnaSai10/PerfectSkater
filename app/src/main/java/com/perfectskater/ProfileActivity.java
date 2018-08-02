package com.perfectskater;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {

    private static final int CONTENT_REQUEST = 1337;
    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    private final int PERMISSION_FOR_CAMERA = 121;
    private final int PERMISSION_FOR_STORAGE = 122;
    private File output = null;
    ImageView profile;
    EditText fname, lname;
    SharedPreferences preferences;
    public static String PROFILE_PREFF = "profilepreff";
    String imageDecode = "";
    private String userChoosenTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pro);
        getSupportActionBar().setTitle("Profile");
        profile = findViewById(R.id.profilePicture);
        fname = findViewById(R.id.firstName);
        lname = findViewById(R.id.lastName);
        preferences = getSharedPreferences(PROFILE_PREFF, MODE_PRIVATE);

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }

        });
        fname.setText(preferences.getString("firstName", ""));
        lname.setText(preferences.getString("lastName", ""));
        if (preferences.getString("profilePic", "").equals("")) {
        } else {
            profile.setImageBitmap(decodeBase64(preferences.getString("profilePic", "")));
        }

        Button save = findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("firstName", fname.getText().toString());
                editor.putString("lastName", lname.getText().toString());
                editor.putString("profilePic", imageDecode);
                editor.commit();
                DatabaseHelper dop = new DatabaseHelper(ProfileActivity.this);
                int count = dop.getCount(DatabaseHelper.PRO_TABLE);
                if (count == 0) {
                    dop.putProfile(dop, fname.getText().toString(), lname.getText().toString());
                }
                startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                finish();

            }
        });
    }

    private void selectImage() {
        final CharSequence[] items = {"Take Photo", "Choose from Library",
                "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
//                boolean result = Utility.checkPermission(ProfileActivity.this);
                if (items[item].equals("Take Photo")) {
                    userChoosenTask = "Take Photo";
                    checkAndRequestPermissions();
                } else if (items[item].equals("Choose from Library")) {
                    userChoosenTask = "Choose from Library";
                    checkAndRequestPermissions();
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void checkAndRequestPermissions() {
        ArrayList<String> neededPermissions = new ArrayList<>();
        if (RuntimePermissionUtils.checkPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            neededPermissions.add(Manifest.permission.CAMERA);
        }
        if (RuntimePermissionUtils.checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            neededPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (neededPermissions.size() > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(neededPermissions.toArray(new String[neededPermissions.size()]), PERMISSION_FOR_CAMERA);
            }
        } else {
            if (userChoosenTask.equalsIgnoreCase("Take Photo")) {
                cameraIntent();
            } else if (userChoosenTask.equalsIgnoreCase("Choose from Library")) {
                galleryIntent();
            }
        }
    }

    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_FOR_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (userChoosenTask.equalsIgnoreCase("Take Photo")) {
                        cameraIntent();
                    } else if (userChoosenTask.equalsIgnoreCase("Choose from Library")) {
                        galleryIntent();
                    }
                } else {
                    //code for deny
                }
                break;
            case PERMISSION_FOR_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (userChoosenTask.equalsIgnoreCase("Take Photo")) {
                        cameraIntent();
                    } else if (userChoosenTask.equalsIgnoreCase("Choose from Library")) {
                        galleryIntent();
                    }
                } else {
                    //code for deny
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
            if (requestCode == CONTENT_REQUEST) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                profile.setImageBitmap(imageBitmap);
                imageDecode = encodeTobase64(imageBitmap);

            }
        }
    }

    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");

        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        profile.setImageBitmap(thumbnail);
        imageDecode = encodeTobase64(thumbnail);
    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {
        Bitmap bm = null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        profile.setImageBitmap(bm);
        imageDecode = encodeTobase64(bm);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ProfileActivity.this, MainActivity.class));
        finish();
    }

    public static String encodeTobase64(Bitmap image) {
        Bitmap immage = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immage.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

        Log.d("Image Log:", imageEncoded);
        return imageEncoded;
    }

    public static Bitmap decodeBase64(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory
                .decodeByteArray(decodedByte, 0, decodedByte.length);
    }

}
