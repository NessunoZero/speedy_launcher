package org.biotstoiq.launch;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
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

    TextView tvEmptyLst;
    ListView lftSrchLstVw;
    ListView lftIISrchLstVw;
    ListView apLst;
    ListView rgtSrchLstVw;
    ListView rgtIISrchLstVw;

    final static String[] lftSrchArr = new String[]{"a", "b", "c", "d", "e", "f", "g", "h", "i",
            "j", "k", "l", "m"};
    final static String[] lftIISrchArr = new String[]{"-", "_", "0", "1", "2", "3", "4"};
    final static String[] rgtSrchArr = new String[]{".", "!", "5", "6", "7", "8", "9"};
    final static String[] rgtIISrchArr = new String[]{"n", "o", "p", "q", "r", "s", "t", "u", "v",
            "w", "x", "y", "z"};

    private ArrayList<String> pkgNmsArlst;
    private ArrayAdapter<String> apAdr;
    private PackageManager pkgMngr;

    private List<ResolveInfo> pkgLst;

    /* the global search string */
    static String srchStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Get UI Elements */
        tvEmptyLst = findViewById(R.id.tvEmptyLst);
        lftSrchLstVw = findViewById(R.id.lftSrchLstVw);
        lftIISrchLstVw = findViewById(R.id.lftIISrchLstVw);
        apLst = findViewById(R.id.apLst);
        rgtSrchLstVw = findViewById(R.id.rgtSrchLstVw);
        rgtIISrchLstVw = findViewById(R.id.rgtIISrchLstVw);

        /* get the system package manager */
        pkgMngr = getPackageManager();

        /* array list for storing the package names i.e. org.biotstoiq.launch */
        pkgNmsArlst = new ArrayList<>();

        /* array adapter which will be used to populate the main list */
        apAdr = new ArrayAdapter<>(this,
                R.layout.activity_app_lstvw, R.id.apTxtVw, new ArrayList<>());

        /* left search textview list */
        ArrayAdapter<String> lftSrchAdptr = new ArrayAdapter<>(this,
                R.layout.activity_srch_lstvw, R.id.srchTxtVw, lftSrchArr);
        ArrayAdapter<String> lftIISrchAdptr = new ArrayAdapter<>(this,
                R.layout.activity_srch_lstvw, R.id.srchTxtVw, lftIISrchArr);

        /* right search textview list */
        ArrayAdapter<String> rgtSrchAdptr = new ArrayAdapter<>(this,
                R.layout.activity_srch_lstvw, R.id.srchTxtVw, rgtSrchArr);
        ArrayAdapter<String> rgtIISrchAdptr = new ArrayAdapter<>(this,
                R.layout.activity_srch_lstvw, R.id.srchTxtVw, rgtIISrchArr);

        lftSrchLstVw.setAdapter(lftSrchAdptr);
        lftIISrchLstVw.setAdapter(lftIISrchAdptr);
        rgtSrchLstVw.setAdapter(rgtSrchAdptr);
        rgtIISrchLstVw.setAdapter(rgtIISrchAdptr);

        /* update the search string and call the filter function */
        lftSrchLstVw.setOnItemClickListener((adapterView, view, i, l) -> {
            if (apLst.getCount() < 2) return;
            srchStr = srchStr.concat(adapterView.getItemAtPosition(i).toString());
            fltrAppLst();
        });
        lftIISrchLstVw.setOnItemClickListener((adapterView, view, i, l) -> {
            if (apLst.getCount() < 2) return;
            srchStr = srchStr.concat(adapterView.getItemAtPosition(i).toString());
            fltrAppLst();
        });

        /* launch the app */
        apLst.setOnItemClickListener((adapterView, view, i, l) -> startActivity(pkgMngr.getLaunchIntentForPackage(pkgNmsArlst.get(i))));

        /* try to open the app's settings */
        apLst.setOnItemLongClickListener((adapterView, view, i, l) -> {
            try {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + pkgNmsArlst.get(i)));
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                ftchAlAps();
            }
            return false;
        });

        /* update the search string and call the filter function */
        rgtSrchLstVw.setOnItemClickListener((adapterView, view, i, l) -> {
            if (apLst.getCount() < 2) return;
            srchStr = srchStr.concat(adapterView.getItemAtPosition(i).toString());
            fltrAppLst();
        });
        rgtIISrchLstVw.setOnItemClickListener((adapterView, view, i, l) -> {
            if (apLst.getCount() < 2) return;
            srchStr = srchStr.concat(adapterView.getItemAtPosition(i).toString());
            fltrAppLst();
        });

    }

    void gtApLst() {
        srchStr = "";
        /* fetch all the installed apps */
        pkgLst = pkgMngr.queryIntentActivities(
                new Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER), 0);

        /* sort the app list */
        Collections.sort(pkgLst, new ResolveInfo.DisplayNameComparator(pkgMngr));

    }

    void ftchAlAps() {

        gtApLst();

        /* clear the list before repopulating */
        apAdr.clear();
        pkgNmsArlst.clear();

        /* add the apps names to the adapter, and the package name to the array list */
        for (ResolveInfo resolver : pkgLst) {
            String apNm = resolver.loadLabel(pkgMngr).toString();
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
        /* return if the search string is empty */
        if (srchStr.equals("")) {
            ftchAlAps();
            return;
        }

        /* clear the current lists */
        apAdr.clear();
        pkgNmsArlst.clear();

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
            startActivity(pkgMngr.getLaunchIntentForPackage(pkgNmsArlst.get(0)));
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
            ftchAlAps();
        }
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ftchAlAps();
    }
}