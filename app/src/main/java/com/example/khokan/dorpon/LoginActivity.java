package com.example.khokan.dorpon;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private Button loginButton;
    private TextView userEmail,userPassword;
    private TextView needAnAccount;
    private FirebaseAuth mAuth;
    private ProgressDialog mdialog;
    private final static int RC_SIGN_IN =1;
    private ImageView googleSignInbutton;
    private GoogleApiClient mGoogleSignInClient;
    private final static String TAG = "LoginActivity";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

//        Firebase Initialize
        mAuth = FirebaseAuth.getInstance();

//        initialize views
        loginButton = findViewById(R.id.login_login_btn);
        userEmail = findViewById(R.id.login_email);
        userPassword = findViewById(R.id.login_password);
        needAnAccount = findViewById(R.id.register_account_link);
        googleSignInbutton = findViewById(R.id.login_with_google);
        mdialog = new ProgressDialog(this);


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mdialog.setTitle("Logging");
                mdialog.setMessage("Please Wait while Logging processing");
                mdialog.setCanceledOnTouchOutside(true);
                mdialog.show();

                String email=userEmail.getText().toString();
                String passowrd = userPassword.getText().toString();
                if (TextUtils.isEmpty(email))
                {

                    Toast.makeText(LoginActivity.this, "Please Write Your Email...", Toast.LENGTH_SHORT).show();
                }
                else if (TextUtils.isEmpty(passowrd))
                {
                    Toast.makeText(LoginActivity.this, "Please Write Your password...", Toast.LENGTH_SHORT).show();
                }
                else
                {

                    mAuth.signInWithEmailAndPassword(email,passowrd)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful())
                                    {
                                        sendToMainActivity();
                                        Toast.makeText(LoginActivity.this, "Welcome Loggin Successfully Done!", Toast.LENGTH_SHORT).show();
                                        mdialog.dismiss();
                                    }else
                                        {
                                            String error = task.getException().getMessage();
                                            Toast.makeText(LoginActivity.this, "Error occured: "+error, Toast.LENGTH_SHORT).show();
                                            mdialog.dismiss();
                                        }
                                }
                            });
                }
            }
        });

        needAnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToRegisterActivity();
            }
        });

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(LoginActivity.this, "Google Sign In Failed", Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();

        googleSignInbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });


    }
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleSignInClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {

            mdialog.setTitle("Google Sign In");
            mdialog.setMessage("Please Wait while we are allowing to login with google account..");
            mdialog.setCanceledOnTouchOutside(true);
            mdialog.show();

            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess())
            {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
                Toast.makeText(this, "Please Wait, While we are getting auth result", Toast.LENGTH_SHORT).show();
            }else 
            {
                Toast.makeText(this, "Can't Got auth result...", Toast.LENGTH_SHORT).show();
                mdialog.dismiss();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            sendToMainActivity();
                            mdialog.dismiss();

                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            String message =task.getException().toString();
                            Toast.makeText(LoginActivity.this, "Not Authenticated:"+message, Toast.LENGTH_SHORT).show();
                            sendtoUserToLoginActivity();
                            mdialog.dismiss();
                        }
                    }
                });
    }

    private void sendtoUserToLoginActivity() {
        Intent mainIntent = new Intent(LoginActivity.this, LoginActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
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

        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void sendToRegisterActivity() {
        Intent regIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(regIntent);
    }

}
