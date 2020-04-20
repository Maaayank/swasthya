package com.blackcat.health_0_meter.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.blackcat.health_0_meter.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private LinearLayout login ;
    private static final int RC_SIGN_IN = 234;
    private String TAG = "LoginError";
    GoogleSignInClient mGoogleSignInClient;
    FirebaseAuth mAuth;
    DatabaseReference dataRef;
    FirebaseDatabase mdb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth =  FirebaseAuth.getInstance();
        login = findViewById(R.id.userLogin);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("916860739820-ekkpt1entqb6s2o19f0itldo7hah1f9m.apps.googleusercontent.com")
                .requestEmail()
                .requestProfile()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mdb = FirebaseDatabase.getInstance();
        dataRef = mdb.getReference();

        dataRef.child("xyz").setValue("henlo").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.d("healtherror","successfull");
                }else if( task.isCanceled()){
                    Log.d("healtherror","canceled");
                }
            }
        });


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //if the requestCode is the Google Sign In code that we defined at starting
        if (requestCode == RC_SIGN_IN) {

            //Getting the GoogleSignIn Task
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                //Google Sign In was successful, authenticate with Firebase
                final GoogleSignInAccount account = task.getResult(ApiException.class);
                //authenticating with firebase
                firebaseAuthWithGoogle(account);

            } catch (ApiException e) {
                mGoogleSignInClient.signOut();
                Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        //getting the auth credential
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        //Now using firebase we are signing in the user here
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");

                            final FirebaseUser user = mAuth.getCurrentUser();
//                            dataRef.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
//                                @Override
//                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                    try{
//                                        if(!dataSnapshot.child(mAuth.getUid()).exists()){
//                                            addData(user.getUid(),user.getEmail(),user.getDisplayName());
//                                        }
//                                    }catch (Exception e){
//
//                                    }
//                                }
//
//                                @Override
//                                public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                                }
//                            });

                            addData(user.getUid(),user.getEmail(),user.getDisplayName());

                            startActivity(new Intent(LoginActivity.this,HomeScreenActivity.class));
                            Toast.makeText(LoginActivity.this, "User Signed In", Toast.LENGTH_SHORT).show();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.d(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            mGoogleSignInClient.signOut();
                        }

                    }
                });
    }

    private void addData(String uid , String email , String name){

        try{

            Log.d("healtherror",uid + " " + email + " " + name);
            Map<String,Object> upload = new HashMap<>();
            upload.put("email",email);
            upload.put("name",name);
            dataRef.child("users").child(uid).child("profile").updateChildren(upload).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("healtherror","adding failed : " + e.getMessage());
                }
            });
            SharedPreferences.Editor prefs = this.getSharedPreferences("user",MODE_PRIVATE).edit();
            prefs.putString("address","users/" + uid );
            prefs.putString("email",email);
            prefs.putString("name",name);
            prefs.apply();
        }catch (Exception e){
            Log.d("helthexception",e.getMessage());
        }
    }
}
