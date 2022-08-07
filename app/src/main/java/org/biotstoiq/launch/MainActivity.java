package org.biotstoiq.launch;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends Activity {

    SharedPreferences prfs;
    SharedPreferences.Editor prfEdtr;

    TextView tvEmptyLst;
    ListView lftSrchLstVw;
    ListView lftIISrchLstVw;
    ListView apLst;
    ListView rgtSrchLstVw;
    ListView rgtIISrchLstVw;

    AlertDialog.Builder alrtDlgBldr;

    final private String[] lftSrchArr = new String[]{"a", "b", "c", "d", "e", "f", "g", "h", "i",
            "j", "k", "l", "m"};
    final private String[] lftIISrchArr = new String[]{"?", "&", "-", "_", "0", "1", "2", "3", "4"};
    final private String[] rgtSrchArr = new String[]{"#", "!", ".", "!", "5", "6", "7", "8", "9"};
    final private String[] rgtIISrchArr = new String[]{"n", "o", "p", "q", "r", "s", "t", "u", "v",
            "w", "x", "y", "z"};

    private ArrayList<String> pkgNmsArlst;
    private ArrayAdapter<String> apAdr;
    private List<ResolveInfo> pkgLst;
    private PackageManager pkgMngr;

    /* the global search string */
    private String srchStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setNavigationBarColor(0xff303030);

        /* Get UI Elements */
        tvEmptyLst = findViewById(R.id.tvEmptyLst);
        lftSrchLstVw = findViewById(R.id.lftSrchLstVw);
        lftIISrchLstVw = findViewById(R.id.lftIISrchLstVw);
        apLst = findViewById(R.id.apLst);
        rgtSrchLstVw = findViewById(R.id.rgtSrchLstVw);
        rgtIISrchLstVw = findViewById(R.id.rgtIISrchLstVw);
        alrtDlgBldr = new AlertDialog.Builder(this);

        /* get the system package manager */
        pkgMngr = getPackageManager();

        /* array list for storing the package names i.e. org.biotstoiq.launch */
        pkgNmsArlst = new ArrayList<>();

        /* array adapter which will be used to populate the main list */
        apAdr = new ArrayAdapter<>(this,
                R.layout.main_listview, R.id.mnTxtVw, new ArrayList<>());

        /* left search textview list */
        ArrayAdapter<String> lftSrchAdptr = new ArrayAdapter<>(this,
                R.layout.main_listview, R.id.mnTxtVw, lftSrchArr);
        ArrayAdapter<String> lftIISrchAdptr = new ArrayAdapter<>(this,
                R.layout.main_listview, R.id.mnTxtVw, lftIISrchArr);

        /* right search textview list */
        ArrayAdapter<String> rgtSrchAdptr = new ArrayAdapter<>(this,
                R.layout.main_listview, R.id.mnTxtVw, rgtSrchArr);
        ArrayAdapter<String> rgtIISrchAdptr = new ArrayAdapter<>(this,
                R.layout.main_listview, R.id.mnTxtVw, rgtIISrchArr);

        lftSrchLstVw.setAdapter(lftSrchAdptr);
        lftIISrchLstVw.setAdapter(lftIISrchAdptr);
        rgtSrchLstVw.setAdapter(rgtSrchAdptr);
        rgtIISrchLstVw.setAdapter(rgtIISrchAdptr);

        /* get all the package names into the list */
        updtApplst();

        /* get all preferences */
        prfs  = PreferenceManager.getDefaultSharedPreferences(this);
        prfEdtr = prfs.edit();

        /* update the search string and call the filter function */
        lftSrchLstVw.setOnItemClickListener((adapterView, view, i, l) -> {
            if (apLst.getCount() < 2) return;
            srchStr = srchStr.concat((String) adapterView.getItemAtPosition(i));
            fltrAppLst();
        });
        lftIISrchLstVw.setOnItemClickListener((adapterView, view, i, l) -> {
            if (apLst.getCount() < 2) return;
            srchStr = srchStr.concat((String) adapterView.getItemAtPosition(i));
            fltrAppLst();
        });

        /* launch the app */
        apLst.setOnItemClickListener((adapterView, view, i, l) -> lnch(pkgNmsArlst.get(i)));

        /* try to open the app's settings */
        apLst.setOnItemLongClickListener((adapterView, view, i, l) -> {
            try {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + pkgNmsArlst.get(i)));
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                ftchAlAps();
            }
            return true;
        });

        /* update the search string and call the filter function */
        rgtSrchLstVw.setOnItemClickListener((adapterView, view, i, l) -> {
            if (apLst.getCount() < 2) return;
            srchStr = srchStr.concat((String) adapterView.getItemAtPosition(i));
            fltrAppLst();
        });
        rgtIISrchLstVw.setOnItemClickListener((adapterView, view, i, l) -> {
            if (apLst.getCount() < 2) return;
            srchStr = srchStr.concat((String) adapterView.getItemAtPosition(i));
            fltrAppLst();
        });

        lftSrchLstVw.setOnItemLongClickListener((adapterView, view, i, l) -> {
            String key = (String) adapterView.getItemAtPosition(i);
            bldLngPrsFlow(key);
            return true;
        });

        lftIISrchLstVw.setOnItemLongClickListener((adapterView, view, i, l) -> {
            String key = (String) adapterView.getItemAtPosition(i);
            bldLngPrsFlow(key);
            return true;
        });

        rgtSrchLstVw.setOnItemLongClickListener((adapterView, view, i, l) -> {
            String key = (String) adapterView.getItemAtPosition(i);
            bldLngPrsFlow(key);
            return true;
        });

        rgtIISrchLstVw.setOnItemLongClickListener((adapterView, view, i, l) -> {
            String key = (String) adapterView.getItemAtPosition(i);
            bldLngPrsFlow(key);
            return true;
        });
    }

    void bldLngPrsFlow (String key) {
        String pkgNm = prfs.getString(key, "");
        if (!pkgNm.equals("")) {
            String apNm;
            apNm = getApNmFrmPkgNm(pkgNm);
            if (apNm.equals("")) {
                chsLngPrsApDlg(key);
            } else {
                lnchOrRmvApDlg(key, apNm, pkgNm);
            }
        } else {
            chsLngPrsApDlg(key);
        }
    }

    void chsLngPrsApDlg (String key) {
        alrtDlgBldr = new AlertDialog.Builder(this);
        alrtDlgBldr.setView(R.layout.main_listview);
        alrtDlgBldr.setAdapter(apLst.getAdapter(), (dialogInterface, i1) -> {
            prfEdtr.putString(key, pkgNmsArlst.get(i1));
            prfEdtr.apply();
        });
        alrtDlgBldr.create().show();
    }

    void lnchOrRmvApDlg (String key, String apNm, String pkgNm) {
        alrtDlgBldr = new AlertDialog.Builder(this);
        alrtDlgBldr.setTitle(apNm);
        alrtDlgBldr.setPositiveButton(R.string.go, (dialogInterface, i1) -> {
            lnch(pkgNm);
        });
        alrtDlgBldr.setNegativeButton(R.string.rmv, (dialogInterface, i1) -> {
            prfEdtr.putString(key,"");
            prfEdtr.apply();
        });
        alrtDlgBldr.create().show();
    }

    String getApNmFrmPkgNm (String pkgNm) {
        try {
            return (String) pkgMngr.getApplicationLabel(pkgMngr.getApplicationInfo(pkgNm, 0));
        } catch (PackageManager.NameNotFoundException ne) {
            ne.printStackTrace();
            return "";
        }
    }

    void lnch(String nm) {
        startActivity(pkgMngr.getLaunchIntentForPackage(nm));
    }

    void updtApplst() {
        srchStr = "";
        /* fetch all the installed apps */
        pkgLst = pkgMngr.queryIntentActivities(
                new Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER), 0);

        /* sort the app list */
        Collections.sort(pkgLst, new ResolveInfo.DisplayNameComparator(pkgMngr));
    }

    void clrLsts() {
        apAdr.clear();
        pkgNmsArlst.clear();
    }

    void ftchAlAps() {

        /* clear the global search string */
        srchStr = "";

        /* clear the list before repopulating */
        clrLsts();

        /* add the apps names to the adapter, and the package name to the array list */
        for (ResolveInfo resolver : pkgLst) {
            String apNm = (String) resolver.loadLabel(pkgMngr);
            apAdr.add(apNm);
            pkgNmsArlst.add(resolver.activityInfo.packageName);
        }

        if(apAdr.getCount() < 1) {
            tvEmptyLst.setVisibility(View.VISIBLE);
            return;
        } else {
            tvEmptyLst.setVisibility(View.GONE);
        }
        shwAps();
    }

    void fltrAppLst() {

        clrLsts();

        /* check each package name and add only the ones that match the search
        string */
        for (ResolveInfo resolver : pkgLst) {
            String appNm = (String) resolver.loadLabel(pkgMngr);
            if (appNm.toLowerCase().contains(srchStr)) {
                apAdr.add(appNm);
                pkgNmsArlst.add(resolver.activityInfo.packageName);
            }
        }

        /* if only one app contains the search string, then launch it */
        if (apAdr.getCount() == 1) {
            lnch(pkgNmsArlst.get(0));
        } else if (apAdr.getCount() < 1) {
            tvEmptyLst.setVisibility(View.VISIBLE);
        } else {
            shwAps();
            tvEmptyLst.setVisibility(View.GONE);
        }
    }

    /* show the app name adapter as the app list */
    void shwAps() {
        apLst.setAdapter(apAdr);
    }

    @Override
    public void onBackPressed() {
        /* on back press, remove the last character of the string and filter app list */
        int strLen = srchStr.length();
        if (strLen <= 0) return;
        srchStr = srchStr.substring(0, strLen - 1);
        fltrAppLst();
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        /* if back key pressed for long, then refresh the app list */
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            updtApplst();
            ftchAlAps();
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        ftchAlAps();
    }
}