package org.n0.speedy_launch;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.content.Context;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class MainActivity extends Activity {

    SharedPreferences prefs;
    SharedPreferences.Editor prefEditor;

    ListView appListView;
    ListView leftPrefsListView;
    ListView rightPrefsListView;

    EditText searchKeyEdit;

    Button leftBtn1;
    Button leftBtn2;
    Button rightBtn1;
    Button rightBtn2;
    AlertDialog.Builder alertDialogBuilder;

    private ArrayList<String> packageNamesArrList;
    private ArrayAdapter<String> appAdapter;
    private List<String[]> packageList;
    private PackageManager packageManager;


    /* the global search string */
    private String searchString;

    private boolean tempShowApp = false;

    final private String[] leftPrefsArr = new String[]{"∫", "∃", "∈", "∉", "⊂", "⊗", "∀"};
    final private String[] rightPrefsArr = new String[]{"∞", "∧", "∨", "⊂", "∑", "∏", "⊕"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        /* Get UI Elements */
        appListView = findViewById(R.id.appListView);
        searchKeyEdit = findViewById(R.id.searchKeyEdit);
        leftBtn1 = findViewById(R.id.leftBtn1);
        leftBtn2 = findViewById(R.id.leftBtn2);
        rightBtn1 = findViewById(R.id.rightBtn1);
        rightBtn2 = findViewById(R.id.rightBtn2);
        leftPrefsListView = findViewById(R.id.leftPrefs);
        rightPrefsListView = findViewById(R.id.rightPrefs);
        alertDialogBuilder = new AlertDialog.Builder(this);

        /* get the system package manager */
        packageManager = getPackageManager();

        /* array list for storing the package names */
        packageNamesArrList = new ArrayList<>();

        /* array adapter which will be used to populate the main list */
        appAdapter = new ArrayAdapter<>(this, R.layout.main_listview, R.id.mnTxtVw, new ArrayList<>());

        ArrayAdapter<String> leftPrefsAdapter = new ArrayAdapter<>(this, R.layout.main_listview, R.id.mnTxtVw, rightPrefsArr);
        ArrayAdapter<String> rightPrefsAdapter = new ArrayAdapter<>(this, R.layout.main_listview, R.id.mnTxtVw, leftPrefsArr);


        leftPrefsListView.setAdapter(leftPrefsAdapter);
        rightPrefsListView.setAdapter(rightPrefsAdapter);

        searchString = "";
        /* get all the package names into the list */
        updateAppList();

        /* launch the app */
        appListView.setOnItemClickListener((adapterView, view, i, l) -> launch(packageNamesArrList.get(i)));

        /* try to open the app's settings */
        appListView.setOnItemLongClickListener((adapterView, view, i, l) -> {
            try {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + packageNamesArrList.get(i)));
                if (getAppNameFromPkgName(packageNamesArrList.get(i)).equals("")) {
                    throw new Exception("package deleted");
                }
                startActivity(intent);
            } catch (Exception e) {
                showAppDeleted();
            }
            return true;
        });



        /* get all preferences */
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefEditor = prefs.edit();


        leftBtn1.setOnLongClickListener((view) -> {
            prefEditor.putBoolean("skeptic", !prefs.getBoolean("skeptic", false)).apply();
            return true;
        });

        leftBtn1.setOnClickListener((view) -> {
            prefEditor.putBoolean("show_menu", !prefs.getBoolean("show_menu", false)).apply();
            filterAppList();
        });

        leftBtn2.setOnClickListener((view) ->

                buttonPrefsFlow("left_2"));


        rightBtn1.setOnClickListener((view) ->

                buttonPrefsFlow("right_1"));

        rightBtn2.setOnClickListener((view) ->

                buttonPrefsFlow("right_2"));

        leftPrefsListView.setOnItemClickListener((adapterView, view, i, l) ->

                buttonPrefsFlow((String) adapterView.

                        getItemAtPosition(i)));

        rightPrefsListView.setOnItemClickListener((adapterView, view, i, l) ->

                buttonPrefsFlow((String) adapterView.

                        getItemAtPosition(i)));

        leftBtn2.setOnLongClickListener((view) ->

        {
            buttonPrefsFlow("left_2_long");
            return true;
        });


        rightBtn1.setOnLongClickListener((view) ->

        {
            buttonPrefsFlow("right_1_long");
            return true;
        });

        rightBtn2.setOnLongClickListener((view) ->

        {
            buttonPrefsFlow("right_2_long");
            return true;
        });


        leftPrefsListView.setOnItemLongClickListener((adapterView, view, i, l) ->

        {
            buttonPrefsFlow(adapterView.getItemAtPosition(i) + "_long");
            return true;
        });

        rightPrefsListView.setOnItemLongClickListener((adapterView, view, i, l) ->

        {
            buttonPrefsFlow(adapterView.getItemAtPosition(i) + "_long");
            return true;
        });

        searchKeyEdit.addTextChangedListener(new

                                                     TextWatcher() {
                                                         @Override
                                                         public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                                         }

                                                         @Override
                                                         public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                                             setKey(String.valueOf(charSequence).toLowerCase(Locale.getDefault()));
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

    void showHelp() {
        updateAppList();
        filterAppList();
        alertDialogBuilder.setMessage(R.string.help);
        alertDialogBuilder.setPositiveButton(R.string.go, (dialogInterface, i) -> {
        });
        alertDialogBuilder.create().show();
    }

    void showAppDeleted() {
        updateAppList();
        filterAppList();
        alertDialogBuilder.setMessage(R.string.appDeleted);
        alertDialogBuilder.setPositiveButton(R.string.go, (dialogInterface, i) -> {
        });
        alertDialogBuilder.create().show();
    }

    void setKey(String s) {
        searchString = s;
        filterAppList();
    }

    void buttonPrefsFlow(String key) {
        String pkgName = prefs.getString(key, "");
        if (!pkgName.equals("")) {
            String appName;
            appName = getAppNameFromPkgName(pkgName);
            if (appName.equals("")) {
                prefEditor.putString(key, "").apply();
                showAppDeleted();
                changeOnPressAppDialog(key);
            } else {
                if (prefs.getBoolean("skeptic", false)) {
                    launchOrChangePref(key, appName, pkgName);
                } else {
                    launch(pkgName);
                }
            }
        } else {
            changeOnPressAppDialog(key);
        }
    }

    void launchOrChangePref(String key, String appName, String packageName) {
        alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(appName);
        alertDialogBuilder.setPositiveButton(R.string.go, (dialogInterface, i1) -> launch(packageName));
        alertDialogBuilder.setNegativeButton(R.string.change, (dialogInterface, i1) -> changeOnPressAppDialog(key));
        alertDialogBuilder.create().show();
    }


    void changeOnPressAppDialog(String key) {
        tempShowApp = true;
        alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(R.layout.main_listview);
        alertDialogBuilder.setAdapter(appListView.getAdapter(), (dialogInterface, i1) -> {
            prefEditor.putString(key, packageNamesArrList.get(i1));
            prefEditor.apply();
            tempShowApp = false;
            filterAppList();
        });
        alertDialogBuilder.setOnCancelListener((dialogInterface) -> {
            tempShowApp = false;
            filterAppList();
        });
        filterAppList();
        alertDialogBuilder.create().show();
    }


    String getAppNameFromPkgName(String pkgName) {
        try {
            return (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkgName, 0));
        } catch (PackageManager.NameNotFoundException ne) {
            ne.printStackTrace();
            return "";
        }
    }

    public void onBackPressed() {
        searchString = "";
        searchKeyEdit.getText().clear();
    }


    void launch(String nm) {
        ((InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(appListView.getWindowToken(), 0);
        if (nm.equals("org.n0.speedy_launch")) {
            showHelp();
        } else {
            try {
                startActivity(packageManager.getLaunchIntentForPackage(nm));
            } catch (Exception e) {
                showAppDeleted();
            }
        }
    }

    void updateAppList() {
        /* fetch all the installed apps */
        List<ResolveInfo> resolveInfoPackageList = packageManager.queryIntentActivities(new Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER), PackageManager.MATCH_ALL);
        resolveInfoPackageList.sort(new ResolveInfo.DisplayNameComparator(packageManager));
        packageList = resolveInfoPackageList.stream().map(resolveInfo -> new String[]{resolveInfo.activityInfo.packageName, (String) resolveInfo.loadLabel(packageManager)}).collect(Collectors.toList());
    }

    void clearList() {
        appAdapter.clear();
        packageNamesArrList.clear();
    }

    void fetchAllApps() {

        /* clear the global search string */
        searchString = "";

        searchKeyEdit.getText().clear();

        /* clear the list before repopulating */
        clearList();

        if (!tempShowApp && searchString.length() == 0 && !prefs.getBoolean("show_menu", false)) {
            showApps();
            return;
        }

        /* add the apps names to the adapter, and the package name to the array list */
        for (String[] resolver : packageList) {
            appAdapter.add(resolver[1]);
            packageNamesArrList.add(resolver[0]);
        }

        showApps();
    }

    void filterAppList() {
        clearList();
        if (!tempShowApp && searchString.length() == 0 && !prefs.getBoolean("show_menu", false)) {
            return;
        }
        /* check each package name and add only the ones that match the search
        string */
        for (String[] resolver : packageList) {
            if (resolver[1].toLowerCase(Locale.getDefault()).contains(searchString)) {
                appAdapter.add(resolver[1]);
                packageNamesArrList.add(resolver[0]);
            }
        }
        appListView.setSelection(0);
    }

    /* show the app name adapter as the app list */
    void showApps() {
        appListView.setAdapter(appAdapter);
        appListView.setSelection(0);
    }


    @Override
    protected void onResume() {
        super.onResume();
        fetchAllApps();
    }
}