package com.thomasjayconsulting.opensmsgateway;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;

public class MessageBroadcastReceiver extends BroadcastReceiver {

    // Note: You will NEVER get any SMS Messages received if you have the default Google SMS Client, it eats all SMS Messages.
    // You need to disable it or install another client and make it the default like "textra" then you will receive messages.

    private static MessageListenerInterface mListener;

    @Override
    public void onReceive(Context context, Intent intent) {

        String fullMessage = "";

        // Note: SMS Message that are more then 140/160 chars may come as multiple parts so they need to be concatenated together.

        Bundle data = intent.getExtras();
        Object[] pdus = (Object[]) data.get("pdus");


        String sender = "";
        SmsMessage[] smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
        for (SmsMessage aMessage : smsMessages) {
            sender = aMessage.getOriginatingAddress();
            fullMessage = fullMessage + aMessage.getMessageBody();
        }

        mListener.messageReceived(sender, fullMessage);

    }


    public static void bindListener(MessageListenerInterface listener) {
        mListener = listener;
    }


}