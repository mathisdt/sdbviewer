package org.zephyrsoft.sdbviewer;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.google.zxing.Result;

import java.util.stream.Stream;

public class QRScannerActivity extends AppCompatActivity {
    private CodeScanner mCodeScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscanner);
        CodeScannerView scannerView = findViewById(R.id.scanner_view);
        mCodeScanner = new CodeScanner(this, scannerView);
        mCodeScanner.setDecodeCallback(this::checkResult);
        scannerView.setOnClickListener(view -> mCodeScanner.startPreview());
    }

    private void checkResult(Result result) {
        if (result.getText()!=null) {
            Stream.of(result.getText().split("\n"))
                .filter(line -> !line.trim().isEmpty()
                    && !line.trim().startsWith("#")
                    && line.contains("="))
                .map(line -> line.split("=", 2))
            .forEach(entry -> applyPreferenceIfValid(entry[0], entry[1]));

            runOnUiThread(() -> Toast.makeText(this, getText(R.string.settings_imported), Toast.LENGTH_LONG).show());
            finish();
        }
    }

    private void applyPreferenceIfValid(String name, String value) {
        Constants.AppPreference pref = Constants.AppPreference.get(name, this);
        if (pref == null) {
            Log.i(Constants.LOG_TAG,"no preference found for: " + name);
        } else if (!pref.checkPossibleValue(value)) {
            Log.i(Constants.LOG_TAG,"preference "+name + ": value invalid: " + value);
        } else {
            pref.applyValue(value, this);
            Log.i(Constants.LOG_TAG,"preference "+name + ": value applied: " + value);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCodeScanner.startPreview();
    }

    @Override
    protected void onPause() {
        mCodeScanner.releaseResources();
        super.onPause();
    }
}