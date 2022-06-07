package com.tms.onchat.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.tms.onchat.R;
import com.tms.onchat.activities.ChatActivity;
import com.tms.onchat.adapers.RecyclerViewRecentMessageAdapter;
import com.tms.onchat.databinding.FragmentRecentMessagesBinding;
import com.tms.onchat.listeners.RecentMessagesListener;
import com.tms.onchat.models.Message;
import com.tms.onchat.models.User;
import com.tms.onchat.utilities.Constants;
import com.tms.onchat.utilities.PreferenceManager;

import java.util.ArrayList;
import java.util.Collections;

public class RecentMessagesFragment extends Fragment implements RecentMessagesListener {
    private FragmentRecentMessagesBinding binding;

    private Dialog dialogWait;

    private RecyclerViewRecentMessageAdapter recentMessageAdapter;
    private ArrayList<Message> recentMessageArrayList;

    private PreferenceManager preferenceManager;

    private EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if(error != null) {
            return;
        }

        if(value != null) {
            for(DocumentChange change : value.getDocumentChanges()) {
                if(change.getType() == DocumentChange.Type.ADDED) {
                    Message message = new Message();
                    message.senderID = change.getDocument().getString(Constants.KEY_RECENTS_SENDER_ID);
                    message.receiverID = change.getDocument().getString(Constants.KEY_RECENTS_RECEIVER_ID);

                    if(preferenceManager.getString(Constants.KEY_USER_UID).equals(message.senderID)) {
                        message.recentID = change.getDocument().getString(Constants.KEY_RECENTS_RECEIVER_ID);
                        message.recentName = change.getDocument().getString(Constants.KEY_RECENTS_RECEIVER_NAME);
                        message.recentImage = change.getDocument().getString(Constants.KEY_RECENTS_RECEIVER_IMAGE);
                    } else {
                        message.recentID = change.getDocument().getString(Constants.KEY_RECENTS_SENDER_ID);
                        message.recentName = change.getDocument().getString(Constants.KEY_RECENTS_SENDER_NAME);
                        message.recentImage = change.getDocument().getString(Constants.KEY_RECENTS_SENDER_IMAGE);
                    }
                    message.message = change.getDocument().getString(Constants.KEY_RECENTS_LAST_MESSAGE);
                    message.dateObj = change.getDocument().getDate(Constants.KEY_RECENTS_TIME);
                    recentMessageArrayList.add(message);
                } else if(change.getType() == DocumentChange.Type.MODIFIED) {
                    for(int i = 0; i < recentMessageArrayList.size(); i++) {
                        String senderId = change.getDocument().getString(Constants.KEY_RECENTS_SENDER_ID);
                        String receiverId = change.getDocument().getString(Constants.KEY_RECENTS_RECEIVER_ID);

                        if(recentMessageArrayList.get(i).senderID.equals(senderId) && recentMessageArrayList.get(i).receiverID.equals(receiverId)) {
                            recentMessageArrayList.get(i).message = change.getDocument().getString(Constants.KEY_RECENTS_LAST_MESSAGE);
                            recentMessageArrayList.get(i).dateObj = change.getDocument().getDate(Constants.KEY_RECENTS_TIME);
                            break;
                        }
                    }
                }
            }
            Collections.sort(recentMessageArrayList, (o1, o2) -> o1.dateObj.compareTo(o2.dateObj));
            recentMessageAdapter.notifyDataSetChanged();
            binding.rcRecentMessage.smoothScrollToPosition(0);
            binding.rcRecentMessage.setVisibility(View.VISIBLE);
            binding.pbWait.setVisibility(View.GONE);
        }
    };

    public RecentMessagesFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRecentMessagesBinding.inflate(getLayoutInflater());

        preferenceManager = new PreferenceManager(getContext());

        setRecyclerViewRecentMessage();
        listenRecentChange();

        return binding.getRoot();
    }

    private void setRecyclerViewRecentMessage() {
        recentMessageArrayList = new ArrayList<>();
        recentMessageAdapter = new RecyclerViewRecentMessageAdapter(recentMessageArrayList, this);
        binding.rcRecentMessage.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
        binding.rcRecentMessage.setAdapter(recentMessageAdapter);
    }

    /** TODO: upload recycler view recent message when add or modify*/
    private void listenRecentChange() {
        binding.pbWait.setVisibility(View.VISIBLE);
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection(Constants.KEY_COLLECTION_RECENTS)
                .whereEqualTo(Constants.KEY_RECENTS_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_UID))
                .addSnapshotListener(eventListener);
        firestore.collection(Constants.KEY_COLLECTION_RECENTS)
                .whereEqualTo(Constants.KEY_RECENTS_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_UID))
                .addSnapshotListener(eventListener);
    }

    /** TODO: Show dialog wait to load receiver, not allow to user click outsize */
    private void showDialogWait(String mes) {
        if(dialogWait == null) {
            dialogWait = new Dialog(getContext());
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

    @Override
    public void sendMessageOther(User user) {
        dialogWait.dismiss();
        Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra(Constants.INTENT_USER, user);
        startActivity(intent);
    }

    @Override
    public void showDialogWait() {
        showDialogWait(getString(R.string.diaolog_wait_load_receiver));
    }
}