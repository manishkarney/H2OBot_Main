package edu.scu.cs.robotics;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.scu.cs.robotics.communication.JoysticksInput;
import edu.scu.cs.robotics.communication.USBDataReceiver;
import edu.scu.cs.robotics.communication.USBJoysticksInputSender;
import edu.scu.cs.robotics.util.Utility;


/**
 * Created by manishkarney on 4/21/14.
 */
public class H2OBotApplication extends Application {

    private static H2OBotApplication singleton;

    private final String LOG_TAG = H2OBotApplication.class.getSimpleName();

    List<DeviceEntry> mAllUSBDevices = new ArrayList<DeviceEntry>();


    //Joystick related objects
    JoysticksInput mJoysticksInput;

    //USB Communication related objects
    USBJoysticksInputSender mUSBJoysticksInputSenderService;
    USBDataReceiver mUSBDataReceiver;
    private static final int JOYSTICK_SENDER_SERVICE_DELAY=0;
    private static final int JOYSTICK_SENDER_SERVICE_INTERVAL=500;


   Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message inputMessage) {
            // Gets the image task from the incoming Message object.
            String purposeValue =inputMessage.getData().getString(Utility.UI_HANDLER_THREAD_PURPOSE,"DEF");

            if(purposeValue.equals(Utility.UI_HANDLER_THREAD_MAKE_TOAST)) {
                Bundle bundle = inputMessage.getData();
                String msg = bundle.getString("Message", "DEF");
                boolean isLong = bundle.getBoolean("isLong", false);
                Toast.makeText(getApplicationContext(), msg, isLong ? Toast.LENGTH_SHORT : Toast.LENGTH_SHORT).show();
            }

        }
    };


    public H2OBotApplication getInstance(){
        return singleton;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;

        //TODO remove this
        Utility.uiHandler=mHandler;
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public List<DeviceEntry> getAllDrivers() {
        return mAllUSBDevices;
    }

    public void setUpDrivers() {

        Utility.makeToastShort(this,"Setting Up Drivers");

        UsbManager usbManager =  (UsbManager) getSystemService(Context.USB_SERVICE);
        for (final UsbDevice device : usbManager.getDeviceList().values()) {
            final List<UsbSerialDriver> drivers =
                    UsbSerialProber.probeSingleDevice(usbManager, device);
            Log.d(LOG_TAG, "Found usb device: " + device);
            if (drivers.isEmpty()) {
                Log.d(LOG_TAG, "  - No UsbSerialDriver available.");
                mAllUSBDevices.add(new DeviceEntry(device, null));
            } else {
                for (UsbSerialDriver driver : drivers) {
                    Log.d(LOG_TAG, "  + " + driver);
                    mAllUSBDevices.add(new DeviceEntry(device, driver));
                }
            }
        }

       if(!mAllUSBDevices.isEmpty()){
           setUpUSBTxRx();
           Utility.makeToastShort(this,"Driver retrieved.");

       }else{
           Log.e(LOG_TAG,"Failed to retrieve driver of the connected device.");
           Utility.makeToastShort(this,"Failed to retrieve driver of the connected device.");
       }
    }


    public void  setUpUSBTxRx(){
        //Only selecting the first device in the array.
        Log.d(LOG_TAG,"setUpUSBTxRx : allUSBDevices Size "+mAllUSBDevices.size() +" Driver:" +mAllUSBDevices.get(0).driver);
        Utility.makeToastShort(this,"Started Setting up USB TX RX");
        setUpDriverConfiguration(mAllUSBDevices.get(0));
        startGetUSBJoysticksInputSenderService();
        startUSBDataReceiverService();
    }

    public void startGetUSBJoysticksInputSenderService(){
        mUSBJoysticksInputSenderService = new USBJoysticksInputSender(mAllUSBDevices.get(0), mJoysticksInput);
        mUSBJoysticksInputSenderService.startSendingAtFixedRate(JOYSTICK_SENDER_SERVICE_DELAY,JOYSTICK_SENDER_SERVICE_INTERVAL);
    }
    public void startUSBDataReceiverService(){
        mUSBDataReceiver = new USBDataReceiver(this,mAllUSBDevices.get(0));

    }

    private void setUpDriverConfiguration(DeviceEntry entry){
        try {
        entry.driver.open();
        entry.driver.setParameters(115200, 8, UsbSerialDriver.STOPBITS_1, UsbSerialDriver.PARITY_NONE);
        } catch (IOException e) {
            Log.e(LOG_TAG,"Error setting parameters of driver.");
            e.printStackTrace();
            try {
                entry.driver.close();
            } catch (IOException e2) {
                // Ignore.
            }
            entry.driver = null;
            return;
        }
    }

    public void tearDownDrivers() {
        closeDriver();
    }

    private void closeDriver() {
//        if (mSerialIoManager != null) {
//            Log.i(TAG, "Stopping io manager ..");
//            mSerialIoManager.stop();
//            mSerialIoManager = null;
//        }

        mUSBJoysticksInputSenderService.cancelSenderTask();
        mUSBDataReceiver.cancelReceiverTask();

        if(mAllUSBDevices!=null&&mAllUSBDevices.get(0)!=null){
            try {
                mAllUSBDevices.get(0).driver.close();
            } catch (IOException e) {
                Log.w(LOG_TAG,"Problem when trying to close driver.");
                e.printStackTrace();
            }

            finally {
                mAllUSBDevices.clear();
                mUSBJoysticksInputSenderService.cleanUp();
                mUSBDataReceiver.cleanUp();
                mUSBDataReceiver=null;
                mUSBJoysticksInputSenderService=null;
            }
        }
    }


    /** Simple container for a UsbDevice and its driver. */
    public static class DeviceEntry {
        public UsbDevice device;
        public UsbSerialDriver driver;

        DeviceEntry(UsbDevice device, UsbSerialDriver driver) {
            this.device = device;
            this.driver = driver;
        }
    }

    public USBJoysticksInputSender getUSBJoysticksInputSenderService() {
        return mUSBJoysticksInputSenderService;
    }


    public JoysticksInput setUpAndGetJoysticksInput(){
        mJoysticksInput = JoysticksInput.getInstance();
        return mJoysticksInput;
    }

}
