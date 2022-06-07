package com.tms.onchat.adapers;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.tms.onchat.fragments.SignInFragment;
import com.tms.onchat.fragments.SignUpFragment;
import com.tms.onchat.listeners.SignInOrUpListener;
import com.tms.onchat.utilities.Constants;

public class ViewPagerSignInOrSUpAdapter extends FragmentStateAdapter {

    private SignInOrUpListener signInOrUpListener;

    public ViewPagerSignInOrSUpAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        this.signInOrUpListener = (SignInOrUpListener) fragmentActivity;
    }

    /** Set fragment show
     * if position = 0 => fragment is sign in
     * if position = 1 => fragment is sign up
     * */
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch(position) {
            case 0:
                return new SignInFragment(signInOrUpListener);

            case 1:
                return new SignUpFragment(signInOrUpListener);

            default:
                return new SignInFragment(signInOrUpListener);
        }
    }

    @Override
    public int getItemCount() {
        return Constants.ITEM_COUNT_VIEWPAGER_SIGN_IN_OR_UP;
    }
}
