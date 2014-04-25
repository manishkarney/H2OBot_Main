package edu.scu.cs.robotics;

import java.util.HashMap;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class StatisticsPaneManager {

	private H2OBotStartActivity mActivity = null;
	private FrameLayout mStatsPaneLeft = null;
    private FrameLayout mStatsPaneRight = null;
	private ScrollView mScrollViewLeft = null;
    private ScrollView mScrollViewRight = null;
	private LinearLayout mLinearLayoutLeft = null;
    private LinearLayout mLinearLayoutRight = null;
	private HashMap<String, StatisticsFrameElement> mStatistics = new HashMap<String, StatisticsFrameElement>();

	// View for displaying statistics
	String STATISTICS_NAMES[] = { "Temparature", "Depth", "Voltage",
			"JoyStick1", "JoyStick2", "JoyStick3" };

	public StatisticsPaneManager(H2OBotStartActivity activity) {
		mActivity = activity;
		init();
	}

	private void init() {
		mStatsPaneLeft = (FrameLayout) mActivity.findViewById(R.id.stats_pane_left);
        mStatsPaneRight = (FrameLayout) mActivity.findViewById(R.id.stats_pane_right);
		mScrollViewLeft = (ScrollView) mActivity
				.findViewById(R.id.stats_scroll_view_left);
        mScrollViewLeft = (ScrollView) mActivity
                .findViewById(R.id.stats_scroll_view_right);
		mLinearLayoutLeft = (LinearLayout) mActivity
				.findViewById(R.id.stats_content_view_left);
        mLinearLayoutRight = (LinearLayout) mActivity
                .findViewById(R.id.stats_content_view_right);
        StatisticsFrameElement element =null;
        String name=null;
		// adding statistics elements, First Half on left, rest on right.
        for(int i=0;i<STATISTICS_NAMES.length; i++) {
            name = STATISTICS_NAMES[i];
            if (i < STATISTICS_NAMES.length / 2) {
                element = new StatisticsFrameElement(
                        mActivity, mLinearLayoutLeft, name);

            } else {
                element = new StatisticsFrameElement(
                        mActivity, mLinearLayoutRight, name);
            }
            element.setName(name);
            mStatistics.put(name, element);
        }
	}

//	void addStatisticsElement(StatisticsFrameElement element) {
//		mLinearLayout.addView(element.getStatisticsElement());
//	}

//	boolean removeStatisticsElement(StatisticsFrameElement element) {
//		boolean isChild = (element.getStatisticsElement().getParent() == mLinearLayout);
//		mLinearLayout.removeView(element.getStatisticsElement());
//		return isChild;
//	}

	HashMap<String, StatisticsFrameElement> getAllStatisticsElements() {
		return mStatistics;
	}


}
