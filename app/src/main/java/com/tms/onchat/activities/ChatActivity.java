package com.tms.onchat.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.tms.onchat.R;
import com.tms.onchat.adapers.RecyclerViewMessagesAdapter;
import com.tms.onchat.databinding.ActivityChatBinding;
import com.tms.onchat.models.Message;
import com.tms.onchat.models.User;
import com.tms.onchat.networks.ApiClient;
import com.tms.onchat.networks.ApiService;
import com.tms.onchat.utilities.Constants;
import com.tms.onchat.utilities.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends BaseActivity {

    private ActivityChatBinding binding;

    private RecyclerViewMessagesAdapter messagesAdapter;

    private ArrayList<Message> messageArrayList;

    private User receiver;
    private boolean isAvailable;
    private String recentId;

    private PreferenceManager preferenceManager;

    /** Listener realtime send/receive message */
    private EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if(error != null) {
            return;
        }

        if(value != null) {
            int count = messageArrayList.size();
            for(DocumentChange change : value.getDocumentChanges()) {
                if(change.getType() == DocumentChange.Type.ADDED) {
                    Message message = new Message();
                    message.message = change.getDocument().getString(Constants.KEY_CHATS_MESSAGE);
                    message.senderID = change.getDocument().getString(Constants.KEY_CHATS_SENDER_ID);
                    message.receiverID = change.getDocument().getString(Constants.KEY_CHATS_RECEIVER_ID);
                    message.date = convertDateToString(change.getDocument().getDate(Constants.KEY_CHATS_DATE));
                    message.dateObj = change.getDocument().getDate(Constants.KEY_CHATS_DATE);
                    messageArrayList.add(message);
                }
            }

            Collections.sort(messageArrayList, (o1, o2) -> o1.dateObj.compareTo(o2.dateObj));
            if(count == 0) {
                messagesAdapter.notifyDataSetChanged();
            } else {
                messagesAdapter.notifyItemRangeInserted(messageArrayList.size(), messageArrayList.size());
                binding.rcMessages.smoothScrollToPosition(messageArrayList.size() - 1);
                binding.rcMessages.setVisibility(View.VISIBLE);
            }
        }
        binding.pbWait.setVisibility(View.GONE);

        /** When move ChatActivity
         * or this is first which chat receiver */
        if(recentId == null) {
            checkRecent();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(this);

        Intent intent = getIntent();
        if(intent == null) {
            return;
        }
        receiver = (User) intent.getSerializableExtra(Constants.INTENT_USER);

        loadReceiver();
        setRecyclerViewMessages();
        listenMessages();
        setListeners();
    }

    private void setRecyclerViewMessages() {
        messageArrayList = new ArrayList<>();
        messagesAdapter = new RecyclerViewMessagesAdapter(
                messageArrayList,
                convertBase64ToBitmap(receiver.image),
                preferenceManager.getString(Constants.KEY_USER_UID)
        );
        binding.rcMessages.setAdapter(messagesAdapter);
    }

    private void setListeners() {
        binding.imgBack.setOnClickListener(v -> onBackPressed());
        binding.imgSend.setOnClickListener(v -> {
            if(checkValidDetails()) {
                sendMessage();
            }
        });
    }

    private void listenMessages() {
        binding.pbWait.setVisibility(View.VISIBLE);
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection(Constants.KEY_COLLECTION_CHATS)
                .whereEqualTo(Constants.KEY_CHATS_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_UID))
                .whereEqualTo(Constants.KEY_CHATS_RECEIVER_ID, receiver.uid)
                .addSnapshotListener(eventListener);
        firestore.collection(Constants.KEY_COLLECTION_CHATS)
                .whereEqualTo(Constants.KEY_CHATS_SENDER_ID, receiver.uid)
                .whereEqualTo(Constants.KEY_CHATS_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_UID))
                .addSnapshotListener(eventListener);
    }

    /** TODO: upload message to firestore */
    private void sendMessage() {
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_CHATS_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_UID));
        message.put(Constants.KEY_CHATS_RECEIVER_ID, receiver.uid);
        message.put(Constants.KEY_CHATS_MESSAGE, binding.edtInputMessage.getText().toString().trim());
        message.put(Constants.KEY_CHATS_DATE, new Date());

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection(Constants.KEY_COLLECTION_CHATS).add(message);

        if(recentId != null) {
            uploadRecentMessage(binding.edtInputMessage.getText().toString().trim());
        } else {
            HashMap<String, Object> recent = new HashMap<>();
            recent.put(Constants.KEY_RECENTS_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_UID));
            recent.put(
                    Constants.KEY_RECENTS_SENDER_NAME,
                    String.format("%s %s", preferenceManager.getString(Constants.KEY_USER_FIRST_NAME), preferenceManager.getString(Constants.KEY_USER_LAST_NAME))
            );
            recent.put(Constants.KEY_RECENTS_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_USER_IMAGE));
            recent.put(Constants.KEY_RECENTS_RECEIVER_ID, receiver.uid);
            recent.put(
                    Constants.KEY_RECENTS_RECEIVER_NAME,
                    String.format("%s %s", receiver.firstName, receiver.lastName)
            );
            recent.put(Constants.KEY_RECENTS_RECEIVER_IMAGE, receiver.image);
            recent.put(Constants.KEY_RECENTS_LAST_MESSAGE, binding.edtInputMessage.getText().toString().trim());
            recent.put(Constants.KEY_RECENTS_TIME, new Date());
            addRecentMessage(recent);
        }

        if(!isAvailable) {
            try {
                JSONArray tokens = new JSONArray();
                tokens.put(receiver.fcm);

                JSONObject data = new JSONObject();
                data.put(Constants.KEY_USER_DOCUMENT_ID, preferenceManager.getString(Constants.KEY_USER_DOCUMENT_ID));
                data.put(Constants.KEY_USER_FCM, preferenceManager.getString(Constants.KEY_USER_FCM));
                data.put(Constants.KEY_CHATS_MESSAGE, binding.edtInputMessage.getText().toString().trim());

                JSONObject body = new JSONObject();
                body.put(Constants.REMOTE_MSG_DATA, data);
                body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

                sendNotification(body.toString());
            } catch(JSONException e) {
                e.printStackTrace();
            }
        }

        binding.edtInputMessage.setText("");
    }

    /** TODO: Check message isn't empty */
    private boolean checkValidDetails() {
        String mes = binding.edtInputMessage.getText().toString().trim();
        if(mes.isEmpty() || mes.equals("")) {
            return false;
        }
        return true;
    }

    /** TODO: load information receiver */
    private void loadReceiver() {
        binding.imgReceiver.setImageBitmap(convertBase64ToBitmap(receiver.image));
        binding.txtNameReceiver.setText(String.format("%s %s", receiver.firstName, receiver.lastName));
        binding.txtEmailReceiver.setText(receiver.email);
    }

    /** TODO: Convert from Base64 to Bitmap */
    private Bitmap convertBase64ToBitmap(String image64) {
        byte[] bytes = Base64.decode(image64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    /** TODO: Send notification to receiver*/
    private void sendNotification(String mes) {
        ApiClient.getClient().create(ApiService.class).sendMessage(
                Constants.getRemoteMsgHeader(),
                mes
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.isSuccessful()) {
                    try {
                        if(response.body() != null) {
                            JSONObject responseObject = new JSONObject(response.body());
                            JSONArray result = responseObject.getJSONArray("results");
                            if(responseObject.getInt("failure") == 1) {
                                JSONObject error = (JSONObject) result.get(0);
                                return;
                            }
                        }
                    } catch(JSONException e) {
                        e.printStackTrace();
                    }
                    showToast(getString(R.string.toast_send_notification_successfully));
                } else {
                    showToast(getString(R.string.toast_send_notification_failured));
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }

    private void showToast(String mes) {
        Toast.makeText(getApplicationContext(), mes, Toast.LENGTH_SHORT).show();
    }

    private void listenAvailableOfReceiver() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection(Constants.KEY_COLLECTION_USERS).document(receiver.documnetId)
                .addSnapshotListener((value, error) -> {
                    if(error != null) {
                        return;
                    }

                    if(value != null) {
                        if(value.getLong(Constants.KEY_USER_AVAILABLE) != null) {
                            int available = Objects.requireNonNull(
                                    value.getLong(Constants.KEY_USER_AVAILABLE)
                            ).intValue();
                            isAvailable = available == 1;
                        }
                    }

                    if(isAvailable) {
                        binding.cvOnline.setVisibility(View.VISIBLE);
                    } else {
                        binding.cvOnline.setVisibility(View.INVISIBLE);
                    }
                });
    }

    /** TODO: add new recent */
    private void addRecentMessage(HashMap<String, Object> recent) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection(Constants.KEY_COLLECTION_RECENTS).add(recent)
                .addOnSuccessListener(documentReference -> recentId = documentReference.getId());
    }

    /** TODO: upload new recent */
    private void uploadRecentMessage(String message) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference documentReference = firestore.collection(Constants.KEY_COLLECTION_RECENTS).document(recentId);
        documentReference.update(
                Constants.KEY_RECENTS_LAST_MESSAGE, message,
                Constants.KEY_RECENTS_TIME, new Date()
        );
    }

    /** TODO: get recentId
     * if had message will assign recentId */
    private void checkRecent() {
        if(messageArrayList.size() != 0) {
            checkRecentRemotely(preferenceManager.getString(Constants.KEY_USER_UID), receiver.uid);
            checkRecentRemotely(receiver.uid, preferenceManager.getString(Constants.KEY_USER_UID));
        }
    }

    private void checkRecentRemotely(String senderId, String receiverId) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection(Constants.KEY_COLLECTION_RECENTS)
                .whereEqualTo(Constants.KEY_RECENTS_SENDER_ID, senderId)
                .whereEqualTo(Constants.KEY_RECENTS_RECEIVER_ID, receiverId)
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        recentId = documentSnapshot.getId();
                    }
                });
    }

    private String convertDateToString(Date date) {
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    @Override
    protected void onResume() {
        super.onResume();
        listenAvailableOfReceiver();
    }
}