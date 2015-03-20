package com.bonovo.mcuupdate_and_setting;

import android.widget.SeekBar;
import android.widget.TextView;
import com.bonovo.colortools.MyViewRectMID;
import android.view.View;
import android.view.MotionEvent;
import android.app.Fragment;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.bonovo.colortools.MyViewRect;
import android.widget.Button;

public class RightFragmentKeysBackLight extends Fragment {
    private static final String COLORVALE = "colorvale";
    private CallBackColor backColor;
    private Activity context;
    private int intB;
    private int intG;
    private int intR;
    private SharedPreferences myShare;
    private SeekBar seekbarB;
    private SeekBar seekbarG;
    private SeekBar seekbarR;
    private TextView textviewB;
    private TextView textviewG;
    private TextView textviewR;
    private MyViewRectMID viewmid;
    
    public RightFragmentKeysBackLight() {
        textviewR = 0;
        textviewG = 0;
        textviewB = 0;
        viewmid = 0;
        seekbarR = 0;
        seekbarG = 0;
        seekbarB = 0;
    }
    
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;
        try {
            (CallBackColor)backColor = activity;
        } catch(Exception e) {
        }
    }
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myShare = getActivity().getSharedPreferences("colorvale", 0x0);
        intR = myShare.getInt("R", 0x64);
        intG = myShare.getInt("G", 0x64);
        intB = myShare.getInt("B", 0x64);
    }
    
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(0x7f030006, 0x0);
        viewmid = (MyViewRectMID)view.findViewById(0x7f08000e);
        viewmid.setARGB(0xff, intR, intG, intB);
        textviewR = (TextView)view.findViewById(0x7f080016);
        textviewG = (TextView)view.findViewById(0x7f08001a);
        textviewB = (TextView)view.findViewById(0x7f08001e);
        textviewR.setText("" + intR);
        textviewG.setText("" + intG);
        textviewB.setText("" + intB);
        MyViewRect view1 = (MyViewRect)view.findViewById(0x7f080007);
        MyViewRect view2 = (MyViewRect)view.findViewById(0x7f080008);
        MyViewRect view3 = (MyViewRect)view.findViewById(0x7f080009);
        MyViewRect view4 = (MyViewRect)view.findViewById(0x7f08000a);
        MyViewRect view5 = (MyViewRect)view.findViewById(0x7f08000b);
        MyViewRect view6 = (MyViewRect)view.findViewById(0x7f08000c);
        MyViewRect view7 = (MyViewRect)view.findViewById(0x7f08000d);
        MyViewRect view8 = (MyViewRect)view.findViewById(0x7f08000f);
        MyViewRect view9 = (MyViewRect)view.findViewById(0x7f080010);
        MyViewRect view10 = (MyViewRect)view.findViewById(0x7f080011);
        MyViewRect view11 = (MyViewRect)view.findViewById(0x7f080012);
        MyViewRect view12 = (MyViewRect)view.findViewById(0x7f080013);
        MyViewRect view13 = (MyViewRect)view.findViewById(0x7f080014);
        MyViewRect view14 = (MyViewRect)view.findViewById(0x7f080015);
        view1.setARGB(0xff, 0xff, 0xff, 0x0);
        view2.setARGB(0xff, 0x7f, 0xff, 0x0);
        view3.setARGB(0xff, 0x0, 0xff, 0x0);
        view4.setARGB(0xff, 0x0, 0xff, 0x7f);
        view5.setARGB(0xff, 0x0, 0xff, 0xff);
        view6.setARGB(0xff, 0x0, 0xbf, 0xff);
        view7.setARGB(0xff, 0x0, 0x7f, 0xff);
        view8.setARGB(0xff, 0x0, 0x0, 0xff);
        view9.setARGB(0xff, 0x80, 0x0, 0xff);
        view10.setARGB(0xff, 0xc8, 0x0, 0xc8);
        view11.setARGB(0xff, 0xff, 0x0, 0x80);
        view12.setARGB(0xff, 0xff, 0x0, 0x0);
        view13.setARGB(0xff, 0xff, 0x3f, 0x0);
        view14.setARGB(0xff, 0xff, 0xbf, 0x0);
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
        seekbarR = (SeekBar)view.findViewById(0x7f080018);
        seekbarG = (SeekBar)view.findViewById(0x7f08001c);
        seekbarB = (SeekBar)view.findViewById(0x7f080020);
        seekbarR.setMax(0xff);
        seekbarG.setMax(0xff);
        seekbarB.setMax(0xff);
        seekbarR.setProgress(intR);
        seekbarG.setProgress(intG);
        seekbarB.setProgress(intB);
        seekbarR.setOnSeekBarChangeListener(new OnSeekBarChangeListener());
        seekbarG.setOnSeekBarChangeListener(new OnSeekBarChangeListener());
        seekbarB.setOnSeekBarChangeListener(new OnSeekBarChangeListener());
        Button button = (Button)view.findViewById(0x7f080022);
        button.setOnClickListener(new android.view.View.OnClickListener() {

			@Override
            public void onClick(View v) {
                android.content.SharedPreferences.Editor editor = myShare.edit();
                SharedPreferences.Editor edit = myShare.edit();
                edit.putInt("R", intR);
                edit.putInt("G", intG);
                edit.putInt("B", intB);
                edit.commit();
                backColor.setColor(intR, intG, intB);
            }

        });
        return view;
    }
    
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
    
    public void onStart() {
        super.onStart();
    }
    
    public void onResume() {
        super.onResume();
    }
    
    public void onPause() {
        super.onPause();
    }
    
    public void onStop() {
        super.onStop();
    }
    
    public void onDestroyView() {
        super.onDestroyView();
    }
    
    public void onDestroy() {
        super.onDestroy();
    }
    
    public void onDetach() {
        super.onDetach();
    }
    
    class OnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
        
        public void onStartTrackingTouch(SeekBar seekBar) {
        }
        
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            switch(seekBar.getId()) {
                case 2131230744:
                {
                    intR = progress;
                    textviewR.setText("" + intR);
                    break;
                }
                case 2131230748:
                {
                    intG = progress;
                    textviewG.setText("" + intG);
                    break;
                }
                case 2131230752:
                {
                    intB = progress;
                    textviewB.setText("" + intB);
                    break;
                }
            }
            viewmid.setARGB(0xff, intR, intG, intB);
        }
    }
    
    class OnTouchListener implements View.OnTouchListener {
        
        public boolean onTouch(View v, MotionEvent event) {
            switch(v.getId()) {
                case 2131230727:
                {
                    seekbarR.setProgress(0xff);
                    seekbarG.setProgress(0xff);
                    seekbarB.setProgress(0x0);
                    viewmid.setARGB(0xff, 0xff, 0xff, 0x0);
                    intR = 0xff;
                    intG = 0xff;
                    intB = 0x0;
                    break;
                }
                case 2131230728:
                {
                    seekbarR.setProgress(0x7f);
                    seekbarG.setProgress(0xff);
                    seekbarB.setProgress(0x0);
                    viewmid.setARGB(0xff, 0x7f, 0xff, 0x0);
                    intR = 0x7f;
                    intG = 0xff;
                    intB = 0x0;
                    break;
                }
                case 2131230729:
                {
                    seekbarR.setProgress(0x0);
                    seekbarG.setProgress(0xff);
                    seekbarB.setProgress(0x0);
                    viewmid.setARGB(0xff, 0x0, 0xff, 0x0);
                    intR = 0x0;
                    intG = 0xff;
                    intB = 0x0;
                    break;
                }
                case 2131230730:
                {
                    seekbarR.setProgress(0x0);
                    seekbarG.setProgress(0xff);
                    seekbarB.setProgress(0x7f);
                    viewmid.setARGB(0xff, 0x0, 0xff, 0x7f);
                    intR = 0x0;
                    intG = 0xff;
                    intB = 0x7f;
                    break;
                }
                case 2131230731:
                {
                    seekbarR.setProgress(0x0);
                    seekbarG.setProgress(0xff);
                    seekbarB.setProgress(0xff);
                    viewmid.setARGB(0xff, 0x0, 0xff, 0xff);
                    intR = 0x0;
                    intG = 0xff;
                    intB = 0xff;
                    break;
                }
                case 2131230732:
                {
                    seekbarR.setProgress(0x0);
                    seekbarG.setProgress(0xbf);
                    seekbarB.setProgress(0xff);
                    viewmid.setARGB(0xff, 0x0, 0xbf, 0xff);
                    intR = 0x0;
                    intG = 0xbf;
                    intB = 0xff;
                    break;
                }
                case 2131230733:
                {
                    seekbarR.setProgress(0x0);
                    seekbarG.setProgress(0x7f);
                    seekbarB.setProgress(0xff);
                    viewmid.setARGB(0xff, 0x0, 0x7f, 0xff);
                    intR = 0x0;
                    intG = 0x7f;
                    intB = 0xff;
                    break;
                }
                case 2131230735:
                {
                    seekbarR.setProgress(0x0);
                    seekbarG.setProgress(0x0);
                    seekbarB.setProgress(0xff);
                    viewmid.setARGB(0xff, 0x0, 0x0, 0xff);
                    intR = 0x0;
                    intG = 0x0;
                    intB = 0xff;
                    break;
                }
                case 2131230736:
                {
                    seekbarR.setProgress(0x80);
                    seekbarG.setProgress(0x0);
                    seekbarB.setProgress(0xff);
                    viewmid.setARGB(0xff, 0x80, 0x0, 0xff);
                    intR = 0x80;
                    intG = 0x0;
                    intB = 0xff;
                    break;
                }
                case 2131230737:
                {
                    seekbarR.setProgress(0xc8);
                    seekbarG.setProgress(0x0);
                    seekbarB.setProgress(0xc8);
                    viewmid.setARGB(0xff, 0xc8, 0x0, 0xc8);
                    intR = 0xc8;
                    intG = 0x0;
                    intB = 0xc8;
                    break;
                }
                case 2131230738:
                {
                    seekbarR.setProgress(0xff);
                    seekbarG.setProgress(0x0);
                    seekbarB.setProgress(0x80);
                    viewmid.setARGB(0xff, 0xff, 0x0, 0x80);
                    intR = 0xff;
                    intG = 0x0;
                    intB = 0x80;
                    break;
                }
                case 2131230739:
                {
                    seekbarR.setProgress(0xff);
                    seekbarG.setProgress(0x0);
                    seekbarB.setProgress(0x0);
                    viewmid.setARGB(0xff, 0xff, 0x0, 0x0);
                    intR = 0xff;
                    intG = 0x0;
                    intB = 0x0;
                    break;
                }
                case 2131230740:
                {
                    seekbarR.setProgress(0xff);
                    seekbarG.setProgress(0x3f);
                    seekbarB.setProgress(0x0);
                    viewmid.setARGB(0xff, 0xff, 0x3f, 0x0);
                    intR = 0xff;
                    intG = 0x3f;
                    intB = 0x0;
                    break;
                }
                case 2131230741:
                {
                    seekbarR.setProgress(0xff);
                    seekbarG.setProgress(0xbf);
                    seekbarB.setProgress(0x0);
                    viewmid.setARGB(0xff, 0xff, 0xbf, 0x0);
                    intR = 0xff;
                    intG = 0xbf;
                    intB = 0x0;
                    break;
                }
                case 2131230734:
                {
                    break;
                }
            }
			textviewR.setText("" + intR);
            textviewG.setText("" + intG);
            textviewB.setText("" + intB);
            return false;
        }
    }
}
