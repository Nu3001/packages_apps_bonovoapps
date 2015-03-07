package com.newsmy.car.radar;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.android.internal.car.can.Radar;

/**
 * Created by zybo on 8/13/14.
 */
public class RadarViewController {
    private static final String TAG = "RadarViewController";
    private final ViewGroup mRootView;
    private final String REAR_LEFT_FORMAT = "image_radar_back_level%d_left_2";
    private final String REAR_CENTER_LEFT_FORMAT = "image_radar_back_level%d_left_1";
    private final String REAR_CENTER_RIGHT_FORMAT = "image_radar_back_level%d_right_1";
    private final String REAR_RIGHT_FORMAT = "image_radar_back_level%d_right_2";
    private final String FRONT_LEFT_FORMAT = "image_radar_front_level%d_left_2";
    private final String FRONT_CENTER_LEFT_FORMAT = "image_radar_front_level%d_left_1";
    private final String FRONT_CENTER_RIGHT_FORMAT = "image_radar_front_level%d_right_1";
    private final String FRONT_RIGHT_FORMAT = "image_radar_front_level%d_right_2";
    private boolean mInited;
    private List<ImageView> mRearLeftImages;
    private List<ImageView> mRearRightImages;
    private List<ImageView> mRearCenterLeftImages;
    private List<ImageView> mRearCenterRightImages;
    private List<ImageView> mFrontLeftImages;
    private List<ImageView> mFrontRightImages;
    private List<ImageView> mFrontCenterLeftImages;
    private List<ImageView> mFrontCenterRightImages;

    public RadarViewController(ViewGroup rootView) {
        mRootView = rootView;
        mInited = false;
        mRootView.setSystemUiVisibility(8);
        mRearLeftImages = new ArrayList<ImageView>(MAX_LEVEL);
        mRearRightImages = new ArrayList<ImageView>(MAX_LEVEL);
        mRearCenterLeftImages = new ArrayList<ImageView>(MAX_LEVEL);
        mRearCenterRightImages = new ArrayList<ImageView>(MAX_LEVEL);
        mFrontLeftImages = new ArrayList<ImageView>(MAX_LEVEL);
        mFrontRightImages = new ArrayList<ImageView>(MAX_LEVEL);
        mFrontCenterLeftImages = new ArrayList<ImageView>(MAX_LEVEL);
        mFrontCenterRightImages = new ArrayList<ImageView>(MAX_LEVEL);
    }

    public void updateViews(final Radar radarModel) {
        if (!mInited) {
            setupViews();
            mInited = true;
        }
        if (radarModel == null)
            return;

        Log.d(TAG, "radar : " + radarModel.toString());
        updateImages(mRearLeftImages, radarModel.getDistanceTailstockLeft());
        updateImages(mRearCenterLeftImages, radarModel.getDistanceTailstockCentreLeft());
        updateImages(mRearCenterRightImages, radarModel.getDistanceTailstockCentreRight());
        updateImages(mRearRightImages, radarModel.getDistanceTailstockRight());

        updateImages(mFrontLeftImages, radarModel.getDistanceHeadstockLeft());
        updateImages(mFrontCenterLeftImages, radarModel.getDistanceHeadstockCentreLeft());
        updateImages(mFrontCenterRightImages, radarModel.getDistanceHeadstockCentreRight());
        updateImages(mFrontRightImages, radarModel.getDistanceHeadstockRight());
    }

    private static final int MIN_LEVEL = 1;
    private static final int MAX_LEVEL = 10;

    private static void setupImageLists(final ViewGroup rootView, final String idFormat,
                                        final List<ImageView> imageViews) {
        imageViews.clear();
        for (int i = MIN_LEVEL; i <= MAX_LEVEL; i++) {
            final String idName = String.format(idFormat, i);
            final int id = rootView.getResources().getIdentifier(idName, "id",
                    rootView.getContext().getPackageName());
            if (id != 0) {
                final ImageView imageView = (ImageView) rootView.findViewById(id);
                imageView.setVisibility(View.INVISIBLE);
                imageViews.add(imageView);
            }
        }
    }

    private static void updateImages(final List<ImageView> imageViews, int level) {
        if (level > MAX_LEVEL)
            level = MAX_LEVEL;
        for (int i = MIN_LEVEL; i <= MAX_LEVEL; i++) {
            final ImageView imageView = imageViews.get(MAX_LEVEL - i);
            if (i == level) {
                if (imageView.getVisibility() != View.VISIBLE)
                    imageView.setVisibility(View.VISIBLE);
            } else {
                if (imageView.getVisibility() != View.INVISIBLE)
                    imageView.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void setupViews() {
        // rear................
        setupImageLists((ViewGroup) mRootView.findViewById(R.id.back_left),
                REAR_LEFT_FORMAT, mRearLeftImages);
        setupImageLists((ViewGroup) mRootView.findViewById(R.id.back_left),
                REAR_CENTER_LEFT_FORMAT, mRearCenterLeftImages);
        setupImageLists((ViewGroup) mRootView.findViewById(R.id.back_right),
                REAR_CENTER_RIGHT_FORMAT, mRearCenterRightImages);
        setupImageLists((ViewGroup) mRootView.findViewById(R.id.back_right),
                REAR_RIGHT_FORMAT, mRearRightImages);

        // front....................
        setupImageLists((ViewGroup) mRootView.findViewById(R.id.front_left),
                FRONT_LEFT_FORMAT, mFrontLeftImages);
        setupImageLists((ViewGroup) mRootView.findViewById(R.id.front_left),
                FRONT_CENTER_LEFT_FORMAT, mFrontCenterLeftImages);
        setupImageLists((ViewGroup) mRootView.findViewById(R.id.front_right),
                FRONT_CENTER_RIGHT_FORMAT, mFrontCenterRightImages);
        setupImageLists((ViewGroup) mRootView.findViewById(R.id.front_right),
                FRONT_RIGHT_FORMAT, mFrontRightImages);
    }
}
