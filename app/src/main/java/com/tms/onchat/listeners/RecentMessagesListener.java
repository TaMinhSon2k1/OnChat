package com.tms.onchat.listeners;

import com.tms.onchat.models.User;

public interface RecentMessagesListener {
    void sendMessageOther(User user);
    void showDialogWait();
}
