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

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.example.fireeats.RestaurantDetailActivity;

import java.io.IOException;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.example.fireeats.model.Rating;
import com.google.firebase.example.fireeats.model.Restaurant;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

/**
 * Dialog Fragment containing rating form.
 */
public class RestaurantDialogFragment extends DialogFragment implements View.OnClickListener {

    public static final String TAG = "RestaurantDialog";
    private static final String CHANNEL_ID = "10001";

    private MaterialRatingBar mPricing;
    private EditText mPlace;
    private EditText mCategory;
    private EditText mName;
    private MaterialRatingBar mRating;
    private int num = 1;

    private static final int LIMIT = 50;

    private FirebaseFirestore mFirestore;
    private Query mQuery;

//    private FirebaseFirestore mFirestore;
//
//    mFirestore = FirebaseFirestore.getInstance();

    interface RestaurantListener {

        void onRestaurant(Restaurant restaurant);

    }

    private RestaurantListener mRestaurantListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_restaurant, container, false);
        mPricing = v.findViewById(R.id.restaurant_pricing);
//        mPlace = v.findViewById(R.id.place_of_restaurant);
        mCategory = v.findViewById(R.id.category_of_restaurant);
        mName = v.findViewById(R.id.name_of_restaurant);
        mRating = v.findViewById(R.id.restaurant_rating);

        v.findViewById(R.id.add_restaurant_cancel_button).setOnClickListener(this);
        v.findViewById(R.id.add_restaurant_button).setOnClickListener(this);

        initFirestore();

        return v;
    }

//    @Override
//    public <mRestaurantListener> void onAttach(Context context) {
//        super.onAttach(context);
//
//        if (context instanceof RestaurantListener) {
//            mRestaurantListener = (mRestaurantListener) context;
//        }
//    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

    }

    private void initFirestore() {
        mFirestore = FirebaseFirestore.getInstance();

        // Get the 50 highest rated restaurants
        mQuery = mFirestore.collection("restaurants" + "." + FirebaseAuth.getInstance().getCurrentUser().getEmail())
                .orderBy("avgRating", Query.Direction.DESCENDING)
                .limit(LIMIT);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_restaurant_button:
                onSubmitClicked(v);
                break;
            case R.id.add_restaurant_cancel_button:
                onCancelClicked(v);
                break;
        }
    }

    public void onSubmitClicked(View view) {
        Long tsLong = System.currentTimeMillis();
        String ts = tsLong.toString();
        Restaurant restaurant = new Restaurant(mName.getText().toString(),
                                                FirebaseAuth.getInstance().getCurrentUser().getEmail(),
                                                mCategory.getText().toString(),
                                                ts,
                                                (int) mPricing.getRating(),
                                                num,
                                                (float)mRating.getRating());

        CollectionReference restaurants = mFirestore.collection("restaurants" + "." + FirebaseAuth.getInstance().getCurrentUser().getEmail());
//        restaurants.add(restaurant);
        restaurants.document(ts).set(restaurant);
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
//                .setSmallIcon(R.drawable.notification_icon)
//                .setContentTitle("My notification")
//                .setContentText("Much longer text that cannot fit one line...")
//                .setStyle(new NotificationCompat.BigTextStyle()
//                        .bigText("Much longer text that cannot fit one line..."))
//                .setPriority(NotificationCompat.PRIORITY_DEFAULT);


        if (mRestaurantListener != null) {
            mRestaurantListener.onRestaurant(restaurant);
        }

        dismiss();
    }


    public void onCancelClicked(View view) {
        dismiss();
    }
}