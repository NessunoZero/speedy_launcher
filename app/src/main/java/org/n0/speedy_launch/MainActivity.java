package org.n0.speedy_launch;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    SharedPreferences prefs;
    SharedPreferences.Editor prefEditor;

    TextView textViewEmptyList;
    ListView appListView;
    EditText searchKeyEdit;

    Button leftBtn1;
    Button leftBtn2;
    Button rightBtn1;
    Button rightBtn2;

    AlertDialog.Builder alertDialogBuilder;

    private ArrayList<String> packageNamesArrList;
    private ArrayAdapter<String> appAdapter;
    private List<ResolveInfo> packageList;
    private PackageManager packageManager;

    /* the global search string */
    private String searchString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        /* Get UI Elements */
        textViewEmptyList = findViewById(R.id.textViewEmptyList);
        appListView = findViewById(R.id.appListView);
        searchKeyEdit = findViewById(R.id.searchKeyEdit);
        leftBtn1 = findViewById(R.id.leftBtn1);
        leftBtn2 = findViewById(R.id.leftBtn2);
        rightBtn1 = findViewById(R.id.rightBtn1);
        rightBtn2 = findViewById(R.id.rightBtn2);

        alertDialogBuilder = new AlertDialog.Builder(this);

        /* get the system package manager */
        packageManager = getPackageManager();

        /* array list for storing the package names */
        packageNamesArrList = new ArrayList<>();

        /* array adapter which will be used to populate the main list */
        appAdapter = new ArrayAdapter<>(this,
                R.layout.main_listview, R.id.mnTxtVw, new ArrayList<>());

        /* get all the package names into the list */
        updateAppList();


        /* launch the app */
        appListView.setOnItemClickListener((adapterView, view, i, l) -> launch(packageNamesArrList.get(i)));

        /* try to open the app's settings */
        appListView.setOnItemLongClickListener((adapterView, view, i, l) -> {
            try {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + packageNamesArrList.get(i)));
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                fetchAllApps();
            }
            return true;
        });



        /* get all preferences */
        prefs  = PreferenceManager.getDefaultSharedPreferences(this);
        prefEditor = prefs.edit();


        leftBtn1.setOnLongClickListener((view) -> {
            prefs.edit().clear().apply();
            return true;
        });

        leftBtn1.setOnClickListener((view) -> buttonPrefsFlow( "left_1"));

        leftBtn2.setOnClickListener((view) -> buttonPrefsFlow( "left_2"));

        rightBtn1.setOnClickListener((view) -> buttonPrefsFlow( "right_1"));

        rightBtn2.setOnClickListener((view) -> buttonPrefsFlow( "right_2"));

        leftBtn2.setOnLongClickListener((view) -> {
            buttonPrefsFlow( "left_2_long");
            return true;
        });

        rightBtn1.setOnLongClickListener((view) -> {
            buttonPrefsFlow( "right_1_long");
            return true;
        });

        rightBtn2.setOnLongClickListener((view) -> {
            buttonPrefsFlow( "right_2_long");
            return true;
        });
        searchKeyEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                setKey(String.valueOf(charSequence).toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        if (!prefs.getBoolean("help_shown", false)) {
            showHelp();
            prefEditor.putBoolean("help_shown", true);
            prefEditor.apply();
        }
    }

    void showHelp () {
        alertDialogBuilder.setMessage(R.string.hlp);
        alertDialogBuilder.setPositiveButton(R.string.go, (dialogInterface, i) -> {
        });
        alertDialogBuilder.create().show();
    }

    void setKey (String s) {
        searchString = s;
        filterAppList();
    }

    void buttonPrefsFlow (String key) {
        if(searchString.length() == 0) {
            fetchAllApps();
        }
        String pkgName = prefs.getString(key, "");
        if (!pkgName.equals("")) {
            String appName;
            appName = getAppNameFromPkgName(pkgName);
            if (appName.equals("")) {
                changeOnPressAppDialog(key);
            } else {
                launch(pkgName);
                filterAppList();
            }
        } else {
            changeOnPressAppDialog(key);
        }
    }

    void changeOnPressAppDialog (String key) {
        alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(R.layout.main_listview);
        alertDialogBuilder.setAdapter(appListView.getAdapter(), (dialogInterface, i1) -> {
            prefEditor.putString(key, packageNamesArrList.get(i1));
            prefEditor.apply();
            filterAppList();
        });
        alertDialogBuilder.setOnCancelListener((dialogInterface) -> filterAppList());
        alertDialogBuilder.create().show();
    }

    String getAppNameFromPkgName (String pkgName) {
        try {
            return (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkgName, 0));
        } catch (PackageManager.NameNotFoundException ne) {
            ne.printStackTrace();
            return "";
        }
    }

    void launch(String nm) {
        startActivity(packageManager.getLaunchIntentForPackage(nm));
    }

    void updateAppList () {
        searchString = "";
        /* fetch all the installed apps */
        packageList = packageManager.queryIntentActivities(
                new Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER), 0);
    }

    void clearList () {
        appAdapter.clear();
        packageNamesArrList.clear();
    }

    void fetchAllApps () {

        /* clear the global search string */
        searchString = "";

        searchKeyEdit.getText().clear();

        /* clear the list before repopulating */
        clearList();

        /* add the apps names to the adapter, and the package name to the array list */
        for (ResolveInfo resolver : packageList) {
            String appName = (String) resolver.loadLabel(packageManager);
            appAdapter.add(appName);
            packageNamesArrList.add(resolver.activityInfo.packageName);
        }

        if(appAdapter.getCount() < 1 && searchString.length() > 0) {
            textViewEmptyList.setVisibility(View.VISIBLE);
            return;
        } else {
            textViewEmptyList.setVisibility(View.GONE);
        }
        showApps();
    }

    void filterAppList () {

        clearList();
        if(searchString.length() == 0){
            return;
        }
        /* check each package name and add only the ones that match the search
        string */
        for (ResolveInfo resolver : packageList) {
            String appName = (String) resolver.loadLabel(packageManager);
            if (appName.toLowerCase().contains(searchString)) {
                appAdapter.add(appName);
                packageNamesArrList.add(resolver.activityInfo.packageName);
            }
        }
    }

    /* show the app name adapter as the app list */
    void showApps () {
        appListView.setAdapter(appAdapter);
        appListView.setSelection(0);
    }


    @Override
    protected void onResume () {
        super.onResume();
        updateAppList();
        fetchAllApps();
        filterAppList();
    }
}