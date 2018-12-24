package lagi.garap.tubes;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginAct extends AppCompatActivity {

    //inisialisasi TAG, RC_signIn, edit text, button, authFirebase, googleSignInClient
    private static final String TAG = "GoogleActivity";
    private static final String TAG_EmailPassword = "EmailPassword";
    private static final int RC_SIGN_IN = 9001;
    EditText txtEmail,txtPwd;
    Button btnLogin,btnRegister,btnGoogle;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //membuat variabel untuk dihubungkan dengan id komponen button, textview pada layout
        btnLogin = (Button)findViewById(R.id.btnLogin);
        btnGoogle = (Button)findViewById(R.id.btnGoogle);
        btnRegister = (Button)findViewById(R.id.btnRegister);
        txtEmail = (EditText) findViewById(R.id.textEmail);
        txtPwd= (EditText)findViewById(R.id.textPass);

        //membuat object googleSignInOptions, meminta Idtoken, dan email
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        //mendapatkan client dari googleSignIn
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        //mendapat instance dari authFirebase
        mAuth = FirebaseAuth.getInstance();
        //aksi saat btnGoogle ditekan
        btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //memanggil method signIn
                signIn();
            }
        });
        //memberi aksi pada btnRegister
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //memulai aktivity intent ke register activity
                startActivity(new Intent(LoginAct.this, RegisterAct.class));
            }
        });
        //memberi aksi pada btnLogin
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //memanggil method signInLogin untuk mendapat email dan password
                signInLogin(txtEmail.getText().toString(),
                        txtPwd.getText().toString());
            }
        });
    }

    //inisialisasi method signInLogin
    private void signInLogin(String email, String password) {

        //memanggil method signIn dengan email dan password dari mAuth
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //jika sign in sukses, maka update UI dengan info user yg login
                            Log.d(TAG_EmailPassword, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            //jika gagal, tampilkan pesan ke user
                            Log.w(TAG_EmailPassword, "signInWithEmail:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                    }
                });
    }

    //inisialisasi method signIn
    private void signIn() {
        //membuat intent dari mGoogleSignInClient
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        //memulai aktivitas signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    //menginisialisasi method updateUI
    private void updateUI(FirebaseUser user) {
        //memeriksa apa user ada
        if (user != null) {
            //jika ada, mulai aktivity intent ke CameraActivity
            startActivity(new Intent(LoginAct.this, CamActivity.class));
            //tutup activity login
            finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //memeriksa apakah kode request sama dengan RC_Sign_In
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                //memanggil method firebaseAuth dengan Google
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                //jika signIn google gagal
                Log.w(TAG, "Google sign in failed", e);
                updateUI(null);

            }
        }

    }

    //inisialisasi method firebaseAuthWithGoogle
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        //membuat credential dari GoogleAuthProvider
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //jika login sukses, maka update UI
                            Log.d(TAG, "signInWithCredential:success");
                            //membuat user dari firebaseUser
                            FirebaseUser user = mAuth.getCurrentUser();
                            startActivity(new Intent(LoginAct.this, MainActivity.class));
                            finish();
                            updateUI(user);

                        } else {
                            // jika login gagal, update updateUI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginAct.this, "Try Again", Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }


                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

}
