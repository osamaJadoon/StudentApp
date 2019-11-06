package com.example.studentapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import es.dmoral.toasty.Toasty;
import maes.tech.intentanim.CustomIntent;

public class LoginActivityS extends AppCompatActivity {

    private EditText emailInput;
    private EditText passwordInput;
    private CheckBox showHidePassword;
    private TextView forgotPassword;
    private Button loginBtn;

    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;

    private Animation mShakeAnimation;
    private LinearLayout linearLayout;

    StudentData studentData;
    String studentStatus = "Student";

    DatabaseReference databaseReference;
    FirebaseUser user;
    String uid;
    FirebaseDatabase firebaseDatabase;
    private int LOCATION_PERMISSION_CODE = 1;



    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This Permission needed otherwise you will be not able to use this app...")
                    .setIcon(R.drawable.warning)
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            loadingBar.dismiss();
                            ActivityCompat.requestPermissions(LoginActivityS.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            loadingBar.dismiss();
                        }
                    })
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toasty.success(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivityS.this, BusNumberActivity.class);
                startActivity(intent);
                CustomIntent.customType(LoginActivityS.this,"left-to-right");
                loadingBar.dismiss();
            } else {
                Toasty.error(this, "Permission Denied", Toast.LENGTH_LONG).show();
                loadingBar.dismiss();
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logins);

        mShakeAnimation = AnimationUtils.loadAnimation(this,R.anim.shake);
        linearLayout = findViewById(R.id.linear_layout_id);

        loadingBar = new ProgressDialog(this);

        emailInput = findViewById(R.id.email_login_id);
        passwordInput = findViewById(R.id.password_login_id);
        showHidePassword = findViewById(R.id.login_show_hide_pass_id);
        showHidePassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)
                {
                    showHidePassword.setText("Hide Password");
                    passwordInput.setInputType(InputType.TYPE_CLASS_TEXT);
                    showHidePassword.setInputType(InputType.TYPE_CLASS_TEXT);
                    passwordInput.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
                else
                {
                    showHidePassword.setText("Show Password");
                    passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    passwordInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });
        forgotPassword = findViewById(R.id.forgot_password);
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivityS.this, ForgotPasswordActivityS.class);
                startActivity(intent);
                CustomIntent.customType(LoginActivityS.this,"left-to-right");
            }
        });
        loginBtn = findViewById(R.id.login_btn_id);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginFunction();
            }
        });


        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

//        user = mAuth.getCurrentUser();
//        assert user != null;
//        uid = user.getUid();
        studentData = new StudentData();

        databaseReference = FirebaseDatabase.getInstance().getReference();

    }



    private boolean validateEmail(){
        String Email = emailInput.getText().toString().trim();
        if (Email.isEmpty())
        {
            Toasty.info(LoginActivityS.this,"Field can't be empty",Toast.LENGTH_LONG).show();
            return false;
        }
        else
        {
            return true;
        }
    }

    private boolean validatePassword(){
        String Password = passwordInput.getText().toString().trim();
        if (Password.isEmpty())
        {
            Toasty.info(LoginActivityS.this,"Field can't be empty",Toast.LENGTH_LONG).show();
            return false;
        }

        else
        {
            return true;
        }
    }

    private void LoginFunction() {
        if (!isOnline())
        {
            linearLayout.startAnimation(mShakeAnimation);
            return;
        }
        if (!gpsEnabled())
        {
            linearLayout.startAnimation(mShakeAnimation);
            return;
        }
        if (!validateEmail() | !validatePassword())
        {
            linearLayout.startAnimation(mShakeAnimation);
            return;
        }

        loadingBar.setTitle("Login Student");
        loadingBar.setMessage("Please wait!");
        loadingBar.show();
        loadingBar.setCanceledOnTouchOutside(false);
        loginBtn.setEnabled(false);

        final String Email = emailInput.getText().toString().trim();
        String Password = passwordInput.getText().toString().trim();

        mAuth.signInWithEmailAndPassword(Email,Password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull final Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                if (ContextCompat.checkSelfPermission(LoginActivityS.this,
                                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                                    Intent intent = new Intent(LoginActivityS.this, BusNumberActivity.class);
                                    startActivity(intent);
                                    CustomIntent.customType(LoginActivityS.this,"left-to-right");
                                } else {
                                    requestLocationPermission();
                                }
                            }

//                            databaseReference = FirebaseDatabase.getInstance().getReference().child("StudentData");
//                            databaseReference.addValueEventListener(new ValueEventListener() {
//                                @Override
//                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//
//                                    Iterable<DataSnapshot> children = dataSnapshot.getChildren();
//                                    for (DataSnapshot child: children){
//                                        final StudentData   studentInfo = child.getValue(studentData.getClass());
//                                        assert studentInfo != null;
//                                        String status = studentInfo.getStatus();
//
//                                        if (studentStatus.equals(status))
//                                        {
//                                            loadingBar.dismiss();
//                                            Toasty.success(LoginActivityS.this,"Login Successful",Toast.LENGTH_LONG).show();
//                                            Intent intent = new Intent(LoginActivityS.this,BusNumberActivity.class);
//                                            startActivity(intent);
//                                            CustomIntent.customType(LoginActivityS.this,"left-to-right");
//                                        }
//                                        else {
//                                            Toasty.error(LoginActivityS.this,"Incorrect Email or Password",Toast.LENGTH_LONG).show();
//                                            loadingBar.dismiss();
//                                            linearLayout.startAnimation(mShakeAnimation);
//                                        }
//                                    }
//                                }
//
//                                @Override
//                                public void onCancelled(@NonNull DatabaseError databaseError) {
//                                    Toasty.error(LoginActivityS.this,"database error",Toast.LENGTH_LONG).show();
//                                    loadingBar.dismiss();
//                                    linearLayout.startAnimation(mShakeAnimation);
//
//                                }
//                            });

                        }
                        else
                        {
                            linearLayout.startAnimation(mShakeAnimation);
                            Toasty.error(LoginActivityS.this,"Something went wrong",Toast.LENGTH_LONG).show();
                            loadingBar.dismiss();
                            loginBtn.setEnabled(true);
                        }

                    }
                });
    }
    protected boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            //Toast.makeText(this, "Yor are online", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            Toasty.warning(LoginActivityS.this,"You are offline",Toast.LENGTH_LONG).show();
            return false;
        }
    }
    private boolean gpsEnabled(){
        //***********************GPS start*************
        this.setFinishOnTouchOutside(true);
        final LocationManager manager = (LocationManager) LoginActivityS.this.getSystemService(Context.LOCATION_SERVICE);
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && hasGPSDevice(LoginActivityS.this)) {
            //Toast.makeText(SignInActivity.this,"Gps already enabled",Toast.LENGTH_SHORT).show();
            return true;
        }

        if(!hasGPSDevice(LoginActivityS.this)){
            Toasty.info(LoginActivityS.this,"Gps not Supported",Toast.LENGTH_LONG).show();
        }

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && hasGPSDevice(LoginActivityS.this)) {
            Toasty.info(LoginActivityS.this,"Please Enable your GPS",Toast.LENGTH_LONG).show();
            return false;
        }else{
            // Toast.makeText(SignInActivity.this,"Gps already enabled",Toast.LENGTH_SHORT).show();
            return true;
        }
        //*************GPS ENDS******************
    }
    //*********code for on the gps if its off***************************
    private boolean hasGPSDevice(Context context) {
        final LocationManager mgr = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
        if (mgr == null)
            return false;
        final List<String> providers = mgr.getAllProviders();
        if (providers == null)
            return false;
        return providers.contains(LocationManager.GPS_PROVIDER);
    }
    //ends here
    //back press function start here

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("EXIT?")
                .setIcon(R.drawable.exit)
                .setMessage("Are u sure you want to exit")
                .setCancelable(false)
                .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finishAffinity();
                    }
                })
                .setNegativeButton("Stay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create().show();
    }

    //back press ends.....
}
