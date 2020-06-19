/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.firebase.example.fireeats;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.example.fireeats.model.Restaurant;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.File;


public class AddRestaurantActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int LIMIT = 50;

    private Bitmap myBitmap;
    private ImageView myImageView;
    private TextView myTextView;
    public static final int WRITE_STORAGE = 100;
    public static final int SELECT_PHOTO = 102;
    public File photo;
    private FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();;
    private Query mQuery;

//
//        // Get reference to the restaurant
//        mRestaurantRef = mFirestore.collection("restaurants").document(restaurantId);
//
//        // Get ratings
//        Query ratingsQuery = mRestaurantRef
//                .collection("ratings")
//                .orderBy("timestamp", Query.Direction.DESCENDING)
//                .limit(50);

//    public static final String KEY_RESTAURANT_ID = "key_restaurant_id";
//    private static final String CHANNEL_ID = "10001";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_restaurant);

        myTextView = findViewById(R.id.textView);
        myImageView = findViewById(R.id.imageView);
        findViewById(R.id.checkText).setOnClickListener(this);
        findViewById(R.id.select_image).setOnClickListener(this);
        findViewById(R.id.restaurant_button_back).setOnClickListener(this);
        findViewById(R.id.adddataOCR).setOnClickListener(this);


    }

    private void initFirestore() {
        mFirestore = FirebaseFirestore.getInstance();

        // Get the 50 highest rated restaurants
        mQuery = mFirestore.collection("restaurants" + "." + FirebaseAuth.getInstance().getCurrentUser().getEmail())
                .orderBy("avgRating", Query.Direction.DESCENDING)
                .limit(LIMIT);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.checkText:
                if (myBitmap != null) {
                    runTextRecognition();
                }
                break;
            case R.id.select_image:
                checkPermission(WRITE_STORAGE);
                break;
            case R.id.restaurant_button_back:
                onBackArrowClicked(view);
                break;
            case R.id.adddataOCR:
                onAddOCRRating(view);
                break;
        }
    }

    public void onBackArrowClicked(View view) {
        onBackPressed();
    }

    public void onAddOCRRating(View view) {
//        Restaurant restaurant = new Restaurant(myTextView.getText().toString(),
//                FirebaseAuth.getInstance().getCurrentUser().getEmail(),
//                "OCR",
//                (int) 1,
//                1,
//                (float)1);
//        Log.d(myTextView.getText().toString(),"handwriting");

        int count = 1;
        String items = myTextView.getText().toString();
//        System.out.println(items);

        String splitted[] =items.split("\n");
        System.out.println(splitted.length);
        for(int i=0;i<splitted.length;i++){
            Restaurant restaurant = new Restaurant(splitted[i].substring(2),
                    FirebaseAuth.getInstance().getCurrentUser().getEmail(),
                    "OCR",
                    (int) 1,
                    1,
                    (float)1);
            CollectionReference restaurants = mFirestore.collection("restaurants" + "." + FirebaseAuth.getInstance().getCurrentUser().getEmail());
            restaurants.add(restaurant);
            System.out.println(splitted[i].substring(2));
        }

//        String item_insert = "";
//        int length = items.length();

//        for (int i=0;i<length;i++){
//            if(items.charAt(i)==toString(count)){
//
//            }
//        }

//        for (String s: items.split("(?<=[0-9])(?=[a-zA-Z])"))
//            if(s.length() > 3){
//                System.out.println("++");
//                System.out.println(s.charAt(0));
//                System.out.println(s);
//                System.out.println("++");
//            }


//        CollectionReference restaurants = mFirestore.collection("restaurants");
//        restaurants.add(restaurant);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case WRITE_STORAGE:
                    checkPermission(requestCode);
                    break;
                case SELECT_PHOTO:
                    Uri dataUri = data.getData();
                    String path = CommonUtils.getPath(this, dataUri);
                    if (path == null) {
                        myBitmap = CommonUtils.resizePhoto(photo, this, dataUri, myImageView);
                    } else {
                        myBitmap = CommonUtils.resizePhoto(photo, path, myImageView);
                    }
                    if (myBitmap != null) {
                        myTextView.setText(null);
                        myImageView.setImageBitmap(myBitmap);
                    }
                    break;

            }
        }
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
                Toast.makeText(AddRestaurantActivity.this,
                        "Exception", Toast.LENGTH_LONG).show();
            }
        });
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case WRITE_STORAGE:

                //If the permission request is granted, then...//
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //...call selectPicture//
                    selectPicture();
                    //If the permission request is denied, then...//
                } else {
                    //...display the “permission_request” string//
                    requestPermission(this, requestCode, R.string.permission_request);
                }
                break;

        }
    }

    //Display the permission request dialog//
    public static void requestPermission(final Activity activity, final int requestCode, int msg) {
        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setMessage(msg);
        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                Intent permissonIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                permissonIntent.setData(Uri.parse("package:" + activity.getPackageName()));
                activity.startActivityForResult(permissonIntent, requestCode);
            }
        });
        alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alert.setCancelable(false);
        alert.show();
    }

    //Check whether the user has granted the WRITE_STORAGE permission//
    public void checkPermission(int requestCode) {
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
