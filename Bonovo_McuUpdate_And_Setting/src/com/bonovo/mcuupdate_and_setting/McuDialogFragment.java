package com.bonovo.mcuupdate_and_setting;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class McuDialogFragment extends DialogFragment {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setCancelable(false);// Settings Dialog click screen don't dismiss
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());  
        // Get the layout inflater  
        LayoutInflater inflater = getActivity().getLayoutInflater();  
        View view = inflater.inflate(R.layout.mcudialog, null);  
        // Inflate and set the layout for the dialog  
        // Pass null as the parent view because its going in the dialog layout  
        builder.setView(view)
                // Add action buttons  
                .setPositiveButton("Update",  
                        new DialogInterface.OnClickListener()  
                        {  
                            @Override  
                            public void onClick(DialogInterface dialog, int id)  
                            {  
                            	
                            } 
                        }).setNegativeButton("Cancel", null);
        return builder.create();  
		
	}

}
