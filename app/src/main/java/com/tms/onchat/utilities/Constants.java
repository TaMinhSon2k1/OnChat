package com.tms.onchat.utilities;

import java.util.HashMap;

public class Constants {
    public static final int ITEM_COUNT_VIEWPAGER_SIGN_IN_OR_UP = 2;
    public static final int ITEM_COUNT_VIEWPAGER_MESSAGES_OR_OTHERS = 2;
    public static final String REQUEST_ID_TOKEN = "665384453328-5sjpc6e42dhjbdfvmijqv1k0c08lo4ni.apps.googleusercontent.com";
    public static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    public static final String INTENT_USER_ID = "uid";
    public static final String INTENT_USER_EMAIL = "email";
    public static final String INTENT_USER = "user";

    public static final String KEY_SHAREDPREFERENCE = "chatsharepreference";
    public static final String KEY_IS_SIGN_IN = "issignin";

    public static final String KEY_COLLECTION_USERS = "users";
    public static final String KEY_USER_UID = "uid";
    public static final String KEY_USER_FIRST_NAME = "firstname";
    public static final String KEY_USER_LAST_NAME = "lastname";
    public static final String KEY_USER_BIRTHDAY = "birthday";
    public static final String KEY_USER_SEX = "sex";
    public static final String KEY_USER_IMAGE = "image";
    public static final String KEY_USER_EMAIL = "email";
    public static final String KEY_USER_DOCUMENT_ID = "documentid";
    public static final String KEY_USER_AVAILABLE = "available";
    public static final String KEY_USER_FCM = "fcm";

    public static final String KEY_COLLECTION_CHATS = "chats";
    public static final String KEY_CHATS_SENDER_ID = "senderid";
    public static final String KEY_CHATS_RECEIVER_ID = "receiverid";
    public static final String KEY_CHATS_DATE = "date";
    public static final String KEY_CHATS_MESSAGE = "message";

    public static final String KEY_COLLECTION_RECENTS = "recents";
    public static final String KEY_RECENTS_SENDER_NAME = "sendername";
    public static final String KEY_RECENTS_RECEIVER_NAME = "receivername";
    public static final String KEY_RECENTS_SENDER_IMAGE = "senderimage";
    public static final String KEY_RECENTS_RECEIVER_IMAGE = "receiverimage";
    public static final String KEY_RECENTS_LAST_MESSAGE = "lastmessage";
    public static final String KEY_RECENTS_TIME = "time";
    public static final String KEY_RECENTS_SENDER_ID = "senderid";
    public static final String KEY_RECENTS_RECEIVER_ID = "receiverid";

    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";

    public static HashMap<String,String> remoteMsgHeader = null;
    public static final HashMap<String, String> getRemoteMsgHeader() {
        if(remoteMsgHeader == null) {
            remoteMsgHeader = new HashMap<>();
            remoteMsgHeader.put(
                    REMOTE_MSG_AUTHORIZATION,
                    "key=AAAAmuwBBNA:APA91bH4xko7IGIxnQOI4Mr5KIOYugxYkgtCfg8T8oo0Xi-ibvQLeOBawEHrcfzpyd7UckVXairIeYADuSRpvNnGG90hvD_SUlH0j-M-tbzWJILZuxgiAAVOFLN1Ao2s2jayePhd6tZJ"
            );
            remoteMsgHeader.put(
                    REMOTE_MSG_CONTENT_TYPE,
                    "application/json"
            );
        }
        return remoteMsgHeader;
    }
}
