package com.bonovo.mcuupdate_and_setting;

import android.app.DialogFragment;
import android.os.Bundle;
import android.app.Dialog;
import android.app.AlertDialog;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.content.DialogInterface;

public class McuDialogFragment extends DialogFragment {
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
    }
    
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }
    
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.mcudialog, null);
        builder.setView(view).setPositiveButton("Update", new DialogInterface.OnClickListener() {
            
			@Override
            public void onClick(DialogInterface dialog, int id) {
            }
        }).setNegativeButton("Cancel", null);
        return builder.create();
    }
}
