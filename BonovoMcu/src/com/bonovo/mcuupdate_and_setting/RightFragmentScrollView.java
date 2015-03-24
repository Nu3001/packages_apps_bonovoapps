package com.bonovo.mcuupdate_and_setting;

import android.app.Fragment;
import android.widget.ScrollView;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;

public class RightFragmentScrollView extends Fragment {
    private ScrollView scrollView;
    
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.right_scrollview, null);
        return view;
    }
    
    public void onDestroy() {
        super.onDestroy();
    }
}
