package com.bonovo.mcuupdate_and_setting;

import android.os.Bundle;
import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Button;
import android.view.View.OnClickListener;
import com.bonovo.colortools.MyViewRectMID;
import com.bonovo.colortools.MyViewRect;

public class RightFragmentKeysBackLight extends Fragment {

	private Activity context;
	private CallBackColor backColor;
	private TextView textviewR = null;
	private TextView textviewG = null;
	private TextView textviewB = null;
	private MyViewRectMID viewmid = null;
	private SeekBar seekbarR = null;
	private SeekBar seekbarG = null;
	private SeekBar seekbarB = null;
	private int intR;
	private int intG;
	private int intB;
	private static final String COLORVALE = "colorvale";
	private SharedPreferences myShare;

	public void onAttach(Activity activity) {
		super.onAttach(activity);
		context = activity;
		try {
			backColor = (CallBackColor)activity;
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		myShare = getActivity().getSharedPreferences(COLORVALE,
				Activity.MODE_PRIVATE);

		intR = myShare.getInt("R", 100);
		intG = myShare.getInt("G", 100);
		intB = myShare.getInt("B", 100);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.right_keysbacklight, null);

		viewmid = (MyViewRectMID) view.findViewById(R.id.mid);
		viewmid.setARGB(255, intR, intG, intB);

		textviewR = (TextView) view.findViewById(R.id.textValueR);
		textviewG = (TextView) view.findViewById(R.id.textValueG);
		textviewB = (TextView) view.findViewById(R.id.textValueB);
		textviewR.setText("" + intR);
		textviewG.setText("" + intG);
		textviewB.setText("" + intB);

		MyViewRect view1 = (MyViewRect) view.findViewById(R.id.min1);
		MyViewRect view2 = (MyViewRect) view.findViewById(R.id.min2);
		MyViewRect view3 = (MyViewRect) view.findViewById(R.id.min3);
		MyViewRect view4 = (MyViewRect) view.findViewById(R.id.min4);
		MyViewRect view5 = (MyViewRect) view.findViewById(R.id.min5);
		MyViewRect view6 = (MyViewRect) view.findViewById(R.id.min6);
		MyViewRect view7 = (MyViewRect) view.findViewById(R.id.min7);
		MyViewRect view8 = (MyViewRect) view.findViewById(R.id.min8);
		MyViewRect view9 = (MyViewRect) view.findViewById(R.id.min9);
		MyViewRect view10 = (MyViewRect) view.findViewById(R.id.min10);
		MyViewRect view11 = (MyViewRect) view.findViewById(R.id.min11);
		MyViewRect view12 = (MyViewRect) view.findViewById(R.id.min12);
		MyViewRect view13 = (MyViewRect) view.findViewById(R.id.min13);
		MyViewRect view14 = (MyViewRect) view.findViewById(R.id.min14);
		view1.setARGB(255, 255, 255, 0);
		view2.setARGB(255, 127, 255, 0);
		view3.setARGB(255, 0, 255, 0);
		view4.setARGB(255, 0, 255, 127);
		view5.setARGB(255, 0, 255, 255);
		view6.setARGB(255, 0, 191, 255);
		view7.setARGB(255, 0, 127, 255);
		view8.setARGB(255, 0, 0, 255);
		view9.setARGB(255, 128, 0, 255);
		view10.setARGB(255, 200, 0, 200);
		view11.setARGB(255, 255, 0, 128);
		view12.setARGB(255, 255, 0, 0);
		view13.setARGB(255, 255, 63, 0);
		view14.setARGB(255, 255, 191, 0);
		view1.setOnTouchListener(new OnTouchListener());
		view2.setOnTouchListener(new OnTouchListener());
		view3.setOnTouchListener(new OnTouchListener());
		view4.setOnTouchListener(new OnTouchListener());
		view5.setOnTouchListener(new OnTouchListener());
		view6.setOnTouchListener(new OnTouchListener());
		view7.setOnTouchListener(new OnTouchListener());
		view8.setOnTouchListener(new OnTouchListener());
		view9.setOnTouchListener(new OnTouchListener());
		view10.setOnTouchListener(new OnTouchListener());
		view11.setOnTouchListener(new OnTouchListener());
		view12.setOnTouchListener(new OnTouchListener());
		view13.setOnTouchListener(new OnTouchListener());
		view14.setOnTouchListener(new OnTouchListener());

		seekbarR = (SeekBar) view.findViewById(R.id.seekBarR);
		seekbarG = (SeekBar) view.findViewById(R.id.seekBarG);
		seekbarB = (SeekBar) view.findViewById(R.id.seekBarB);
		seekbarR.setMax(255);
		seekbarG.setMax(255);
		seekbarB.setMax(255);
		seekbarR.setProgress(intR);
		seekbarG.setProgress(intG);
		seekbarB.setProgress(intB);
		seekbarR.setOnSeekBarChangeListener(new OnSeekBarChangeListener());
		seekbarG.setOnSeekBarChangeListener(new OnSeekBarChangeListener());
		seekbarB.setOnSeekBarChangeListener(new OnSeekBarChangeListener());

		Button button = (Button) view.findViewById(R.id.ok);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				SharedPreferences.Editor edit = myShare.edit();
				edit.putInt("R", intR);
				edit.putInt("G", intG);
				edit.putInt("B", intB);
				edit.commit();
				
				backColor.setColor(intR, intG, intB);
			}
		});

		return (view);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	private class OnSeekBarChangeListener implements
			SeekBar.OnSeekBarChangeListener {

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// TODO Auto-generated method stub
			switch (seekBar.getId()) {
			case R.id.seekBarR:
				intR = progress;
				textviewR.setText("" + intR);
				break;
			case R.id.seekBarG:
				intG = progress;
				textviewG.setText("" + intG);
				break;
			case R.id.seekBarB:
				intB = progress;
				textviewB.setText("" + intB);
				break;
			}
			viewmid.setARGB(255, intR, intG, intB);
		}
	}

	private class OnTouchListener implements android.view.View.OnTouchListener {

		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.min1:
				seekbarR.setProgress(255);
				seekbarG.setProgress(255);
				seekbarB.setProgress(0);
				viewmid.setARGB(255, 255, 255, 0);
				intR = 255;
				intG = 255;
				intB = 0;
				break;
			case R.id.min2:
				seekbarR.setProgress(127);
				seekbarG.setProgress(255);
				seekbarB.setProgress(0);
				viewmid.setARGB(255, 127, 255, 0);
				intR = 127;
				intG = 255;
				intB = 0;
				break;
			case R.id.min3:
				seekbarR.setProgress(0);
				seekbarG.setProgress(255);
				seekbarB.setProgress(0);
				viewmid.setARGB(255, 0, 255, 0);
				intR = 0;
				intG = 255;
				intB = 0;
				break;
			case R.id.min4:
				seekbarR.setProgress(0);
				seekbarG.setProgress(255);
				seekbarB.setProgress(127);
				viewmid.setARGB(255, 0, 255, 127);
				intR = 0;
				intG = 255;
				intB = 127;
				break;
			case R.id.min5:
				seekbarR.setProgress(0);
				seekbarG.setProgress(255);
				seekbarB.setProgress(255);
				viewmid.setARGB(255, 0, 255, 255);
				intR = 0;
				intG = 255;
				intB = 255;
				break;
			case R.id.min6:
				seekbarR.setProgress(0);
				seekbarG.setProgress(191);
				seekbarB.setProgress(255);
				viewmid.setARGB(255, 0, 191, 255);
				intR = 0;
				intG = 191;
				intB = 255;
				break;
			case R.id.min7:
				seekbarR.setProgress(0);
				seekbarG.setProgress(127);
				seekbarB.setProgress(255);
				viewmid.setARGB(255, 0, 127, 255);
				intR = 0;
				intG = 127;
				intB = 255;
				break;
			case R.id.min8:
				seekbarR.setProgress(0);
				seekbarG.setProgress(0);
				seekbarB.setProgress(255);
				viewmid.setARGB(255, 0, 0, 255);
				intR = 0;
				intG = 0;
				intB = 255;
				break;
			case R.id.min9:
				seekbarR.setProgress(128);
				seekbarG.setProgress(0);
				seekbarB.setProgress(255);
				viewmid.setARGB(255, 128, 0, 255);
				intR = 128;
				intG = 0;
				intB = 255;
				break;
			case R.id.min10:
				seekbarR.setProgress(200);
				seekbarG.setProgress(0);
				seekbarB.setProgress(200);
				viewmid.setARGB(255, 200, 0, 200);
				intR = 200;
				intG = 0;
				intB = 200;
				break;
			case R.id.min11:
				seekbarR.setProgress(255);
				seekbarG.setProgress(0);
				seekbarB.setProgress(128);
				viewmid.setARGB(255, 255, 0, 128);
				intR = 255;
				intG = 0;
				intB = 128;
				break;
			case R.id.min12:
				seekbarR.setProgress(255);
				seekbarG.setProgress(0);
				seekbarB.setProgress(0);
				viewmid.setARGB(255, 255, 0, 0);
				intR = 255;
				intG = 0;
				intB = 0;
				break;
			case R.id.min13:
				seekbarR.setProgress(255);
				seekbarG.setProgress(63);
				seekbarB.setProgress(0);
				viewmid.setARGB(255, 255, 63, 0);
				intR = 255;
				intG = 63;
				intB = 0;
				break;
			case R.id.min14:
				seekbarR.setProgress(255);
				seekbarG.setProgress(191);
				seekbarB.setProgress(0);
				viewmid.setARGB(255, 255, 191, 0);
				intR = 255;
				intG = 191;
				intB = 0;
				break;
			}
			textviewR.setText("" + intR);
			textviewG.setText("" + intG);
			textviewB.setText("" + intB);
			return false;
		}
	}
	
	public interface CallBackColor{
		public void setColor(int red, int green, int blue);
	}

}
