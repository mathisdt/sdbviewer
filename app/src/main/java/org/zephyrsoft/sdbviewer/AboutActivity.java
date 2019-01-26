package org.zephyrsoft.sdbviewer;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {

    private String getVersionName() {
        try {
            return getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(Constants.LOG_TAG, "could not get version name from manifest: " + e.getMessage(), e);
            return "?";
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        setContentView(R.layout.activity_about);

        ((TextView) findViewById(R.id.versionTextView)).setText("SDB Viewer " + getVersionName());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Intent upIntent = NavUtils.getParentActivityIntent(this);
            if (upIntent == null || NavUtils.shouldUpRecreateTask(this, upIntent)) {
                // this activity is NOT part of this app's task, so create a new task when navigating up
                navigateUpTo(upIntent);
            } else {
                // this activity is part of this app's task
                finish();
            }

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
