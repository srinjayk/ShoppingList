package com.google.firebase.example.fireeats;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.example.fireeats.model.Rating;
import com.google.firebase.example.fireeats.model.Restaurant;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class AddEditActivity extends AppCompatActivity implements
        View.OnClickListener{

    private MaterialRatingBar mPricing;
    private EditText mPlace;
    private EditText mCategory;
    private EditText mName;
    private MaterialRatingBar mRating;
    private TextView texttxt;

//    private String item_id_id = getIntent().getStringExtra("item_id_id");
//
//    Bundle extras = getIntent().getExtras();
//        String value = extras.getString("item_id_id");
//        //The key argument here must match that used in the other activity

    private FirebaseFirestore mFirestore;
    private DocumentReference mRestaurantRef;

    public static final String KEY_RESTAURANT_ID = "key_restaurant_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);
        mPricing = findViewById(R.id.restaurant_pricing_new);
        mRating = findViewById(R.id.restaurant_rating_new);
        mCategory = findViewById(R.id.category_of_restaurant_new);
        mName = findViewById(R.id.name_of_restaurant_new);
        texttxt = findViewById(R.id.id_of_restaurant_new);

        findViewById(R.id.add_restaurant_back_button_edit).setOnClickListener(this);
        findViewById(R.id.add_restaurant_cancel_button_edit).setOnClickListener(this);
        findViewById(R.id.add_restaurant_button_edit).setOnClickListener(this);

        // Initialize Firestore
        mFirestore = FirebaseFirestore.getInstance();

        // Get reference to the restaurant
//        mRestaurantRef = mFirestore.collection("restaurants" + "." + FirebaseAuth.getInstance().getCurrentUser().getEmail());

        Intent intent = getIntent();
        String str_1 = intent.getStringExtra("message");
        texttxt.setText(str_1);
        String str_2 = intent.getStringExtra("name");
        mName.setText(str_2);
        String str_3 = intent.getStringExtra("category");
        mCategory.setText(str_3);

        System.out.println(texttxt.getText().toString());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_restaurant_back_button_edit:
                onBackArrowClicked(view);
                break;
            case R.id.add_restaurant_cancel_button_edit:
                onBackArrowClicked(view);
                break;
            case R.id.add_restaurant_button_edit:
                EditElementClicked(view);
                break;
        }
    }

    public void onBackArrowClicked(View view) {
        onBackPressed();
    }

    public void EditElementClicked(View view) {
//        CollectionReference restaurants_1 = mFirestore.collection("restaurants" + "."+FirebaseAuth.getInstance().getCurrentUser().getEmail());
        System.out.println(texttxt.getText().toString());
        DocumentReference itemRef = mFirestore.collection("restaurants" + "."+FirebaseAuth.getInstance().getCurrentUser().getEmail()).document(texttxt.getText().toString());

        // Set the "isCapital" field of the city 'DC'
        itemRef
                .update("name", mName.getText().toString(),
                        "category", mCategory.getText().toString(),
                        "avgRating",mRating.getRating(),
                        "price",mPricing.getRating())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        System.out.println("DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println("Error updating document");
                    }
                });
        onBackArrowClicked(view);
    }

}
