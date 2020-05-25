package com.blackcat.health_0_meter.Fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.blackcat.health_0_meter.Activities.SplashScreenActivity;
import com.blackcat.health_0_meter.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import static android.content.Context.MODE_PRIVATE;


public class ProfileFragment extends Fragment {

    Button logout;
    TextView email, name;
    ImageView profile;
    GoogleSignInClient mGoogleSignInClient;
    FirebaseAuth mAuth;
    FirebaseDatabase mdb;
    SharedPreferences user ;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        mdb = FirebaseDatabase.getInstance();
        user = getActivity().getSharedPreferences("user",MODE_PRIVATE);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("916860739820-ekkpt1entqb6s2o19f0itldo7hah1f9m.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        logout = view.findViewById(R.id.logout);
        email = view.findViewById(R.id.email);
        name = view.findViewById(R.id.name);
        profile = view.findViewById(R.id.profile);

        if(mAuth.getCurrentUser().getPhotoUrl() != null && !mAuth.getCurrentUser().getPhotoUrl().toString().isEmpty())
            Glide.with(getActivity()).asBitmap().load(mAuth.getCurrentUser().getPhotoUrl()).circleCrop().into(profile);

        email.setText(mAuth.getCurrentUser().getEmail());
        name.setText(mAuth.getCurrentUser().getDisplayName());

        //todo average steps fetched here usko display karna hai
        float averageSteps = user.getFloat("averageSteps",0);


        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGoogleSignInClient.signOut();
                mAuth.signOut();
                getActivity().finish();
                startActivity(new Intent(getActivity(), SplashScreenActivity.class));
            }
        });
    }
}
