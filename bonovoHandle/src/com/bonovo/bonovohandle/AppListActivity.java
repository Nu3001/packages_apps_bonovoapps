package com.bonovo.bonovohandle;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
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
    PackageManager pm;

    private ArrayList<AppItem> appItems = new ArrayList<AppItem>();
    List<ApplicationInfo> apps;
    private ArrayList<String> appList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_list);

        getApps();
        setView();

        final Button btnSave = (Button) findViewById(R.id.btn_save);
        btnSave.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        final Button btnClear = (Button) findViewById(R.id.btn_clear);
        btnClear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                clearList();
            }
        });

    }

    private void setView() {
        pm = getApplicationContext().getPackageManager();
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        appItems.clear();

        apps = checkLaunchIntent(pm.getInstalledApplications(PackageManager.GET_META_DATA));

        for (int i = 0; i < apps.size(); i++) {
            ApplicationInfo applicationInfo = apps.get(i);

            String pkgName = applicationInfo.packageName;
            String appName = applicationInfo.loadLabel(pm).toString();
            Drawable appIcon = applicationInfo.loadIcon(pm);
            AppItem appItem = new AppItem(pkgName, appName, appIcon, false);

            appItems.add(i, appItem);

            for (int j = 0; j < appList.size(); j++) {

                if ((appList.get(j)).equals(pkgName)) {
                    appItem.setSelected(true);

                    if (j < i) {
                        AppItem appItemOld = appItems.get(j);

                        appItems.remove(i);
                        appItems.remove(j);

                        appItems.add(j, appItem);

                        if (appItems.size() > appList.size())
                            appItems.add(appList.size(), appItemOld);
                        else
                            appItems.add(appItemOld);
                    }
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

    private List<ApplicationInfo> checkLaunchIntent(List<ApplicationInfo> list) {
        ArrayList<ApplicationInfo> apps = new ArrayList<ApplicationInfo>();
        for (ApplicationInfo info : list) {
            try {
                if (pm.getLaunchIntentForPackage(info.packageName) != null) {
                    apps.add(info);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return apps;
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

        if (appList.size() < toPosition) {
            appList.add(app);
        } else {
            appList.add(toPosition, app);
        }

        for (int i = 0; i < appList.size(); i++) {
            editor.remove("APP_" + i);
            editor.putString("APP_" + i, appList.get(i));
        }
        editor.apply();
    }

    private void clearList() {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        for (int i = 0; i < appList.size(); i++) {
            editor.remove("APP_" + i);
        }

        editor.putInt("apparraysize", 0);
        editor.apply();

        appList.clear();
        setView();
    }

}