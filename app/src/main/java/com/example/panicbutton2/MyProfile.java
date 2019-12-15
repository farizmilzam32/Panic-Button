package com.example.panicbutton2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MyProfile extends AppCompatActivity {
    DatabaseReference reference;
    DatabaseReference uidRef;




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_profile);
        setSignOutBtn();


        final TextView name = findViewById(R.id.editName);
        final TextView email = findViewById(R.id.editEmail);
        final TextView telp = findViewById(R.id.textTelp);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        reference = FirebaseDatabase.getInstance().getReference();
        uidRef = reference.child("Users").child(uid);



        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String propNameTxt = dataSnapshot.child("name").getValue(String.class);
                String emailNameTxt = dataSnapshot.child("email").getValue(String.class);
                String phonelineTxt = dataSnapshot.child("phone").getValue(String.class);

                name.setText(propNameTxt);
                email.setText(emailNameTxt);
                telp.setText(phonelineTxt);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        uidRef.addListenerForSingleValueEvent(eventListener);



    }

    public void setSignOutBtn() {
        Button signOutBtn = findViewById(R.id.buttonSignOut);
        signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                finish();
                startActivity(new Intent(MyProfile.this, LoginActivity.class));

            }
        });
    }
}







