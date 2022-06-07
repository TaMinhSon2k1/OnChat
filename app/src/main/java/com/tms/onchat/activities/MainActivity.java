package com.tms.onchat.activities;

import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.tms.onchat.R;
import com.tms.onchat.adapers.ViewpagerMessagesOrOthersAdapter;
import com.tms.onchat.databinding.ActivityMainBinding;
import com.tms.onchat.utilities.Constants;
import com.tms.onchat.utilities.PreferenceManager;

import java.util.HashMap;

public class MainActivity extends BaseActivity {

    private ActivityMainBinding binding;

    private ViewpagerMessagesOrOthersAdapter messagesOrOthersAdapter;

    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(this);
        String image64 = preferenceManager.getString(Constants.KEY_USER_IMAGE);
        if(image64 != null) {
            loadImage(image64);
        }

        setBottomNavigationMenuMessagesOrOthers();
        getToken();

        setListeners();
    }

    private void setListeners() {
        binding.imgSignOut.setOnClickListener(v -> signOut());
    }

    /** TODO: Sign out and clear SharedPreference */
    private void signOut() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference documentReference = firestore.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_DOCUMENT_ID));
        HashMap<String, Object> update = new HashMap<>();
        update.put(Constants.KEY_USER_FCM, FieldValue.delete());
        documentReference.update(update)
                .addOnSuccessListener(unused -> {
                    FirebaseAuth.getInstance().signOut();
                    preferenceManager.clear();
                    startActivity(new Intent(getApplicationContext(), SignInOrUpActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> showToast(getString(R.string.toast_sign_out_failured)));
    }

    /** TODO: set image user by base64 */
    private void loadImage(String image64) {
        binding.imgImage.setImageBitmap(convertBase64ToBitmap(image64));
    }

    /** TODO: Convert from Base64 to Bitmap */
    private Bitmap convertBase64ToBitmap(String image64) {
        byte[] bytes = Base64.decode(image64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private void setBottomNavigationMenuMessagesOrOthers() {
        messagesOrOthersAdapter = new ViewpagerMessagesOrOthersAdapter(this);
        binding.vpMenuMessagesOrOthers.setAdapter(messagesOrOthersAdapter);

        /** Set title default */
        binding.txtTitle.setText(getString(R.string.title_messages));

        /** Set to bottom navigation when click navigation */
        binding.nvgtMenuMessagesOrOthers.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if(id == R.id.item_message) {
                binding.vpMenuMessagesOrOthers.setCurrentItem(0);
                binding.txtTitle.setText(getString(R.string.title_messages));
            } else if(id == R.id.item_others) {
                binding.vpMenuMessagesOrOthers.setCurrentItem(1);
                binding.txtTitle.setText(getString(R.string.title_others));
            } else {
                binding.vpMenuMessagesOrOthers.setCurrentItem(0);
                binding.txtTitle.setText(getString(R.string.title_messages));
            }
            return true;
        });

        /** Set to ViewPager2 when move activity between Messages and Others */
        binding.vpMenuMessagesOrOthers.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0:
                        binding.nvgtMenuMessagesOrOthers.getMenu().findItem(R.id.item_message).setChecked(true);
                        binding.txtTitle.setText(getString(R.string.title_messages));
                        break;

                    case 1:
                        binding.nvgtMenuMessagesOrOthers.getMenu().findItem(R.id.item_others).setChecked(true);
                        binding.txtTitle.setText(getString(R.string.title_others));
                        break;
                }
            }
        });
    }

    private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(s -> updateFCM(s));
    }

    /** TODO: Upload fcm when sign in */
    private void updateFCM(String token) {
        preferenceManager.putString(Constants.KEY_USER_FCM, token);
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference documentReference = firestore.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_DOCUMENT_ID));
        documentReference.update(Constants.KEY_USER_FCM, token)
                .addOnSuccessListener(unused -> showToast(getString(R.string.toast_upload_token_successfully)))
                .addOnFailureListener(e -> showToast(getString(R.string.toast_uplaod_token_failured)));
    }

    private void showToast(String mes) {
        Toast.makeText(getApplicationContext(), mes, Toast.LENGTH_SHORT).show();
    }
}