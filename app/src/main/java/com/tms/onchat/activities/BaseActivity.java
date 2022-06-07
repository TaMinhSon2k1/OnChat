package com.tms.onchat.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tms.onchat.utilities.Constants;
import com.tms.onchat.utilities.PreferenceManager;

public class BaseActivity extends AppCompatActivity {
    private DocumentReference documentReference;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(this);
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        documentReference = firestore.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_DOCUMENT_ID));
    }

    @Override
    protected void onPause() {
        super.onPause();
        documentReference.update(Constants.KEY_USER_AVAILABLE, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        documentReference.update(Constants.KEY_USER_AVAILABLE, 1);
    }
}
