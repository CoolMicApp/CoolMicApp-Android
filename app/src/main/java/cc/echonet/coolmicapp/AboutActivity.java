/*
 *      Copyright (C) Jordan Erickson                     - 2014-2020,
 *      Copyright (C) Löwenfelsen UG (haftungsbeschränkt) - 2015-2020
 *       on behalf of Jordan Erickson.
 *
 * This file is part of Cool Mic.
 *
 * Cool Mic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cool Mic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cool Mic.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package cc.echonet.coolmicapp;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NavUtils;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

import cc.echonet.coolmicapp.Configuration.Manager;

public class AboutActivity extends Activity {
    private ClipboardManager myClipboard;
    private int extra = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final @NotNull Button cmdAboutCopy;
        final @NotNull Button cmdOpenPrivacyPolicy;
        final @NotNull Button cmdOpenLicenses;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        this.setupActionBar();

        String shortRev = BuildConfig.GIT_REVISION.substring(BuildConfig.GIT_REVISION.length() - 8);

        ((TextView) findViewById(R.id.txtVersion)).setText(BuildConfig.VERSION_NAME);
        ((TextView) findViewById(R.id.txtBuildType)).setText(BuildConfig.BUILD_TYPE);
        ((TextView) findViewById(R.id.txtBuildTS)).setText(BuildConfig.BUILD_TS);
        ((TextView) findViewById(R.id.txtGITBranch)).setText(BuildConfig.GIT_BRANCH);
        ((TextView) findViewById(R.id.txtGITRevision)).setText(shortRev);
        ((TextView) findViewById(R.id.txtGITDirty)).setText(BuildConfig.GIT_DIRTY);
        ((TextView) findViewById(R.id.txtAPILevel)).setText(String.format(Locale.ROOT, "%d", Build.VERSION.SDK_INT));
        ((TextView) findViewById(R.id.txtSystemArch)).setText(System.getProperty("os.arch"));

        ((TextView) findViewById(R.id.txtVersion)).setOnClickListener(v -> {
            extra++;
            if (extra == 6) {
                Toast.makeText(AboutActivity.this, "yes", Toast.LENGTH_SHORT).show();
                new Manager(this).getGlobalConfiguration().setDeveloperMode(true);
            }
        });

        myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        cmdAboutCopy = findViewById(R.id.cmdAboutCopy);
        cmdAboutCopy.setOnClickListener(this::onCMDAboutCopy);

        cmdOpenPrivacyPolicy = findViewById(R.id.cmdOpenPrivacyPolicy);
        cmdOpenPrivacyPolicy.setOnClickListener(this::onCMDAboutOpenPP);

        cmdOpenLicenses = findViewById(R.id.cmdOpenLicenses);
        cmdOpenLicenses.setOnClickListener(this::onCMDAboutOpenLicenses);
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void onCMDAboutCopy(View view) {
        ClipData myClip = ClipData.newPlainText("text", getString(
                R.string.aboutactivity_copy_string,
                BuildConfig.VERSION_NAME,
                BuildConfig.BUILD_TYPE,
                BuildConfig.GIT_BRANCH,
                BuildConfig.GIT_REVISION,
                BuildConfig.GIT_DIRTY,
                Build.VERSION.SDK_INT,
                System.getProperty("os.arch")
        ));

        myClipboard.setPrimaryClip(myClip);

        Toast.makeText(getApplicationContext(), R.string.aboutactivity_copied_string, Toast.LENGTH_SHORT).show();
    }

    private void onCMDAboutOpenPP(View view) {
        Intent helpIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_privacy_policy)));
        startActivity(helpIntent);
    }

    private void onCMDAboutOpenLicenses(View view) {
        Intent helpIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_licenses)));
        startActivity(helpIntent);
    }
}
