package edu.scu.cs.robotics;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

import java.util.HashMap;

import edu.scu.cs.robotics.customviews.JoystickPane;
import edu.scu.cs.robotics.customviews.StatisticsFrameElement;
import edu.scu.cs.robotics.customviews.StatisticsPaneManager;
import edu.scu.cs.robotics.util.SystemUiHider;
import edu.scu.cs.robotics.util.Utility;
import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class H2OBotStartActivity extends Activity implements MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnVideoSizeChangedListener, SurfaceHolder.Callback {

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = false;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;


    //Constants
    public static final String RECEIVE_USB_DATA_ACTION = "edu.scu.cs.robotics.RECEIVE_USB_DATA_ACTION";
    public static final String RECEIVE_USB_DATA = "edu.scu.cs.robotics.RECEIVE_USB_DATA";

    public static final String LOG_TAG = H2OBotStartActivity.class.getSimpleName();



    //Gesture recogniser
    GestureDetector gDetector = null;
    /**
     * Statistics Pane Manager that is responsible to maintain individual
     * statistics elements
     */

    private StatisticsPaneManager mStatisticsPaneManager = null;
    private JoystickPane mJoystickPane = null;

    private LocalBroadcastManager mLocalBroadcastManager;
    private BroadcastReceiver bReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(RECEIVE_USB_DATA_ACTION)){
                //TODO update ui elements

                String data=intent.getStringExtra(RECEIVE_USB_DATA);
                Log.d(LOG_TAG,"Received data."+ data);
                //Test update
                HashMap<String,StatisticsFrameElement> elements = mStatisticsPaneManager.getAllStatisticsElements();
                StatisticsFrameElement joystick1Element= elements.get("JoyStick1");
                StatisticsFrameElement joystick2Element= elements.get("JoyStick2");
                //Not safe!
                String[] parts = data.split("%");
                String part1 = parts[0];
                String part2 = parts[1];
                joystick1Element.setValue(part1);
                joystick2Element.setValue(part2);

            }
        }
    };






	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_h2o_bot_start);

		final View controlsView = findViewById(R.id.fullscreen_content_controls);
		final View contentView = findViewById(R.id.fullscreen_content);

		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		mSystemUiHider = SystemUiHider.getInstance(this, contentView,
				HIDER_FLAGS);
		mSystemUiHider.setup();
		mSystemUiHider
				.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
					// Cached values.
					int mControlsHeight;
					int mShortAnimTime;

					@Override
					@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
					public void onVisibilityChange(boolean visible) {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
							// If the ViewPropertyAnimator API is available
							// (Honeycomb MR2 and later), use it to animate the
							// in-layout UI controls at the bottom of the
							// screen.
							if (mControlsHeight == 0) {
								mControlsHeight = controlsView.getHeight();
							}
							if (mShortAnimTime == 0) {
								mShortAnimTime = getResources().getInteger(
										android.R.integer.config_shortAnimTime);
							}
							controlsView
									.animate()
									.translationY(visible ? 0 : mControlsHeight)
									.setDuration(mShortAnimTime);
						} else {
							// If the ViewPropertyAnimator APIs aren't
							// available, simply show or hide the in-layout UI
							// controls.
							controlsView.setVisibility(visible ? View.VISIBLE
									: View.GONE);
						}

						if (visible && AUTO_HIDE) {
							// Schedule a hide().
							delayedHide(AUTO_HIDE_DELAY_MILLIS);
						}
					}
				});

		// Set up the user interaction to manually show or hide the system UI.
		contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
            }
        });

		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// // while interacting with the UI.
		// findViewById(R.id.dummy_button).setOnTouchListener(
		// mDelayHideTouchListener);


		// Set up statistics pane

		setUpStatisticsPane();
		setUpJoystickPane();


        //Registering receiver
        registerUSBDataReceiver();
	}


//    private static final String VIDEO_URL="http://www.law.duke.edu/cspd/contest/finalists/viewentry.php?file=docandyou";
    private static final String VIDEO_URL="rtsp://192.168.2.10:8554/";
    private int mVideoWidth;
    private int mVideoHeight;
    private MediaPlayer mMediaPlayer;
    private SurfaceView mPreview;
    private SurfaceHolder holder;
    private boolean mIsVideoSizeKnown = false;
    private boolean mIsVideoReadyToBePlayed = false;
    private void setUpVideo(){
//        setContentView(R.layout.activity_main);
        if (!LibsChecker.checkVitamioLibs(this)) {
            Utility.makeToastShort(this,"Video lib loading error.");
            return;
        }
         mPreview = (SurfaceView) findViewById(R.id.video_surface_view);
         holder = mPreview.getHolder();
        holder.addCallback(this);
        holder.setFormat(PixelFormat.RGBA_8888);
        Bundle extras = getIntent().getExtras();



//        VideoView videoView = (VideoView) findViewById(R.id.video_view);
//        MediaController mediaController = new MediaController(this);
//        mediaController.setAnchorView(videoView);
//        mediaController.setMediaPlayer(videoView);
//        Uri video = Uri.parse(VIDEO_URL);
//        videoView.setMediaController(mediaController);
//        videoView.setVideoURI(video);
//        videoView.start();
    }


    private void playVideo(){
        doCleanUp();
        try {
            Utility.makeToastShort(this,"Playing from URL "+VIDEO_URL);
            mMediaPlayer = new MediaPlayer(this);
            mMediaPlayer.setDataSource(VIDEO_URL);
            mMediaPlayer.setDisplay(holder);
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setOnBufferingUpdateListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnVideoSizeChangedListener(this);
            setVolumeControlStream(AudioManager.STREAM_MUSIC);

        } catch (Exception e) {
            Log.e(LOG_TAG, "error: " + e.getMessage(), e);
        }

    }
    public void registerUSBDataReceiver(){
        //Registering Local Broadcast Receiver for data over USB
        if(mLocalBroadcastManager==null) {
            mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        }
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(RECEIVE_USB_DATA_ACTION);
            mLocalBroadcastManager.registerReceiver(bReceiver, intentFilter);
    }

    public void unregisterUSBDataReceiver(){
       mLocalBroadcastManager.unregisterReceiver(bReceiver);
    }


	private void setUpStatisticsPane() {
		mStatisticsPaneManager = new StatisticsPaneManager(this);
	}

	private void setUpJoystickPane() {
		mJoystickPane = new JoystickPane(this);

	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
        setUpVideo();
//        gDetector=new GestureDetector(this,new GestureDetector.OnGestureListener(){
//
//            @Override
//            public boolean onDown(MotionEvent motionEvent) {
//                System.out.println("onDown DETECTED");
//                return false;
//            }
//
//            @Override
//            public void onShowPress(MotionEvent motionEvent) {
//
//            }
//
//            @Override
//            public boolean onSingleTapUp(MotionEvent motionEvent) {
//                return false;
//            }
//
//            @Override
//            public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
//                return false;
//            }
//
//            @Override
//            public void onLongPress(MotionEvent motionEvent) {
//
//            }
//
//            @Override
//            public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
//                System.out.println("FLING DETECTED");
//                return false;
//            }
//        });

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		delayedHide(100);
	}
//    @Override
//    public boolean onTouchEvent(MotionEvent me) {
//        return gDetector.onTouchEvent(me);
//    }
	// ADDED



	private void setActionBarVisible(boolean visibility) {
		ActionBar actionBar = getActionBar();
		if (!visibility)
			actionBar.hide();
		else
			actionBar.show();
	}

	// END ADDED
//

	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the
	 * system UI. This is to prevent the jarring behavior of controls going away
	 * while interacting with activity UI.
	 */
	View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {

			if (AUTO_HIDE) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
			return false;
		}
	};

	Handler mHideHandler = new Handler();
	Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			mSystemUiHider.hide();
		}
	};

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}

    @Override
    protected void onPause() {
        super.onPause();
        unregisterUSBDataReceiver();

        //Video
        releaseMediaPlayer();
        doCleanUp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterUSBDataReceiver();

        //Video
        releaseMediaPlayer();
        doCleanUp();
    }



    //Video call back methods



    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.d(LOG_TAG, "surfaceCreated called");
        playVideo();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {

        Log.d(LOG_TAG, "surfaceChanged called");

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.d(LOG_TAG, "surfaceDestroyed called");
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d(LOG_TAG, "onPrepared called");
        mIsVideoReadyToBePlayed = true;
        if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
            startVideoPlayback();
        }

    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        Log.v(LOG_TAG, "onVideoSizeChanged called");
        if (width == 0 || height == 0) {
            Log.e(LOG_TAG, "invalid video width(" + width + ") or height(" + height + ")");
            return;
        }
        mIsVideoSizeKnown = true;
        mVideoWidth = width;
        mVideoHeight = height;
        if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
            startVideoPlayback();
        }

    }


    private void releaseMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    private void doCleanUp() {
        mVideoWidth = 0;
        mVideoHeight = 0;
        mIsVideoReadyToBePlayed = false;
        mIsVideoSizeKnown = false;
    }

    private void startVideoPlayback() {
        Log.v(LOG_TAG, "startVideoPlayback");
        holder.setFixedSize(mVideoWidth, mVideoHeight);
        mMediaPlayer.start();
    }




}
