package com.bonovo.musicplayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

public class APEMusicListActivity extends Activity {
    private static final String KEY_APEFILELIST = "apefilelist";
    private static final String KEY_SELECTEDFILELIST = "selectedfilelist";
    private ListView lvSong;
    private Button btnOK;
    private Button btnCancel;
    private ArrayList<TrackItem> mItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ape_music_list_layout);
        lvSong = (ListView) findViewById(R.id.ape_music_list_view);
        btnOK = (Button) findViewById(R.id.btn_OK);
        btnCancel = (Button) findViewById(R.id.btn_Cancel);
        Intent intent = this.getIntent();
        mItems = (ArrayList<TrackItem>) intent.getExtras().getSerializable(
                KEY_APEFILELIST);
        int songsCount = mItems.size();
        String songs[] = new String[songsCount];
        for (int i = 0; i < songsCount; i++) {
            songs[i] = mItems.get(i).trackTitle.trim();
        }
        lvSong.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_multiple_choice, songs));
        lvSong.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        btnOK.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                SparseBooleanArray selecedItems = lvSong
                        .getCheckedItemPositions();
                ArrayList<Integer> selectedIndex = new ArrayList<Integer>();
                for (int i = 0; i < mItems.size(); i++) {
                    if (selecedItems.get(i)) {
                        selectedIndex.add(i);
                    }
                }
                if (selectedIndex.size() == 0) {
                    Dialog dialog = new AlertDialog.Builder(
                            APEMusicListActivity.this)
                            .setTitle("ϵͳ��ʾ")
                            .setMessage("��ѡ����Ҫ���ŵĸ���")
                            .setPositiveButton("ȷ��",
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(
                                                DialogInterface arg0, int arg1) {
                                            // TODO Auto-generated method stub
                                            arg0.dismiss();
                                        }
                                    }).create();
                    dialog.show();
                    return;
                }
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < selectedIndex.size(); i++) {
                    int index = selectedIndex.get(i);
                    if (index != mItems.size() - 1) {
                        sb.append(mItems.get(index).playtime);
                        sb.append(",");
                        sb.append(mItems.get(index + 1).waitTime);
                        sb.append("|");
                    } else {
                        sb.append(mItems.get(index).playtime);
                    }
                }
                if (sb.charAt(sb.length() - 1) == '|') {
                    sb.deleteCharAt(sb.length() - 1);
                }
                Intent intent = APEMusicListActivity.this.getIntent();
                intent.putExtra(KEY_SELECTEDFILELIST, sb.toString());
                APEMusicListActivity.this.setResult(RESULT_OK, intent);
                APEMusicListActivity.this.finish();
            }

        });
        btnCancel.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                APEMusicListActivity.this.setResult(RESULT_CANCELED);
                APEMusicListActivity.this.finish();
            }

        });
    }

}
