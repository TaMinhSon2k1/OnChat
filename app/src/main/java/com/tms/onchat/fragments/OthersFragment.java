package com.tms.onchat.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.tms.onchat.R;
import com.tms.onchat.activities.ChatActivity;
import com.tms.onchat.adapers.RecyclerViewOthersAdapter;
import com.tms.onchat.databinding.FragmentOthersBinding;
import com.tms.onchat.listeners.OtherListener;
import com.tms.onchat.models.User;
import com.tms.onchat.utilities.Constants;
import com.tms.onchat.utilities.PreferenceManager;

import java.util.ArrayList;

public class OthersFragment extends Fragment implements OtherListener {
    private FragmentOthersBinding binding;

    private RecyclerViewOthersAdapter othersAdapter;

    private ArrayList<User> userArrayList;

    private PreferenceManager preferenceManager;

    public OthersFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentOthersBinding.inflate(getLayoutInflater());

        preferenceManager = new PreferenceManager(getContext());

        setRecyclerViewOthers();
        getOthers();
        setListeners();

        return binding.getRoot();
    }

    private void setListeners() {
        /** Refresh userArrayList */
        binding.refreshOthers.setOnRefreshListener(() -> {
            binding.refreshOthers.setRefreshing(false);
            getOthers();
        });

        /** Search other */
        binding.edtInputName.setOnEditorActionListener((v, actionId, event) -> {
            if(actionId == EditorInfo.IME_ACTION_SEARCH) {
                othersAdapter.getFilter().filter(binding.edtInputName.getText().toString().trim());
                return true;
            }
            return false;
        });
    }

    /** TODO: Load list others on firebase */
    private void getOthers() {
        waitGetOthers(true);
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    userArrayList.clear();
                    String currentUID = preferenceManager.getString(Constants.KEY_USER_UID);
                    if(task.isSuccessful() && task.getResult() != null) {
                        for(QueryDocumentSnapshot snapshot : task.getResult()) {
                            /** If is current user will continue, not add userArrayList */
                            if(currentUID.equals(snapshot.get(Constants.KEY_USER_UID))) {
                                continue;
                            }

                            /** else will add userArrayList */
                            User user = new User();
                            user.firstName = snapshot.getString(Constants.KEY_USER_FIRST_NAME);
                            user.lastName = snapshot.getString(Constants.KEY_USER_LAST_NAME);
                            user.dateBirthday = snapshot.getString(Constants.KEY_USER_BIRTHDAY);
                            user.sex = snapshot.getString(Constants.KEY_USER_SEX);
                            user.image = snapshot.getString(Constants.KEY_USER_IMAGE);
                            user.uid = snapshot.getString(Constants.INTENT_USER_ID);
                            user.email = snapshot.getString(Constants.KEY_USER_EMAIL);
                            user.documnetId = snapshot.getId();
                            user.fcm = snapshot.getString(Constants.KEY_USER_FCM);

                            userArrayList.add(user);
                        }

                        waitGetOthers(false);
                        if(userArrayList.size() > 0) {
                            othersAdapter.notifyDataSetChanged();
                        } else {
                            showToast(getString(R.string.toast_not_have_others));
                        }
                    } else {
                        showToast(getString(R.string.toast_get_others_failured));
                    }
                });
    }

    private void waitGetOthers(boolean isWait) {
        if(isWait) {
            binding.rcOthers.setVisibility(View.INVISIBLE);
            binding.pbWait.setVisibility(View.VISIBLE);
        } else {
            binding.pbWait.setVisibility(View.GONE);
            binding.rcOthers.setVisibility(View.VISIBLE);
        }
    }

    private void setRecyclerViewOthers() {
        userArrayList = new ArrayList<>();
        othersAdapter = new RecyclerViewOthersAdapter(userArrayList, this);
        binding.rcOthers.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
        binding.rcOthers.setAdapter(othersAdapter);
    }

    private void showToast(String mes) {
        Toast.makeText(getContext(), mes, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void sendMessageOther(User user) {
        Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra(Constants.INTENT_USER, user);
        startActivity(intent);
    }
}