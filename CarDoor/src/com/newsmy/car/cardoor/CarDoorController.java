
package com.newsmy.car.cardoor;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.internal.car.can.CarDoor;

public class CarDoorController {
    private CarDoor mCurrentStatus;
    private ImageView mFrontLeftView;
    private ImageView mFrontRightView;
    private ImageView mRearLeftView;
    private ImageView mRearRightView;
    private ImageView mRearCenterView;

    public void init(View viewContainer) {
        mFrontLeftView = (ImageView) viewContainer.findViewById(R.id.car_door_front_left);
        mFrontRightView = (ImageView) viewContainer.findViewById(R.id.car_door_front_right);
        mRearLeftView = (ImageView) viewContainer.findViewById(R.id.car_door_rear_left);
        mRearRightView = (ImageView) viewContainer.findViewById(R.id.car_door_rear_right);
        mRearCenterView = (ImageView) viewContainer.findViewById(R.id.car_door_rear_center);
    }

    public void updateStatus(CarDoor carDoor) {
        mCurrentStatus = carDoor;
    }

    public void updateViews() {
        final CarDoor carDoor = mCurrentStatus;
        mFrontLeftView.setVisibility(carDoor.getFrontLeft() ? View.VISIBLE : View.INVISIBLE);
        mFrontRightView.setVisibility(carDoor.getFrontRight() ? View.VISIBLE : View.INVISIBLE);
        mRearLeftView.setVisibility(carDoor.getRearLeft() ? View.VISIBLE : View.INVISIBLE);
        mRearRightView.setVisibility(carDoor.getRearRight() ? View.VISIBLE : View.INVISIBLE);
        mRearCenterView.setVisibility(carDoor.getRearCenter() ? View.VISIBLE : View.INVISIBLE);
    }

    public boolean isAllClosed() {
        return mCurrentStatus.isAllClosed();
    }
}
