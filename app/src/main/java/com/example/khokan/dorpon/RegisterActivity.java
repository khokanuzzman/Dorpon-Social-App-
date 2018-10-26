package com.example.khokan.dorpon;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

public class RegisterActivity extends AppCompatActivity {

    private EditText regUserEmail, regUserPassword, regUserConfirmPassword;
    private Button createAnAccountButton;
    private FirebaseAuth mAuth;
    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

//        Firebase initialize
        mAuth = FirebaseAuth.getInstance();


//        initialize
        regUserEmail = findViewById(R.id.reg_email);
        regUserPassword = findViewById(R.id.reg_password);
        regUserConfirmPassword = findViewById(R.id.reg_confirm_password);
        createAnAccountButton = findViewById(R.id.reg_create_account_btn);
        mDialog = new ProgressDialog(this);

        
        
        createAnAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = regUserEmail.getText().toString();
                String password = regUserPassword.getText().toString();
                String confirmPassword = regUserConfirmPassword.getText().toString();
                
                if (TextUtils.isEmpty(email))
                {
                    Toast.makeText(RegisterActivity.this, "Please Write Your Email..", Toast.LENGTH_SHORT).show();
                }else if (TextUtils.isEmpty(password))
                {
                    Toast.makeText(RegisterActivity.this, "Please Write Your Password", Toast.LENGTH_SHORT).show();
                }else if (TextUtils.isEmpty(confirmPassword))
                {
                    Toast.makeText(RegisterActivity.this, "Please Write Your Confirm Password..", Toast.LENGTH_SHORT).show();    
                }else if (!password.equals(confirmPassword))
                {
                    Toast.makeText(RegisterActivity.this, "Your Password did not Match, Please Match your Password To Confirm...", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    mDialog.setTitle("Creating A New Account");
                    mDialog.setMessage("Please Wait while we are creating you new Account!");
                    mDialog.show();
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful())
                                    {
                                        mDialog.dismiss();
                                        sendToSetupActivity();
                                        Toast.makeText(RegisterActivity.this, "User Authenticated Successfull...", Toast.LENGTH_SHORT).show();
                                    }else
                                        {
                                            String error = task.getException().getMessage();
                                            Toast.makeText(RegisterActivity.this, "Error occured: "+error, Toast.LENGTH_SHORT).show();
                                            mDialog.dismiss();
                                        }
                                }
                            });
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null)
        {
            sendToMainActivity();
        }
    }

    private void sendToMainActivity() {

        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void sendToSetupActivity() {
        Intent setupIntent = new Intent(RegisterActivity.this, SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }
}
