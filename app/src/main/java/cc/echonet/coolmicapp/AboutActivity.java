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
import android.os.Bundle;
import androidx.core.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class AboutActivity extends Activity {

    ClipboardManager myClipboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Button cmdAboutCopy;
        Button cmdOpenPrivacyPolicy;
        Button cmdOpenLicenses;

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

        myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        cmdAboutCopy = (Button) findViewById(R.id.cmdAboutCopy);
        cmdAboutCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCMDAboutCopy(v);
            }
        });

        cmdOpenPrivacyPolicy = (Button) findViewById(R.id.cmdOpenPrivacyPolicy);
        cmdOpenPrivacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCMDAboutOpenPP(v);
            }
        });

        cmdOpenLicenses = (Button) findViewById(R.id.cmdOpenLicenses);
        cmdOpenLicenses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCMDAboutOpenLicenses(v);
            }
        });
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
        int id = item.getItemId();

        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();

    }

    public void onCMDAboutCopy(View view) {
        ClipData myClip = ClipData.newPlainText("text", getString(
                R.string.aboutactivity_copy_string,
                BuildConfig.VERSION_NAME,
                BuildConfig.BUILD_TYPE,
                BuildConfig.GIT_BRANCH,
                BuildConfig.GIT_REVISION,
                BuildConfig.GIT_DIRTY
        ));

        myClipboard.setPrimaryClip(myClip);

        Toast.makeText(getApplicationContext(), R.string.aboutactivity_copied_string, Toast.LENGTH_SHORT).show();
    }

    public void onCMDAboutOpenPP(View view) {
        Intent helpIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_privacy_policy)));
        startActivity(helpIntent);
    }

    public void onCMDAboutOpenLicenses(View view) {
        Intent helpIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_licenses)));
        startActivity(helpIntent);
    }
}
