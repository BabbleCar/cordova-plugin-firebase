package org.apache.cordova.firebase;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.messaging.RemoteMessage;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;

public class FirebasePluginMessageReceiverManager {

    private static HashSet<String> receivers = new HashSet<>();

    private static String KEY_OBJ_MNG = "key_obj_mng";
    private static String PREFS_MNG_USER = "prefs_mng_user";

    /**
     * Call when Receive a Meesage
     *
     * @param remoteMessage
     * @param context
     * @return
     */
    public static boolean onMessageReceived(RemoteMessage remoteMessage, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_MNG_USER, Context.MODE_PRIVATE);
        receivers = (HashSet<String>) prefs.getStringSet(KEY_OBJ_MNG, new HashSet<String>());

        boolean handled = false;
        for (String receiver : receivers) {
            try {
                Class n_class = Class.forName(receiver);
                FirebasePluginMessageReceiver obj =
                        (FirebasePluginMessageReceiver) n_class.getConstructor(new Class[]{Context.class}).newInstance(context);

                boolean wasHandled = obj.onMessageReceived(remoteMessage);
                if (wasHandled) {
                    handled = true;
                }
            } catch (InstantiationException |
                    NoSuchMethodException | InvocationTargetException | IllegalAccessException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        return handled;
    }

    /**
     * Registre receiver
     *
     * @param receiver
     * @param context
     * @return
     */
    public static boolean register(String receiver, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_MNG_USER, Context.MODE_PRIVATE);
        receivers = (HashSet<String>) prefs.getStringSet(KEY_OBJ_MNG, new HashSet<String>());

        boolean is_present = false;
        for (String rec : receivers) {
            if(receiver.equals(rec)) {
                is_present = true;
                break;
            }
        }

        if(!is_present) {
            receivers.add(receiver);
            SharedPreferences.Editor ed = prefs.edit();
            ed.putStringSet(KEY_OBJ_MNG, receivers);
            ed.apply();
        }

        return !is_present;
    }
}
