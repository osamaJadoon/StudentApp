package com.example.studentapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import es.dmoral.toasty.Toasty;

public class ForgotPasswordActivityS extends AppCompatActivity {

    private EditText forgotEmail;
    private Button submit;

    private FirebaseAuth mAuth;

    ProgressDialog loadingBar;

    AlertDialog.Builder alertDialoge;
    Animation shakeAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_passwords);

        shakeAnimation = AnimationUtils.loadAnimation(this,R.anim.shake);

        alertDialoge = new AlertDialog.Builder(this);
        alertDialoge.setTitle("Reset Password");
        alertDialoge.setMessage("Please check your email, we just send a link to reset your password");
        alertDialoge.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialoge.create();

        forgotEmail = findViewById(R.id.forgot_email_id);
        submit = findViewById(R.id.submit_btn_id);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ResetFunction();
            }
        });
    }
    private void ResetFunction() {

        loadingBar = new ProgressDialog(this);
        loadingBar.setTitle("Reset Password");
        loadingBar.setMessage("Just a moment...!");
        loadingBar.show();
        loadingBar.setCanceledOnTouchOutside(false);
        submit.setEnabled(false);

        String Email = forgotEmail.getText().toString();
        if (Email.isEmpty()) {
            Toasty.info(this,"Please Enter your Email Id",Toast.LENGTH_LONG).show();
            loadingBar.dismiss();
            forgotEmail.startAnimation(shakeAnimation);
            submit.setEnabled(true);
        } else {


            mAuth.sendPasswordResetEmail(Email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toasty.success(ForgotPasswordActivityS.this,"Email Send",Toast.LENGTH_LONG).show();
                                loadingBar.dismiss();
                                alertDialoge.show();
                            } else {
                                Toasty.error(ForgotPasswordActivityS.this,"Failed to send Email",Toast.LENGTH_LONG).show();
                                loadingBar.dismiss();
                                submit.setEnabled(true);
                            }
                        }
                    });
        }
    }

}
