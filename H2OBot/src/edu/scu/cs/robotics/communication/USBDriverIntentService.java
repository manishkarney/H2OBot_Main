package edu.scu.cs.robotics.communication;

import android.app.IntentService;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.util.Log;

import edu.scu.cs.robotics.H2OBotApplication;
import edu.scu.cs.robotics.util.Utility;

/**
 * Created by manishkarney on 4/21/14.
 */
public class USBDriverIntentService extends IntentService {

    public static final String H2OBOT_REGISTER_USB_DEVICE="edu.scu.cs.robotics.H2OBOT_REGISTER_USB_DEVICE";
    public static final String H2OBOT_DEREGISTER_USB_DEVICE="edu.scu.cs.robotics.H2OBOT_DEREGISTER_USB_DEVICE";
    private final String LOG_TAG = USBDriverIntentService.class.getSimpleName() ;


    public USBDriverIntentService(String name) {
        super(name);
    }

    public USBDriverIntentService() {
        super("USBDriverIntentService");
    }
    @Override
    protected void onHandleIntent(Intent intent) {


        if(intent.getAction().equals(H2OBOT_REGISTER_USB_DEVICE)){
           H2OBotApplication application= (H2OBotApplication)getApplicationContext();
            if(application!=null){
                try {
                    application.setUpDrivers();
                }
                catch (Exception e){
                    e.printStackTrace();
                    Utility.makeToastShort(getApplicationContext(),"Security Exception, Try again.");
                    System.exit(0);
                }
            }
            else {

                Utility.makeToastShort(this, LOG_TAG+"Application was null while generating drivers.");
                Log.e(LOG_TAG, "Application is null, cant generate drivers.");
            }
        }

        if(intent.getAction().equals(H2OBOT_DEREGISTER_USB_DEVICE)){
            H2OBotApplication application= (H2OBotApplication)getApplicationContext();
            if(application!=null){
                application.tearDownDrivers();
            }
            else {
                Utility.makeToastShort(this, LOG_TAG+"Application was null while removing drivers.");
                Log.e(LOG_TAG, "Application is null, cant remove drivers.");
            }
        }

    }
}
