/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//package com.android.contacts.dialpad;
package com.bonovo.bluetooth.dialpad;

import com.bonovo.bluetooth.R;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.PopupMenu;

/**
 * Fragment that displays a twelve-key phone dialpad.
 */
public class DialpadFragment extends Fragment
        implements View.OnClickListener,
        View.OnLongClickListener, View.OnKeyListener,
        AdapterView.OnItemClickListener, TextWatcher,
        PopupMenu.OnMenuItemClickListener,
        DialpadImageButton.OnPressedListener {
	
//	private static final String TAG = "DialpadFragment";
//	private static final boolean DEBUG = true;
//
//	private View mDigitsContainer;
	private EditText mDigits;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
//		return super.onCreateView(inflater, container, savedInstanceState);
		View fragmentView = inflater.inflate(R.layout.dialpad_fragment, container, false);
//		Resources r = this.getResources();
		
//		mDigitsContainer = fragmentView.findViewById(R.id.digits_container);
		mDigits = (EditText)fragmentView.findViewById(R.id.digits);
//		mDigits.setKeyListener(input)
		mDigits.setOnClickListener(this);
		mDigits.setOnKeyListener(this);
		mDigits.setOnLongClickListener(this);
		mDigits.addTextChangedListener(this);
	
		View oneButton = fragmentView.findViewById(R.id.one);
		if(oneButton != null){
			setupKeypad(fragmentView);
		}
		return fragmentView;
	}
	
	private void setupKeypad(View fragmentView){
		int[] buttonIds = new int[] {
				R.id.one, R.id.two, R.id.three, R.id.four, R.id.five, R.id.six,
				R.id.seven, R.id.eight, R.id.nine, R.id.zero, R.id.star, R.id.pound};
		for(int id : buttonIds){
			((DialpadImageButton)fragmentView.findViewById(id)).setOnPressedListener(this);
		}
		fragmentView.findViewById(R.id.one).setOnLongClickListener(this);
		fragmentView.findViewById(R.id.zero).setOnLongClickListener(this);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	public boolean onMenuItemClick(MenuItem arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void afterTextChanged(Editable s) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onLongClick(View v) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPressed(View view, boolean pressed) {
		// TODO Auto-generated method stub
		
	}
	
	public static boolean phoneIsInUse(){
		boolean phoneInUse = false;
		return phoneInUse;
	}
}
