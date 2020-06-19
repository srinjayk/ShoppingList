package com.google.firebase.example.fireeats;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.example.fireeats.model.Restaurant;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AddCameraActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST = 1888;
    private Bitmap myBitmap;
    public File photo;
    String ImagePath;
    Uri URI;
    private ImageView imageView;
    public static final int WRITE_STORAGE = 100;
    public static final int SELECT_PHOTO = 102;
    private TextView myTextView;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();

    String currentPhotoPath;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private Bitmap mImageBitmap;
    private String mCurrentPhotoPath;
    private ImageView mImageView;

    private String imagePath = "/files/Camera/test.jpg";
    private File originalFile  = new File(imagePath);

    private static final int REQUEST_CAMERA = 100;


    static final int REQUEST_TAKE_PHOTO = 1;


    int num = 1;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_camera);
        this.myTextView = this.findViewById(R.id.textView_1);
//        findViewById(R.id.checkText).setOnClickListener(this);
//        findViewById(R.id.select_image).setOnClickListener(this);
//        findViewById(R.id.restaurant_button_back).setOnClickListener(this);
//        findViewById(R.id.adddataOCR).setOnClickListener(this);
        this.imageView = (ImageView)this.findViewById(R.id.imageView1);
        Button photoButton = (Button) this.findViewById(R.id.button1);
        photoButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                }
                else
                {
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    Uri outputFileUri = Uri.fromFile(originalFile);
                    cameraIntent.putExtra("filepath", outputFileUri);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                }
//                dispatchTakePictureIntent();
            }
        });

        Button photoButton_1 = (Button) this.findViewById(R.id.checkText_1);
        photoButton_1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (myBitmap != null) {
                    runTextRecognition();
                }
            }
        });

        Button photoButton_2 = (Button) this.findViewById(R.id.adddataOCR_1);
        photoButton_2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (myBitmap != null) {
                    onAddOCRRating(v);
                }
            }
        });

        Button photoButton_3 = (Button) this.findViewById(R.id.add_data_from_storage);
        photoButton_3.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                checkPermission_storage(WRITE_STORAGE);
            }
        });

        ImageView photoButton_4 = this.findViewById(R.id.restaurant_button_back_1);
        photoButton_4.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                onBackArrowClicked(v);
            }
        });
    }

    public void onBackArrowClicked(View view) {
        onBackPressed();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                System.out.println("Error");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.google.firebase.example.fireeats",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void runTextRecognition() {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(myBitmap);
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getCloudTextRecognizer();
        detector.processImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText texts) {
                processExtractedText(texts);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure
                    (@NonNull Exception exception) {
                Toast.makeText(AddCameraActivity.this,
                        "Exception", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void onAddOCRRating(View view) {

        int count = 1;
        String items = myTextView.getText().toString();
//        System.out.println(items);

        String splitted[] =items.split("\n");

        System.out.println(splitted.length);
        for(int i=0;i<splitted.length;i++){
            Long tsLong = System.currentTimeMillis();
            String ts = tsLong.toString();
            String listItem[] = splitted[i].split("-");
            if (listItem.length == 1){
                Restaurant restaurant = new Restaurant(listItem[0],
                        FirebaseAuth.getInstance().getCurrentUser().getEmail(),
                        "",
                        ts,
                        (int) 1,
                        1,
                        (float)1);
                CollectionReference restaurants_1 = mFirestore.collection("restaurants" + "."+FirebaseAuth.getInstance().getCurrentUser().getEmail());
//            restaurants.add(restaurant);
                restaurants_1.document(ts).set(restaurant);
            }
            else{
                Restaurant restaurant = new Restaurant(listItem[0],
                        FirebaseAuth.getInstance().getCurrentUser().getEmail(),
                        listItem[1],
                        ts,
                        (int) 1,
                        1,
                        (float)1);
                CollectionReference restaurants_1 = mFirestore.collection("restaurants" + "."+FirebaseAuth.getInstance().getCurrentUser().getEmail());
//            restaurants.add(restaurant);
                restaurants_1.document(ts).set(restaurant);
            }

//            CollectionReference restaurants = mFirestore.collection("restaurants");
////            restaurants.add(restaurant);
//            restaurants.document(ts).set(restaurant);

//            CollectionReference restaurants_1 = mFirestore.collection("restaurants" + "."+FirebaseAuth.getInstance().getCurrentUser().getEmail());
////            restaurants.add(restaurant);
//            restaurants_1.document(ts).set(restaurant);

            System.out.println(splitted[i]);
        }
    }

    private void processExtractedText(FirebaseVisionText firebaseVisionText) {
        myTextView.setText(null);
        if (firebaseVisionText.getTextBlocks().size() == 0) {
            myTextView.setText(R.string.no_text);
            return;
        }
        for (FirebaseVisionText.TextBlock block : firebaseVisionText.getTextBlocks()) {
            myTextView.append(block.getText());

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
            else
            {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap photo = (Bitmap) extras.get("data");
            myBitmap = photo;
            imageView.setImageBitmap(myBitmap);
            Intent intent = getIntent();
            System.out.println(intent.getStringExtra("filepath"));
//            rotateImage(imageView);
            imageView.setRotation(90);
        }

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case WRITE_STORAGE:
                    checkPermission_storage(requestCode);
                    break;
                case SELECT_PHOTO:
                    Uri dataUri = data.getData();
                    String path = CommonUtils.getPath(this, dataUri);
                    if (path == null) {
                        myBitmap = CommonUtils.resizePhoto(photo, this, dataUri, imageView);
                    } else {
                        myBitmap = CommonUtils.resizePhoto(photo, path, imageView);
                    }
                    if (myBitmap != null) {
                        myTextView.setText(null);
                        imageView.setImageBitmap(myBitmap);
                        imageView.setRotation(90);
                    }
                    break;

            }
        }
    }

    //Check whether the user has granted the WRITE_STORAGE permission//
    public void checkPermission_storage(int requestCode) {
        switch (requestCode) {
            case WRITE_STORAGE:
                int hasWriteExternalStoragePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

                //If we have access to external storage...//
                if (hasWriteExternalStoragePermission == PackageManager.PERMISSION_GRANTED) {
                    //...call selectPicture, which launches an Activity where the user can select an image//
                    selectPicture();
                    //If permission hasn’t been granted, then...//
                } else {
                    //...request the permission//
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestCode);
                }
                break;

        }
    }

    private void selectPicture() {
        photo = CommonUtils.createTempFile(photo);
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        //Start an Activity where the user can choose an image//
        startActivityForResult(intent, SELECT_PHOTO);
    }


}
