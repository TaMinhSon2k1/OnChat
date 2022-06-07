package com.tms.onchat.adapers;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.tms.onchat.fragments.RecentMessagesFragment;
import com.tms.onchat.fragments.OthersFragment;
import com.tms.onchat.utilities.Constants;

public class ViewpagerMessagesOrOthersAdapter extends FragmentStateAdapter {

    public ViewpagerMessagesOrOthersAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    /** Set fragment show
     * if position = 0 => fragment is messages
     * if position = 1 => fragment is others
     * */
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch(position) {
            case 0:
                return new RecentMessagesFragment();

            case 1:
                return new OthersFragment();

            default:
                return new RecentMessagesFragment();
        }
    }

    @Override
    public int getItemCount() {
        return Constants.ITEM_COUNT_VIEWPAGER_MESSAGES_OR_OTHERS;
    }
}
