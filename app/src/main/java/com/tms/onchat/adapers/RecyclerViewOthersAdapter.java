package com.tms.onchat.adapers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tms.onchat.databinding.LayoutItemOrtherBinding;
import com.tms.onchat.listeners.OtherListener;
import com.tms.onchat.models.User;

import java.util.ArrayList;

public class RecyclerViewOthersAdapter extends RecyclerView.Adapter<RecyclerViewOthersAdapter.ViewHolder> implements Filterable {
    private ArrayList<User> userArrayList, userArrayListOld;
    private OtherListener otherListener;

    public RecyclerViewOthersAdapter(ArrayList<User> userArrayList, OtherListener otherListener) {
        this.userArrayList = userArrayList;
        this.userArrayListOld = userArrayList;
        this.otherListener = otherListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutItemOrtherBinding binding = LayoutItemOrtherBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setData(userArrayList.get(position));
    }

    @Override
    public int getItemCount() {
        if(userArrayList != null) {
            return userArrayList.size();
        }
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private LayoutItemOrtherBinding binding;

        public ViewHolder(LayoutItemOrtherBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setData(User user) {
            binding.imgImageOther.setImageBitmap(convertBase64ToBitmap(user.image));
            binding.txtNameOther.setText(String.format("%s %s", user.firstName, user.lastName));
            binding.txtEmailOther.setText(user.email);

            /** Move to chat activity */
            binding.layoutItemOther.setOnClickListener(v -> otherListener.sendMessageOther(user));
        }

        /** TODO: Convert from Base64 to Bitmap */
        private Bitmap convertBase64ToBitmap(String image64) {
            byte[] bytes = Base64.decode(image64, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }
    }

    /** TODO: Search other by name */
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String search = constraint.toString();
                if(search.isEmpty() || search.equals("")) {
                    userArrayList = userArrayListOld;
                } else {
                    ArrayList<User> temps = new ArrayList<>();
                    for(User user : userArrayListOld) {
                        if(String.format("%s %s", user.firstName, user.lastName).toLowerCase().contains(search.toLowerCase())) {
                            temps.add(user);
                        }
                    }
                    userArrayList = temps;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = userArrayList;

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                userArrayList = (ArrayList<User>) results.values;
                notifyDataSetChanged();
            }
        };
    }
}
