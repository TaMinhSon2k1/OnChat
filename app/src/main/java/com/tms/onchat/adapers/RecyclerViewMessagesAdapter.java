package com.tms.onchat.adapers;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tms.onchat.databinding.LayoutItemMessageReceivedBinding;
import com.tms.onchat.databinding.LayoutItemMessageSentBinding;
import com.tms.onchat.models.Message;

import java.util.ArrayList;

public class RecyclerViewMessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<Message> messageArrayList;
    private Bitmap imgReceiver;
    private String senderID;

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    public RecyclerViewMessagesAdapter(ArrayList<Message> messageArrayList, Bitmap imgReceiver, String senderID) {
        this.messageArrayList = messageArrayList;
        this.imgReceiver = imgReceiver;
        this.senderID = senderID;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_SENT) {
            LayoutItemMessageSentBinding binding = LayoutItemMessageSentBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent,
                    false
            );
            return new RecyclerViewMessagesAdapter.SentViewHolder(binding);
        } else {
            LayoutItemMessageReceivedBinding binding = LayoutItemMessageReceivedBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent,
                    false
            );
            return new RecyclerViewMessagesAdapter.ReceivedViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position) == VIEW_TYPE_SENT) {
            ((SentViewHolder) holder).setData(messageArrayList.get(position));
        } else {
            ((ReceivedViewHolder) holder).setData(messageArrayList.get(position), imgReceiver);
        }
    }

    @Override
    public int getItemCount() {
        if(messageArrayList != null) {
            return messageArrayList.size();
        }
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        if(messageArrayList.get(position).senderID.equals(this.senderID)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    public class SentViewHolder extends RecyclerView.ViewHolder {
        private LayoutItemMessageSentBinding binding;

        public SentViewHolder(LayoutItemMessageSentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setData(Message message) {
            binding.txtMessage.setText(message.message);
            binding.txtDateSent.setText(message.date);
        }
    }

    public class ReceivedViewHolder extends RecyclerView.ViewHolder {
        private LayoutItemMessageReceivedBinding binding;

        public ReceivedViewHolder(LayoutItemMessageReceivedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setData(Message message, Bitmap imgReceiver) {
            binding.txtMessage.setText(message.message);
            binding.txtDateSent.setText(message.date);
            binding.imgReceiver.setImageBitmap(imgReceiver);
        }
    }
}
