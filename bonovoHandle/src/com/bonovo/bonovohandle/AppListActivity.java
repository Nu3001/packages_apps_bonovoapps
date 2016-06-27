package com.bonovo.bonovohandle;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

public class AppListActivity extends Activity implements AppListTransfer, AppListSwap {

    RecyclerView recyclerView;
    AppListAdapter mAdapter;
    SharedPreferences sharedPreferences;

    private ArrayList<AppItem> appItems = new ArrayList<AppItem>();
    List<PackageInfo> apps;
    private ArrayList<String> appList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_list);

        getApps();
        setView();

        final Button button = (Button) findViewById(R.id.btn_save);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void setView() {
        final PackageManager pm = getApplicationContext().getPackageManager();
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        appItems.clear();

        apps = pm.getInstalledPackages(PackageManager.GET_META_DATA);

        for (int i = 0; i < apps.size(); i++) {
            PackageInfo packageInfo = apps.get(i);

            String pkgName = packageInfo.packageName;
            String appName = packageInfo.applicationInfo.loadLabel(pm).toString();
            Drawable appIcon = packageInfo.applicationInfo.loadIcon(pm);
            AppItem appItem = new AppItem(pkgName, appName, appIcon, false);

            appItems.add(appItem);

            for (int j = 0; j < appList.size(); j++) {
                if ((appList.get(j)).equals(pkgName) && j < i) {
                    appItems.remove(appItem);
                    appItems.add(j, new AppItem(pkgName, appName, appIcon, true));
                }
            }
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new AppListAdapter(this, this, this, appItems, appList.size());
        recyclerView.setAdapter(mAdapter);

        ItemTouchHelper.Callback callback = new ItemTouchAdapter(mAdapter);
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(recyclerView);
    }

    private void getApps() {
        appList.clear();
        sharedPreferences = this.getSharedPreferences("apps", MODE_PRIVATE);
        int size = sharedPreferences.getInt("apparraysize", 0);

        for(int i=0; i<size; i++)
            appList.add(sharedPreferences.getString("APP_" + i, ""));
    }

    @Override
    public void setValues(AppItem app) {
        String pkgName = app.getPackageName();
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (app.isSelected()) {
            int pos = app.getPosition();

            if (appList.size() <= pos) {
                appList.add(pkgName);
            }
            else {
                appList.add(pos, pkgName);
            }

            editor.putInt("apparraysize", appList.size());
            for(int i=0;i<appList.size(); i++)
                editor.putString("APP_" + i, appList.get(i));
            editor.apply();
        }

        else {
            int pos = appList.indexOf(pkgName);

            appList.remove(pos);
            editor.remove("APP_" + pos);

            editor.putInt("apparraysize", appList.size());
            for(int i=0;i<appList.size(); i++)
                editor.putString("APP_" + i, appList.get(i));
            editor.apply();
        }

    }

    @Override
    public void appSwap(AppItem appItem, int fromPosition, int toPosition) {

        SharedPreferences.Editor editor = sharedPreferences.edit();

        String app = appItem.getPackageName();
        appList.remove(app);

        if (appList.size() <= toPosition) {
            appList.add(app);
        } else {
            appList.add(toPosition, app);
        }

        for (int i = 0; i < appList.size(); i++)
            editor.putString("APP_" + i, appList.get(i));
        editor.apply();
    }

}