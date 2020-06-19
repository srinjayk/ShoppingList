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
 package com.google.firebase.example.fireeats.adapter;

import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.example.fireeats.R;
import com.google.firebase.example.fireeats.model.Restaurant;
import com.google.firebase.example.fireeats.util.RestaurantUtil;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;

/**
 * RecyclerView adapter for a list of Restaurants.
 */
public class RestaurantAdapter extends FirestoreAdapter<RestaurantAdapter.ViewHolder> {



    public interface OnRestaurantSelectedListener {

        void addRestaurantClicked_V_2();

        void onRestaurantSelected(DocumentSnapshot restaurant);

    }

    private OnRestaurantSelectedListener mListener;

    public RestaurantAdapter(Query query, OnRestaurantSelectedListener listener) {
        super(query);
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.item_restaurant, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getSnapshot(position), mListener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView nameView;
        MaterialRatingBar ratingBar;
        TextView numRatingsView;
        TextView numRatingsView_1;
        TextView priceView;
        TextView categoryView;
        TextView cityView;
        TextView categoryView_1;
        Button deleteButton;
        TextView phototext;
        TextView item_id;

        private FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();

        public ViewHolder(View itemView) {
            super(itemView);
//            imageView = itemView.findViewById(R.id.restaurant_item_image);
            nameView = itemView.findViewById(R.id.restaurant_item_name);
            ratingBar = itemView.findViewById(R.id.restaurant_item_rating);
            numRatingsView = itemView.findViewById(R.id.num_of_items);
            numRatingsView_1 = itemView.findViewById(R.id.restaurant_item_category_1);
            priceView = itemView.findViewById(R.id.restaurant_item_price);
            categoryView = itemView.findViewById(R.id.restaurant_item_category);
            cityView = itemView.findViewById(R.id.restaurant_item_city);
            deleteButton = itemView.findViewById(R.id.delete_item);
            phototext = itemView.findViewById(R.id.restaurant_item_name_1);
//            deleteButton.setOnClickListener(new View.OnClickListener()
//            {
//                @Override
//                public void onClick(View v)
//                {
//                    CollectionReference restaurants = mFirestore.collection("restaurants");
//                    mFirestore.collection("states").document("UP")
//                            .delete();
//                }
//            });
        }

        public void bind(final DocumentSnapshot snapshot,
                         final OnRestaurantSelectedListener listener) {

            final Restaurant restaurant = snapshot.toObject(Restaurant.class);
            Resources resources = itemView.getResources();


                nameView.setText(restaurant.getName());
                ratingBar.setRating((float) restaurant.getAvgRating());
                cityView.setText(restaurant.getCity());
                categoryView.setText(restaurant.getCategory());
                numRatingsView.setText(resources.getString(R.string.fmt_num_ratings,
                        restaurant.getNumRatings()));


                phototext.setText(restaurant.getPhoto());

                numRatingsView_1.setText(restaurant.getCategory());

                priceView.setText(RestaurantUtil.getPriceString(restaurant));

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (listener != null) {
                            listener.onRestaurantSelected(snapshot);
                        }
                    }
                });

            deleteButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    System.out.println("Delete button clicked");
                    System.out.println();
                    CollectionReference restaurants = mFirestore.collection("restaurants");
//                    mFirestore.collection("restaurants").document(phototext.getText().toString()).delete()
//                            .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                @Override
//                                public void onSuccess(Void aVoid) {
//                                    System.out.println("Data deleted successfully");
//                                }
//                            })
//                            .addOnFailureListener(new OnFailureListener() {
//                                @Override
//                                public void onFailure(@NonNull Exception e) {
//                                    System.out.println("Error while deleting the data : " + e.getMessage());
//                                }
//                            });
                    mFirestore.collection("restaurants" + "." + FirebaseAuth.getInstance().getCurrentUser().getEmail()).document(phototext.getText().toString()).delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    System.out.println("Data deleted successfully");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    System.out.println("Error while deleting the data : " + e.getMessage());
                                }
                            });
                }
            });

        }

    }
}
