package org.zephyrsoft.sdbviewer;

import android.app.Application;

import org.acra.ACRA;
import org.acra.annotation.AcraCore;
import org.acra.annotation.AcraDialog;
import org.acra.annotation.AcraHttpSender;
import org.acra.data.StringFormat;
import org.acra.sender.HttpSender;
import org.zephyrsoft.sdbviewer.fetch.SDBFetcher;

import static org.acra.ReportField.ANDROID_VERSION;
import static org.acra.ReportField.APP_VERSION_CODE;
import static org.acra.ReportField.APP_VERSION_NAME;
import static org.acra.ReportField.BRAND;
import static org.acra.ReportField.CRASH_CONFIGURATION;
import static org.acra.ReportField.INSTALLATION_ID;
import static org.acra.ReportField.LOGCAT;
import static org.acra.ReportField.PACKAGE_NAME;
import static org.acra.ReportField.PHONE_MODEL;
import static org.acra.ReportField.PRODUCT;
import static org.acra.ReportField.REPORT_ID;
import static org.acra.ReportField.SHARED_PREFERENCES;
import static org.acra.ReportField.STACK_TRACE;
import static org.acra.ReportField.USER_APP_START_DATE;
import static org.acra.ReportField.USER_CRASH_DATE;

@AcraCore(reportFormat = StringFormat.JSON, reportContent = {
    ANDROID_VERSION, APP_VERSION_CODE, APP_VERSION_NAME, BRAND,
    CRASH_CONFIGURATION, INSTALLATION_ID, LOGCAT, PACKAGE_NAME, PHONE_MODEL, PRODUCT,
    REPORT_ID, SHARED_PREFERENCES, STACK_TRACE, USER_APP_START_DATE, USER_CRASH_DATE})
@AcraHttpSender(httpMethod = HttpSender.Method.POST,
    uri = "https://crashreport.zephyrsoft.org/")
@AcraDialog(resTitle = R.string.acra_title,
    resText = R.string.acra_text,
    resCommentPrompt = R.string.acra_comment_prompt)
public class SDBViewerApplication extends Application {

    /**
     * The topmost item's index in the song list.
     * Used to keep the scroll position after navigation or data reloads.
     */
    private int firstVisiblePosition = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        ACRA.init(this);

        SDBFetcher.createAndRegisterInstance();
    }

    public int getFirstVisiblePosition() {
        return firstVisiblePosition;
    }

    public void setFirstVisiblePosition(int firstVisiblePosition) {
        this.firstVisiblePosition = firstVisiblePosition;
    }
}