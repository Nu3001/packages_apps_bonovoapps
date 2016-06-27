package com.bonovo.bonovohandle;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;

public class AppSwitchActivity extends Activity implements ItemClickListener, ItemLongClickListener{

    RecyclerView recyclerView;
    AppSwitchAdapter mAdapter;
    int currentPos = 0;
    int newPos = 0;
    private ArrayList<String> apps = new ArrayList<String>();
    public static final String SWITCH_APP = "com.bonovo.bonovohandle.SWITCH_APP";

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.app_switcher);
        //setTitle("Switch App");

        HandleService.AppSwitchResumed();
        Intent intent = getIntent();
        apps = intent.getExtras().getStringArrayList("apps");

        if (apps != null) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(SWITCH_APP);
            LocalBroadcastManager.getInstance(this).registerReceiver(bReceiver, intentFilter);

            recyclerView = (RecyclerView) findViewById(R.id.recyclerview);

            setView();
            onItemClicked(0);
        }
        else {
            finish();
        }
    }

    private void setView() {
        int width = recyclerView.getMinimumWidth();
        int count = apps.size();

        final PackageManager pm = getApplicationContext().getPackageManager();
        AppItem appItem[] = new AppItem[apps.size()];
        PackageInfo packageInfo;

        for(int i=0;i<count; i++) {
            try {
                packageInfo = pm.getPackageInfo(apps.get(i), PackageManager.GET_META_DATA);
            } catch (PackageManager.NameNotFoundException e) {
                return;
            }

            String appName = packageInfo.applicationInfo.loadLabel(pm).toString();
            Drawable appIcon = packageInfo.applicationInfo.loadIcon(pm);
            appItem[i] = new AppItem(appName, appIcon);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setLayoutParams(new LinearLayout.LayoutParams(width * count, LinearLayout.LayoutParams.WRAP_CONTENT));

        mAdapter = new AppSwitchAdapter(this, appItem, this, this);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());


    }

    protected void launchApp(String packageName) {
        Intent mIntent = getApplicationContext().getPackageManager().getLaunchIntentForPackage(
                packageName);
        if (mIntent != null) {
            try {
                startActivity(mIntent);
            } catch (ActivityNotFoundException err) {
                Toast t = Toast.makeText(getApplicationContext(),
                        "App not found", Toast.LENGTH_SHORT);
                t.show();
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        HandleService.AppSwitchResumed();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        HandleService.AppSwitchPaused();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        HandleService.AppSwitchPaused();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(bReceiver);
    }

    @Override
    public void onItemClicked(final int position) {
        currentPos = position;
        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            int selectedPos = position;
            @Override
            public void run() {
                if (this.selectedPos == currentPos) {
                        launchApp(apps.get(position));
                    finish();
                }
            }
        }, 3000);

    }

    @Override
    public void onItemLongClicked(int position, View v) {
        launchApp(apps.get(position));
        finish();
    }

    private BroadcastReceiver bReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(SWITCH_APP)) {
                newPos = currentPos + 1;
                if (newPos == apps.size()) {
                    newPos = 0;
                }
                recyclerView.findViewHolderForAdapterPosition(newPos).itemView.performClick();
            }
        }
    };
}
