package edu.scu.cs.robotics;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import edu.scu.cs.robotics.communication.USBDriverIntentService;
import edu.scu.cs.robotics.util.Utility;

/**
 * Created by manishkarney on 4/21/14.
 */
public class USBDeviceBroadcastReceiver extends BroadcastReceiver{
   private static final String USB_DEVICE_ATTACHED= "android.hardware.usb.action.USB_DEVICE_ATTACHED";
    private static final String USB_DEVICE_DETACHED= "android.hardware.usb.action.USB_DEVICE_DETACHED";
    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(USB_DEVICE_ATTACHED)){

            Utility.makeToastShort(context,"USB Device Attached");


            //Start intent service to create drivers.
           Intent serviceIntent = new Intent(context.getApplicationContext(), USBDriverIntentService.class);
            serviceIntent.setAction(USBDriverIntentService.H2OBOT_REGISTER_USB_DEVICE);
            context.startService(serviceIntent);

            //Send Local intent about USB state
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(UsbManager.ACTION_USB_DEVICE_ATTACHED));

        }else if(intent.getAction().equals(USB_DEVICE_DETACHED)){
//TODO Handle this appropriately.
            System.exit(1);
            Utility.makeToastShort(context,"USB Device Detached");
            //Start intent service to create drivers.
            Intent serviceIntent = new Intent(context.getApplicationContext(), USBDriverIntentService.class);
            serviceIntent.setAction(USBDriverIntentService.H2OBOT_DEREGISTER_USB_DEVICE);
            context.startService(serviceIntent);

            //Send Local intent about USB state
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(UsbManager.ACTION_USB_DEVICE_DETACHED));

        }

    }
     }


