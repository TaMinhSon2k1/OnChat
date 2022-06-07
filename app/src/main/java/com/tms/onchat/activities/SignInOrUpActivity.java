package com.tms.onchat.activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.tms.onchat.R;
import com.tms.onchat.adapers.ViewPagerSignInOrSUpAdapter;
import com.tms.onchat.databinding.ActivitySignInOrUpBinding;
import com.tms.onchat.listeners.SignInOrUpListener;
import com.tms.onchat.utilities.Constants;
import com.tms.onchat.utilities.PreferenceManager;

import java.util.HashMap;

public class SignInOrUpActivity extends AppCompatActivity implements SignInOrUpListener {

    private ActivitySignInOrUpBinding binding;

    private Dialog dialogWait;

    private ViewPagerSignInOrSUpAdapter signInOrSUpAdapter;

    private PreferenceManager preferenceManager;

    private GoogleSignInOptions gso;
    private GoogleSignInClient gsc;
    private FirebaseAuth auth;
    private FirebaseUser user;

    private ActivityResultLauncher<Intent> signInGoogleLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        showDialogWait(getString(R.string.dialog_wait_sign_in));

                        Intent data = result.getData();

                        /** Get account when login to set on Firebase */
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        try {
                            task.getResult(ApiException.class);
                            gsc = GoogleSignIn.getClient(getApplicationContext(), gso);
                            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());

                            signInWithGoogle(account.getIdToken());
                        } catch (ApiException e) {
                            showToast(e.getMessage());
                            dialogWait.dismiss();
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInOrUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        /** Check isSign in SharedPreference to move MainActivity */
        preferenceManager = new PreferenceManager(this);
        if(preferenceManager.getBoolean(Constants.KEY_IS_SIGN_IN)) {
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }

        setGoogleSignIn();
        setTabLayoutSignInOrUp();
        setListeners();
    }

    private void setListeners() {
        binding.imgGoogle.setOnClickListener(v -> signInWithGoogle());
    }

    private void setGoogleSignIn() {
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(Constants.REQUEST_ID_TOKEN)
                .requestEmail()
                .build();

        gsc = GoogleSignIn.getClient(this, gso);
    }

    /** TODO: Sign in with Google's account */
    private void signInWithGoogle() {
        Intent intent = gsc.getSignInIntent();
        signInGoogleLauncher.launch(intent);
    }

    private void setTabLayoutSignInOrUp() {
        signInOrSUpAdapter = new ViewPagerSignInOrSUpAdapter(this);
        binding.vpSignInOrUp.setAdapter(signInOrSUpAdapter);

        /** TODO: Connect tablayout with viewpaper2
         * if position = 0 => tab's title is "SIGN IN"
         * if position = 1 => tab's title is "SIGN UP""
         * */
        new TabLayoutMediator(binding.tlSignInOrUp, binding.vpSignInOrUp, (tab, position) -> {
            switch(position) {
                case 0:
                    tab.setText(getString(R.string.title_sign_in));
                    break;

                case 1:
                    tab.setText(getString(R.string.title_sign_up));
                    break;
            }
        }).attach();
    }

    /** TODO: Show dialog wait to sign in, sign up, not allow to user click outsize */
    private void showDialogWait(String mes) {
        if(dialogWait == null) {
            dialogWait = new Dialog(this);
            dialogWait.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialogWait.setContentView(R.layout.layout_wait);

            Window window = dialogWait.getWindow();
            if(window == null) {
                return;
            }

            window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);

            WindowManager.LayoutParams windowAttributes = window.getAttributes();
            windowAttributes.gravity = Gravity.CENTER;
            window.setAttributes(windowAttributes);
            dialogWait.setCancelable(false);
        }

        TextView txtWait = dialogWait.findViewById(R.id.txtWait);
        txtWait.setText(mes);

        dialogWait.show();
    }

    /** TODO: Move fill information or main activity
     * if user has information, will move main activity
     * else will move fill information user activity*/
    private void moveFillInfoOrMainActivity() {
        showDialogWait(getString(R.string.dialog_wait_check_have_information));
        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_USER_UID, user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() &&
                            task.getResult() != null &&
                            task.getResult().getDocuments().size() > 0) {
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        /** Data to upload SharedPreference */
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGN_IN, true);
                        preferenceManager.putString(Constants.KEY_USER_FIRST_NAME, documentSnapshot.getString(Constants.KEY_USER_FIRST_NAME));
                        preferenceManager.putString(Constants.KEY_USER_LAST_NAME, documentSnapshot.getString(Constants.KEY_USER_LAST_NAME));
                        preferenceManager.putString(Constants.KEY_USER_BIRTHDAY, documentSnapshot.getString(Constants.KEY_USER_BIRTHDAY));
                        preferenceManager.putString(Constants.KEY_USER_SEX, documentSnapshot.getString(Constants.KEY_USER_SEX));
                        preferenceManager.putString(Constants.KEY_USER_IMAGE, documentSnapshot.getString(Constants.KEY_USER_IMAGE));
                        preferenceManager.putString(Constants.KEY_USER_UID, documentSnapshot.getString(Constants.INTENT_USER_ID));
                        preferenceManager.putString(Constants.KEY_USER_EMAIL, documentSnapshot.getString(Constants.INTENT_USER_EMAIL));
                        preferenceManager.putString(Constants.KEY_USER_DOCUMENT_ID, documentSnapshot.getId());

                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(getApplicationContext(), FillInformationActivity.class);
                        intent.putExtra(Constants.INTENT_USER_ID, user.getUid());
                        intent.putExtra(Constants.INTENT_USER_EMAIL, user.getEmail());
                        startActivity(intent);
                    }
                });
    }

    private void signInWithGoogle(String idToken) {
        AuthCredential criteria = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(criteria)
                .addOnSuccessListener(authResult -> {
                    user = authResult.getUser();
                    showToast(getString(R.string.toast_sign_in_successful));

                    /** Move activity */
                    moveFillInfoOrMainActivity();
                })
                .addOnFailureListener(e -> showToast(getString(R.string.toast_sign_in_failured)))
                .addOnCompleteListener(task -> dialogWait.dismiss());
    }

    /** Listener of SignUpFragmnet */
    @Override
    public void signUpUserByEmailPassword(String email, String password) {
        showDialogWait(getString(R.string.dialog_wait_sign_up));
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    user = authResult.getUser();
                    showToast(getString(R.string.toast_sign_up_successful));

                    /** Move activity */
                    moveFillInfoOrMainActivity();
                })
                .addOnFailureListener(e -> showToast(getString(R.string.toast_sign_up_failured)))
                .addOnCompleteListener(task -> dialogWait.dismiss());
    }

    /** Listener of SignInFragmnet */
    @Override
    public void signInUserByEmailPassword(String email, String password) {
        showDialogWait(getString(R.string.dialog_wait_sign_in));
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    user = authResult.getUser();
                    showToast(getString(R.string.toast_sign_in_successful));

                    /** Move activity */
                    moveFillInfoOrMainActivity();
                })
                .addOnFailureListener(e -> showToast(getString(R.string.toast_sign_in_failured)))
                .addOnCompleteListener(task -> dialogWait.dismiss());
    }

    private void showToast(String mes) {
        Toast.makeText(getApplicationContext(), mes, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        auth = FirebaseAuth.getInstance();
    }
}