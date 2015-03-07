package com.newsmy.car.airconditioner;

import android.graphics.drawable.LevelListDrawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import com.android.internal.car.can.AirConditioning;

/**
 * Created by zybo on 8/1/14.
 */
public class AirConditionUIController {
    private static final String TAG = "AirConditionUIController";
    private static final boolean D = false;
    private static final float TEMP_MIN = 10.0f;
    private static final float TEMP_MAX = 35.0f;
    private final View mRootView;
    private ImageView mRearLockImageView;
    private ImageView mACView;
    private ImageView mAutoImageView;
    private ImageView mACMaxImageView;
    private ImageView mFrontMaxImageView;
    private ImageView mBackImageView;
    private ImageView mDualImageView;
    private ImageView mCycleImageView;
    private ImageView mLeftWindImageView;
    private ImageView mRightWindImageView;
    private ImageView mLeftSeatImageView;
    private ImageView mRightSeatImageView;
    private ImageView mLeftWindowWindImageView;
    private ImageView mRightWindowWindImageView;
    private ImageView mLeftSeatHeatingImageView;
    private ImageView mRightSeatHeatingImageView;
    private TextView mLeftTempTextView;
    private TextView mRightTempTextView;
    private LevelListDrawable mCycleLevelListDrawable;
    private LevelListDrawable mLeftWindDrawable;
    private LevelListDrawable mRightWindDrawable;
    private LevelListDrawable mLeftSeatHeatingDrawable;
    private LevelListDrawable mRightSeatHeatingDrawable;
    private List<LevelListDrawable> mWindIndicatorList;
    private String mTempUnit;
    private ViewGroup mTopContainer;
    private ViewGroup mMiddleContainer;
    private ViewGroup mBottomContainer;
    private TextView mPowerOffText;

    public AirConditionUIController(final View rootView) {
        mRootView = rootView;
//        mTempUnit = rootView.getResources().getString(R.string.temp_unit);
    }

    private static int getWindDirectionLevel(final boolean windUp, final boolean windHorization, final boolean windDown) {
        int level = -1;
        if (windUp && !windHorization && !windDown) {
            level = 1;
        }
        if (!windUp && windHorization && !windDown) {
            level = 2;
        }
        if (!windUp && !windHorization && windDown) {
            level = 3;
        }
        if (windUp && windHorization && !windDown) {
            level = 4;
        }
        if (windUp && !windHorization && windDown) {
            level = 5;
        }
        if (!windUp && windHorization && windDown) {
            level = 8;
        }
        if (windUp && windHorization && windDown) {
            level = 9;
        }
        if (!windUp && !windHorization && !windDown) {
            level = -1;
        }
        return level;
    }

    private void setupViews() {
        mFrontMaxImageView.setVisibility(View.GONE);
        mACMaxImageView.setVisibility(View.GONE);
        mBackImageView.setVisibility(View.GONE);
        mDualImageView.setVisibility(View.GONE);
        mACView.setVisibility(View.GONE);
        mRearLockImageView.setVisibility(View.GONE);
        mCycleLevelListDrawable.setLevel(AirConditioning.EXTERNAL_CYCLE);
        mLeftWindImageView.setVisibility(View.INVISIBLE);
        mRightWindImageView.setVisibility(View.INVISIBLE);
        mLeftSeatHeatingImageView.setVisibility(View.INVISIBLE);
        mRightSeatHeatingImageView.setVisibility(View.INVISIBLE);
        for (int i = 0; i < mWindIndicatorList.size(); i++) {
            mWindIndicatorList.get(i).setLevel(0);
        }
        mLeftWindowWindImageView.setVisibility(View.INVISIBLE);
        mRightWindowWindImageView.setVisibility(View.INVISIBLE);
    }

    public void initViews(final AirConditioning airConditioning) {
        mRootView.setSystemUiVisibility(8);
        mTopContainer = (ViewGroup) mRootView.findViewById(R.id.top_container);
        mPowerOffText = (TextView) mRootView.findViewById(R.id.power_off_text);
        mACView = (ImageView) mRootView.findViewById(R.id.image_top_ac);
        mMiddleContainer = (ViewGroup) mRootView.findViewById(R.id.middle_container);
        mBottomContainer = (ViewGroup) mRootView.findViewById(R.id.bottom_container);
        mAutoImageView = (ImageView) mRootView.findViewById(R.id.image_top_auto);
        mRearLockImageView = (ImageView) mRootView.findViewById(R.id.image_top_rearlock);
        mFrontMaxImageView = (ImageView) mRootView.findViewById(R.id.image_top_front_max);
        mACMaxImageView = (ImageView) mRootView.findViewById(R.id.image_top_acmax);
        mBackImageView = (ImageView) mRootView.findViewById(R.id.image_top_back);
        mDualImageView = (ImageView) mRootView.findViewById(R.id.image_top_dual);
        mCycleImageView = (ImageView) mRootView.findViewById(R.id.image_top_cycle);
        mCycleLevelListDrawable = (LevelListDrawable) mCycleImageView.getDrawable();
        mLeftWindImageView = (ImageView) mRootView.findViewById(R.id.image_middle_wind_left_horizontal);
        mLeftWindDrawable = (LevelListDrawable) mLeftWindImageView.getDrawable();
        mRightWindImageView = (ImageView) mRootView.findViewById(R.id.image_middle_wind_right_horizontal);
        mRightWindDrawable = (LevelListDrawable) mRightWindImageView.getDrawable();
        mLeftSeatImageView = (ImageView) mRootView.findViewById(R.id.image_middle_seat_left_heating);
        mRightSeatImageView = (ImageView) mRootView.findViewById(R.id.image_middle_seat_right_heating);
        mLeftSeatHeatingImageView = (ImageView) mRootView.findViewById(R.id.image_middle_seat_left_heating);
        mLeftSeatHeatingDrawable = (LevelListDrawable) mLeftSeatHeatingImageView.getDrawable();
        mRightSeatHeatingImageView = (ImageView) mRootView.findViewById(R.id.image_middle_seat_right_heating);
        mRightSeatHeatingDrawable = (LevelListDrawable) mRightSeatHeatingImageView.getDrawable();
        mLeftWindowWindImageView = (ImageView) mRootView.findViewById(R.id.image_middle_window_left_wind);
        mRightWindowWindImageView = (ImageView) mRootView.findViewById(R.id.image_middle_window_right_wind);
        mWindIndicatorList = new ArrayList<LevelListDrawable>();
        mWindIndicatorList.add((LevelListDrawable) ((ImageView) mRootView.findViewById(R.id.image_bottom_wind1)).getDrawable());
        mWindIndicatorList.add((LevelListDrawable) ((ImageView) mRootView.findViewById(R.id.image_bottom_wind2)).getDrawable());
        mWindIndicatorList.add((LevelListDrawable) ((ImageView) mRootView.findViewById(R.id.image_bottom_wind3)).getDrawable());
        mWindIndicatorList.add((LevelListDrawable) ((ImageView) mRootView.findViewById(R.id.image_bottom_wind4)).getDrawable());
        mWindIndicatorList.add((LevelListDrawable) ((ImageView) mRootView.findViewById(R.id.image_bottom_wind5)).getDrawable());
        mWindIndicatorList.add((LevelListDrawable) ((ImageView) mRootView.findViewById(R.id.image_bottom_wind6)).getDrawable());
        mWindIndicatorList.add((LevelListDrawable) ((ImageView) mRootView.findViewById(R.id.image_bottom_wind7)).getDrawable());
        mLeftTempTextView = (TextView) mRootView.findViewById(R.id.text_bottom_left_temp);
        mRightTempTextView = (TextView) mRootView.findViewById(R.id.text_bottom_right_temp);
        setupViews();
    }

    private void hideAllViews(final ViewGroup viewGroup) {
        final int count = viewGroup.getChildCount();
        for (int i = 0; i < count; i++) {
            viewGroup.getChildAt(i).setVisibility(View.INVISIBLE);
        }
    }
    
    private void showAllViews(final ViewGroup viewGroup) {
        final int count = viewGroup.getChildCount();
        for (int i = 0; i < count; i++) {
            viewGroup.getChildAt(i).setVisibility(View.VISIBLE);
        }
    }

    public void updateViews(final AirConditioning airConditioning) {
        if (D)
            Log.d(TAG, "update view");
        final boolean ifPowerOn = airConditioning.getAirConditioningSwitch();
        if (!ifPowerOn) {
            hideAllViews(mTopContainer);
            hideAllViews(mMiddleContainer);
            hideAllViews(mBottomContainer);
            mPowerOffText.setVisibility(View.VISIBLE);
            return;
        } else {
            showAllViews(mTopContainer);
            showAllViews(mMiddleContainer);
            showAllViews(mBottomContainer);
            mLeftWindowWindImageView.setVisibility(View.INVISIBLE);
            mRightWindowWindImageView.setVisibility(View.INVISIBLE);
            mPowerOffText.setVisibility(View.INVISIBLE);
        }

        if (airConditioning.getAUTOSoftWindSiwtch() ||
                airConditioning.getAUTOStrongWindSwitch()
                || airConditioning.getAUTOSwitch()) {
            mAutoImageView.setVisibility(View.VISIBLE);
        } else {
            mAutoImageView.setVisibility(View.GONE);
        }

        mRearLockImageView.setVisibility(airConditioning.getREARLockSwitch() ? View.VISIBLE : View.GONE);
        mFrontMaxImageView.setVisibility(airConditioning.getMAXFORNTSwitch() ? View.VISIBLE : View.GONE);
        mACMaxImageView.setVisibility(airConditioning.getACMAXSwitch() ? View.VISIBLE : View.GONE);
        mBackImageView.setVisibility(airConditioning.getREARSwitch() ? View.VISIBLE : View.GONE);
        mACView.setVisibility(airConditioning.getACSwitch() ? View.VISIBLE : View.GONE);
        mDualImageView.setVisibility(airConditioning.getDUALSwitch() ? View.VISIBLE : View.GONE);
        mCycleLevelListDrawable.setLevel(airConditioning.getCycle());
//        final boolean ifWindUp = airConditioning.getUpWindSwitch();
//        final boolean ifWindHorization = airConditioning.getHorizontalWindSwitch();
//        final boolean ifWindDown = airConditioning.getDownWindSwitch();
        // TODO : need check!
        final int windDirectionLevel = airConditioning.getWindDirection();
//        final int windDirectionLevel = getWindDirectionLevel(ifWindUp, ifWindHorization, ifWindDown);
        if (windDirectionLevel == 0) {
            mLeftWindImageView.setVisibility(View.INVISIBLE);
            mRightWindImageView.setVisibility(View.INVISIBLE);
        } else {
            mLeftWindImageView.setVisibility(View.VISIBLE);
            mRightWindImageView.setVisibility(View.VISIBLE);
            mLeftWindDrawable.setLevel(windDirectionLevel);
            mRightWindDrawable.setLevel(windDirectionLevel);
        }

        int leftSeatHeatingLevel = airConditioning.getLeftSeatHeatingLevel();
        if (leftSeatHeatingLevel > 0) {
            mLeftSeatHeatingImageView.setVisibility(View.VISIBLE);
            mLeftSeatHeatingDrawable.setLevel(leftSeatHeatingLevel);
        } else {
            mLeftSeatHeatingImageView.setVisibility(View.INVISIBLE);
        }

        int rightSeatHeatingLevel = airConditioning.getRightSeatHeatingLevel();
        if (rightSeatHeatingLevel > 0) {
            mRightSeatHeatingImageView.setVisibility(View.VISIBLE);
            mRightSeatHeatingDrawable.setLevel(rightSeatHeatingLevel);
        } else {
            mRightSeatHeatingImageView.setVisibility(View.INVISIBLE);
        }
        int windLevel = airConditioning.getWindLevel();
        for (int i = 0; i < mWindIndicatorList.size(); i++) {
            final LevelListDrawable drawable = mWindIndicatorList.get(i);
            if (windLevel > i) {
                drawable.setLevel(1);
            } else {
                drawable.setLevel(0);
            }
        }
//        final float leftTemp = airConditioning.getLeftTemp();
//        if (leftTemp < TEMP_MIN) {
//            mLeftTempTextView.setText(mLeftTempTextView.getResources().getString(R.string.air_containing_temp_min));
//        } else if (leftTemp > TEMP_MAX) {
//            mLeftTempTextView.setText(mLeftTempTextView.getResources().getString(R.string.air_containing_temp_max));
//        } else {
//            mLeftTempTextView.setText(leftTemp + mTempUnit);
//        }
//        final float rightTemp = airConditioning.getRightTemp();
//        if (rightTemp < TEMP_MIN) {
//            mRightTempTextView.setText(mRightTempTextView.getResources().getString(R.string.air_containing_temp_min));
//        } else if (rightTemp > TEMP_MAX) {
//            mRightTempTextView.setText(mRightTempTextView.getResources().getString(R.string.air_containing_temp_max));
//        } else {
//            mRightTempTextView.setText(rightTemp + mTempUnit);
//        }
        updateTemp(airConditioning.getLeftTemp(), mLeftTempTextView);
        updateTemp(airConditioning.getRightTemp(), mRightTempTextView);
    }
    
    private static void updateTemp(float temp, TextView textView) {
        if (temp < TEMP_MIN) {
            textView.setText(textView.getResources().getString(R.string.air_containing_temp_min));
        } else if (temp > TEMP_MAX) {
            textView.setText(textView.getResources().getString(R.string.air_containing_temp_max));
        } else {
            textView.setText(temp + textView.getResources().getString(R.string.temp_unit));
        }
    }
}
