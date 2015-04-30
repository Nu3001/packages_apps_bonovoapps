package com.bonovo.musicplayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;


import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.ParcelFileDescriptor;

import android.net.Uri;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;

import com.bonovo.musicplayer.IMusicPlayerService.MusicStatus;
import com.bonovo.musicplayer.IMusicPlayerService.MusicStatusChangeListener;

import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;


public class PlayerDetailFragment extends PlayerControl 
{
	private final static String TAG = "PlayerDetailFragment";


	private ImageView mDetailAlbumImage;
    	private TextView mDetailSongText;
    	private TextView mDetailAlbumText;
    	private TextView mDetailGenreText;
    	private TextView mDetailArtistText;


	private void setupViews(View nview) 
	{
		mDetailAlbumImage = (ImageView) nview.findViewById(R.id.player_detail_album_image);
		mDetailSongText = (TextView) nview.findViewById(R.id.player_detail_song_name);
      		mDetailAlbumText = (TextView) nview.findViewById(R.id.player_detail_album_text);
      		mDetailGenreText = (TextView) nview.findViewById(R.id.player_detail_genre_text);
		mDetailArtistText = (TextView) nview.findViewById(R.id.player_detail_artist_text);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onCreateView");
		
		View view = inflater.inflate(R.layout.player_detail_fragment, container,false);

		setupViews(view);	

		super.bindService();
		
		return view;
	}

       @Override
       public void onUpdateUI(MusicStatus status)
       {
		Log.d(TAG, "onUpdateUI");

		
		mDetailSongText.setText(status.currMusic);
		mDetailAlbumText.setText(status.currentAlbum);
		mDetailGenreText.setText(status.currentGenre);
		mDetailArtistText.setText(status.currentArtist);
		SetAlbumArtBmp(status.albumArt);
		
       }
	
	@Override
	public void onDestroy() {
	
	    Log.d(TAG, "onDestroy");

	    super.unbindService();
		
	    super.onDestroy();
	}
///////////////////////////////////////////////////////////////////////////////////////////////////////////
	// PlayerDetailFragment  private function

	private void SetAlbumArtBmp(String albumArtFile)
	{
            if (albumArtFile != null) {
                Uri uri = Uri.fromFile(new File(albumArtFile));
                ParcelFileDescriptor pfd = null;
                Bitmap bm = null;
                try {
                    pfd = getActivity().getContentResolver().openFileDescriptor(uri, "r");
                    if (pfd != null) {
                        FileDescriptor fd = pfd.getFileDescriptor();
                        bm = BitmapFactory.decodeFileDescriptor(fd);
                    }
                    BonovoMusicPlayerUtil.setBackground(
                            mDetailAlbumImage, bm);
                } catch (FileNotFoundException e) {
                    Log.e(TAG, "File not found : " + e);
                }
            } else {
                mDetailAlbumImage.setImageResource(R.drawable.image);
            }
	}

}
