package edu.scu.cs.robotics.util;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;



/**
 * Created by manishkarney on 4/21/14.
 */
public class Utility {

    public static Handler uiHandler;
    private static final String LOG_TAG= Utility.class.getSimpleName();

    //Constants
    public static final String UI_HANDLER_THREAD_PURPOSE="UI_HANDLER_THREAD_PURPOSE";
    public static final String UI_HANDLER_THREAD_MAKE_TOAST="UI_HANDLER_THREAD_MAKE_TOAST";

    public static void makeToastLong(Context context,String message){
        Bundle newBundle = new Bundle();
        newBundle.putString(UI_HANDLER_THREAD_PURPOSE,UI_HANDLER_THREAD_MAKE_TOAST);
        newBundle.putBoolean("isLong",true);
        newBundle.putString("Message",message);
        Message newMsg= new Message();
        newMsg.setData(newBundle);
        uiHandler.sendMessage(newMsg);
    }
    public static void makeToastShort(Context context,String message){
        Bundle newBundle = new Bundle();
        newBundle.putString(UI_HANDLER_THREAD_PURPOSE,UI_HANDLER_THREAD_MAKE_TOAST);
        newBundle.putBoolean("isLong",false);
        newBundle.putString("Message",message);
        Message newMsg= new Message();
        newMsg.setData(newBundle);
        uiHandler.sendMessage(newMsg);
    }

    public static void runOnUIThread(Runnable runnable){
        if(runnable==null){
            Log.w(LOG_TAG,"Action to be performed on UI thread is null");
        }
        uiHandler.post(runnable);
    }

    public static void sendMessageToUIHandler(String purpose,String msg){
        Bundle newBundle = new Bundle();
        newBundle.putString(purpose,msg);
        Message newMsg= new Message();
        newMsg.setData(newBundle);
        uiHandler.sendMessage(newMsg);

    }
}
