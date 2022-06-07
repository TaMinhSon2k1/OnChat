package com.tms.onchat.firebase;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.tms.onchat.R;
import com.tms.onchat.activities.ChatActivity;
import com.tms.onchat.models.User;
import com.tms.onchat.utilities.Constants;

import java.util.Random;

public class MessageService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection(Constants.KEY_COLLECTION_USERS).document(message.getData().get(Constants.KEY_USER_DOCUMENT_ID)).get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null) {
                        User user = new User();
                        user.uid = task.getResult().getString(Constants.KEY_USER_UID);
                        user.firstName = task.getResult().getString(Constants.KEY_USER_FIRST_NAME);
                        user.lastName = task.getResult().getString(Constants.KEY_USER_LAST_NAME);
                        user.dateBirthday = task.getResult().getString(Constants.KEY_USER_BIRTHDAY);
                        user.sex = task.getResult().getString(Constants.KEY_USER_SEX);
                        user.image = task.getResult().getString(Constants.KEY_USER_IMAGE);
                        user.email = task.getResult().getString(Constants.KEY_USER_EMAIL);
                        user.documnetId = message.getData().get(Constants.KEY_USER_DOCUMENT_ID);
                        user.fcm = task.getResult().getString(Constants.KEY_USER_FCM);

                        int notificationId = new Random().nextInt();
                        String channelId = "on_chat";
                        Intent intent = new Intent(this, ChatActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.putExtra(Constants.INTENT_USER, user);
                        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
                        builder.setSmallIcon(R.drawable.ic_notification);
                        builder.setContentTitle(user.firstName + user.lastName);
                        builder.setContentText(message.getData().get(Constants.KEY_CHATS_MESSAGE));
                        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(
                                message.getData().get(Constants.KEY_CHATS_MESSAGE)
                        ));
                        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
                        builder.setContentIntent(pendingIntent);
                        builder.setAutoCancel(true);

                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            CharSequence channelName = "OnChat";
                            String channelDescription = "This is OnChat's notification";
                            int importance = NotificationManager.IMPORTANCE_DEFAULT;
                            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
                            channel.setDescription(channelDescription);
                            NotificationManager manager = getSystemService(NotificationManager.class);
                            manager.createNotificationChannel(channel);
                        }

                        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
                        notificationManagerCompat.notify(notificationId, builder.build());
                    }
                });
    }
}
