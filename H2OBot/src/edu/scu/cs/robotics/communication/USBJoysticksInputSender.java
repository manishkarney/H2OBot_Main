package edu.scu.cs.robotics.communication;

import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialDriver;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import edu.scu.cs.robotics.H2OBotApplication;
import edu.scu.cs.robotics.util.Utility;

/**
 * Wrapper class that sends joystick readings.
 * Created by manishkarney on 4/20/14.
 */
public class USBJoysticksInputSender{


    private ScheduledExecutorService mScheduler;
    private H2OBotApplication.DeviceEntry mDeviceEntry;
    private JoysticksInput mJoysticksInput;
    private ScheduledFuture<?> mFuture;
    //Constants
    private static final int DEFAULT_WRITE_TIMEOUT = 100;
    private static final String LOG_TAG = USBJoysticksInputSender.class.getSimpleName();

    private static final boolean DEBUG=false;


    public USBJoysticksInputSender(H2OBotApplication.DeviceEntry mDeviceEntry, JoysticksInput mJoysticksInput){
        mScheduler= Executors.newSingleThreadScheduledExecutor();
        this.mDeviceEntry = mDeviceEntry;
        this.mJoysticksInput=mJoysticksInput;
    }

    public void startSendingAtFixedRate(int delay, int rate){

          Log.d(LOG_TAG,"Starting joystick reading sender service.");
          mFuture= mScheduler.scheduleAtFixedRate(new Runnable() {
              @Override
              public void run() {


//               //Clear buffer
//                  try {
//                      //TODO this shouldn't be there
//                      mDeviceEntry.driver.purgeHwBuffers(true,true);
//                  } catch (IOException e) {
//
//                      Log.e(LOG_TAG,"Failed to purge hw buffers (RX: false, TX: true)");
//                      e.printStackTrace();
//                  }
                  //Get latest Sample
                  if(DEBUG)
                  Log.d(LOG_TAG,"Attempting read ");
                 String msg= mJoysticksInput.getReadingsData();
                  if(DEBUG)
                  Log.d(LOG_TAG,"Writing "+msg);
              //Write the data
                  try {
                      synchronized (this) {
                          mDeviceEntry.driver.write(msg.getBytes(Charset.forName("US-ASCII")), DEFAULT_WRITE_TIMEOUT);
                      }
                      if(DEBUG)
                      Log.d(LOG_TAG,"Done Writing "+msg);
                  } catch (IOException e) {
                      Log.e(LOG_TAG,"WRITE ERROR: Write failed while sending readings.");
                      e.printStackTrace();
                  }

              }
          },delay,rate, TimeUnit.MILLISECONDS);


    }

    public void cancelSenderTask(){
            mScheduler.shutdown();
    }

    public void cleanUp(){
        if(!mScheduler.isShutdown())
            mScheduler.shutdown();
        mDeviceEntry=null;
        mScheduler=null;
        mJoysticksInput=null;
        mFuture=null;
    }

    public ScheduledFuture<?> getScheduledFuture() {
        return mFuture;
    }


}
