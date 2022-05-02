package org.zephyrsoft.sdbviewer;

import android.app.Application;

import org.acra.ACRA;
import org.acra.config.CoreConfigurationBuilder;
import org.acra.config.DialogConfigurationBuilder;
import org.acra.config.HttpSenderConfigurationBuilder;
import org.acra.data.StringFormat;
import org.acra.sender.HttpSender;
import org.zephyrsoft.sdbviewer.db.DatabaseAccess;
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

public class SDBViewerApplication extends Application {

    /**
     * The topmost item's index in the song list.
     * Used to keep the scroll position after navigation or data reloads.
     */
    private int firstVisiblePosition = 0;

    private SDBFetcher sdbFetcher;

    @Override
    public void onCreate() {
        super.onCreate();

        CoreConfigurationBuilder builder = new CoreConfigurationBuilder()
            .withBuildConfigClass(BuildConfig.class)
            .withReportFormat(StringFormat.JSON)
            .withReportContent(ANDROID_VERSION, APP_VERSION_CODE, APP_VERSION_NAME, BRAND,
                CRASH_CONFIGURATION, INSTALLATION_ID, LOGCAT, PACKAGE_NAME, PHONE_MODEL, PRODUCT,
                REPORT_ID, SHARED_PREFERENCES, STACK_TRACE, USER_APP_START_DATE, USER_CRASH_DATE)
            .withPluginConfigurations(new DialogConfigurationBuilder()
                    .withTitle(getString(R.string.acra_title))
                    .withText(getString(R.string.acra_text))
                    .withCommentPrompt(getString(R.string.acra_comment_prompt))
                    .withEnabled(true)
                    .build(),
                new HttpSenderConfigurationBuilder()
                    .withHttpMethod(HttpSender.Method.POST)
                    .withUri("https://crashreport.zephyrsoft.org/")
                    .withEnabled(true)
                    .build());

        ACRA.init(this);

        // create services and connect them:
        DatabaseAccess databaseAccess = new DatabaseAccess(this);
        sdbFetcher = new SDBFetcher(databaseAccess);
    }

    public int getFirstVisiblePosition() {
        return firstVisiblePosition;
    }

    public void setFirstVisiblePosition(int firstVisiblePosition) {
        this.firstVisiblePosition = firstVisiblePosition;
    }

    public SDBFetcher getSdbFetcher() {
        return sdbFetcher;
    }
}