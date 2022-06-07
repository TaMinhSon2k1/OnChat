package com.tms.onchat.adapers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.tms.onchat.databinding.LayoutItemRecentMessagesBinding;
import com.tms.onchat.listeners.RecentMessagesListener;
import com.tms.onchat.models.Message;
import com.tms.onchat.models.User;
import com.tms.onchat.utilities.Constants;

import java.util.ArrayList;

public class RecyclerViewRecentMessageAdapter extends RecyclerView.Adapter<RecyclerViewRecentMessageAdapter.ViewHolder> implements Filterable {
    private ArrayList<Message> messageArrayList;
    private ArrayList<Message> messageArrayListOld;

    private RecentMessagesListener recentMessagesListener;

    public RecyclerViewRecentMessageAdapter(ArrayList<Message> messageArrayList, RecentMessagesListener recentMessagesListener) {
        this.messageArrayList = messageArrayList;
        this.messageArrayListOld = messageArrayList;
        this.recentMessagesListener = recentMessagesListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutItemRecentMessagesBinding binding = LayoutItemRecentMessagesBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setData(messageArrayList.get(position));
    }

    @Override
    public int getItemCount() {
        if(messageArrayList != null) {
            return messageArrayList.size();
        }
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private LayoutItemRecentMessagesBinding binding;

        public ViewHolder(LayoutItemRecentMessagesBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setData(Message message) {
            binding.imgReceiver.setImageBitmap(convertBase64ToBitmap(message.recentImage));
            binding.txtNameReceiver.setText(message.recentName);
            binding.txtRecentMessage.setText(message.message);

            binding.layoutItemRecentMessage.setOnClickListener(v -> {
                recentMessagesListener.showDialogWait();
                FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                firestore.collection(Constants.KEY_COLLECTION_USERS).get()
                        .addOnCompleteListener(task -> {
                            if(task.isSuccessful() && task.getResult() != null) {
                                for(QueryDocumentSnapshot snapshot : task.getResult()) {
                                    if(snapshot.getString(Constants.KEY_USER_UID).equals(message.recentID)) {
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

                                        recentMessagesListener.sendMessageOther(user);
                                        break;
                                    }
                                }
                            }
                        });
            });
        }

        /** TODO: Convert from Base64 to Bitmap */
        private Bitmap convertBase64ToBitmap(String image64) {
            byte[] bytes = Base64.decode(image64, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }
    }

    /** TODO: Search recent message by name */
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String search = constraint.toString();
                if(search.isEmpty() || search.equals("")) {
                    messageArrayList = messageArrayListOld;
                } else {
                    ArrayList<Message> temps = new ArrayList<>();
                    for(Message message : messageArrayListOld) {
                        if(message.recentName.toLowerCase().contains(search.toLowerCase())) {
                            temps.add(message);
                        }
                    }
                    messageArrayList = temps;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = messageArrayList;

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                messageArrayList = (ArrayList<Message>) results.values;
                notifyDataSetChanged();
            }
        };
    }
}
