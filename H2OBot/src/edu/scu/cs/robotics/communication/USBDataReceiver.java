package edu.scu.cs.robotics.communication;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.scu.cs.robotics.H2OBotApplication;
import edu.scu.cs.robotics.H2OBotApplication.DeviceEntry;
import edu.scu.cs.robotics.H2OBotStartActivity;
import edu.scu.cs.robotics.util.Utility;

/**
 * Created by manishkarney on 4/22/14.
 */
public class USBDataReceiver {
    private  ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private SerialInputOutputManager mSerialIoManager;
    private static final String LOG_TAG = USBDataReceiver.class.getSimpleName();
    private DeviceEntry mDeviceEntry;
    private Context mContext;

    //Constants
    private static final boolean DEBUG=false;



    public USBDataReceiver(Context mContext, DeviceEntry deviceEntry){
        this.mDeviceEntry=deviceEntry;
        this.mContext=mContext;
        mSerialIoManager = new SerialInputOutputManager(deviceEntry.driver,mListener);
        onDeviceStateChange();
    }


    private SerialInputOutputManager.Listener mListener =
            new SerialInputOutputManager.Listener() {
                final String startDelimiter ="{";
                final String endDelimiter="}";
                boolean expectingFragment = false;
                final StringBuilder mBuilder = new StringBuilder();
                @Override
                public void onRunError(Exception e) {

                    Log.w(LOG_TAG, "Serial IO manager stopped");
                }

                @Override
                public void onNewData(final byte[] data) {
                    if(DEBUG)
                    Log.d(LOG_TAG,"New data received, length:"+data.length);
                    String stringFromBytes = new String (data);

                    //TODO Data received is often broken down into multiple calls to this method, getting around this by using delimiters. ASSUMPTION:All data is received in two or less calls. Might have to fix this for other scenarios.
                   //
                    if(expectingFragment){
                        mBuilder.append(stringFromBytes);
                        stringFromBytes=mBuilder.toString();
                        mBuilder.setLength(0);
                        expectingFragment=false;
                        Intent intentToUpdateUI = new Intent(H2OBotStartActivity.RECEIVE_USB_DATA_ACTION);
                        intentToUpdateUI.putExtra(H2OBotStartActivity.RECEIVE_USB_DATA, stringFromBytes);
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intentToUpdateUI);
                    }

                    if(stringFromBytes.contains(startDelimiter)&&!stringFromBytes.contains(endDelimiter)){
                        //String not complete
                        expectingFragment=true;
                        mBuilder.append(stringFromBytes);
                        stringFromBytes=null;
                    }


                }
            };

    private void stopIoManager() {
        if (mSerialIoManager != null) {
            Log.i(LOG_TAG, "Stopping io manager ..");
            mSerialIoManager.stop();
            mSerialIoManager = null;
        }
    }

    private void startIoManager() {
        if (mDeviceEntry.driver != null) {
            Log.i(LOG_TAG, "Starting io manager ..");
            mSerialIoManager = new SerialInputOutputManager(mDeviceEntry.driver, mListener);
            mExecutor.submit(mSerialIoManager);
        }
    }

    private void onDeviceStateChange() {
        stopIoManager();
        startIoManager();
    }

    public void cancelReceiverTask(){
        mExecutor.shutdown();
    }

    public void cleanUp(){
        stopIoManager();
        if(!mExecutor.isShutdown())
            mExecutor.shutdown();
        mDeviceEntry=null;
        mExecutor=null;
        mContext=null;
    }


}
