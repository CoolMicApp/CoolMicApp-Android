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
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.StringRes;
import androidx.core.app.NavUtils;
import cc.echonet.coolmicapp.Configuration.*;
import cc.echonet.coolmicdspjava.Wrapper;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class AboutActivity extends Activity {
    private ClipboardManager myClipboard;
    private int extra = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final @NotNull GlobalConfiguration globalConfiguration = new Manager(this).getGlobalConfiguration();
        final @NotNull View developerModeLabel;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        this.setupActionBar();

        developerModeLabel = findViewById(R.id.lblDeveloperMode);

        String shortRev = BuildConfig.GIT_REVISION.substring(BuildConfig.GIT_REVISION.length() - 8);

        ((TextView) findViewById(R.id.txtVersion)).setText(BuildConfig.VERSION_NAME);
        ((TextView) findViewById(R.id.txtBuildType)).setText(BuildConfig.BUILD_TYPE);
        ((TextView) findViewById(R.id.txtBuildTS)).setText(BuildConfig.BUILD_TS);
        ((TextView) findViewById(R.id.txtGITBranch)).setText(BuildConfig.GIT_BRANCH);
        ((TextView) findViewById(R.id.txtGITRevision)).setText(shortRev);
        ((TextView) findViewById(R.id.txtGITDirty)).setText(BuildConfig.GIT_DIRTY);
        ((TextView) findViewById(R.id.txtAPILevel)).setText(String.format(Locale.ROOT, "%d", Build.VERSION.SDK_INT));
        ((TextView) findViewById(R.id.txtSystemArch)).setText(System.getProperty("os.arch"));

        findViewById(R.id.txtVersion).setOnClickListener(v -> {
            extra++;
            if (extra == 6) {
                Toast.makeText(AboutActivity.this, "yes", Toast.LENGTH_SHORT).show();
                globalConfiguration.setDeveloperMode(true);
                developerModeLabel.setVisibility(View.VISIBLE);
            }
        });

        developerModeLabel.setVisibility(globalConfiguration.getDeveloperMode() ? View.VISIBLE : View.GONE);
        developerModeLabel.setOnClickListener(v -> {
            globalConfiguration.setDeveloperMode(false);
            developerModeLabel.setVisibility(View.GONE);
        });

        myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        findViewById(R.id.cmdAboutCopy).setOnClickListener(this::onCMDAboutCopy);
        findViewById(R.id.cmdDebugCopy).setOnClickListener(this::onCMDDebugCopy);
        findViewById(R.id.cmdOpenPrivacyPolicy).setOnClickListener(this::onCMDAboutOpenPP);
        findViewById(R.id.cmdOpenLicenses).setOnClickListener(this::onCMDAboutOpenLicenses);
        findViewById(R.id.cmdOpenSponsor).setOnClickListener(this::onCMDAboutOpenSponsor);
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
        // Respond to the action bar's Up/Home button
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private @NotNull String getInfoAsString(boolean debug) {
        final @NotNull String base = getString(
                R.string.aboutactivity_copy_string,
                BuildConfig.VERSION_NAME,
                BuildConfig.BUILD_TYPE,
                BuildConfig.GIT_BRANCH,
                BuildConfig.GIT_REVISION,
                BuildConfig.GIT_DIRTY,
                Build.VERSION.SDK_INT,
                System.getProperty("os.arch")
        );

        if (!debug)
            return base;

        {
            final @NotNull Manager manager = new Manager(this);
            final @NotNull GlobalConfiguration globalConfiguration = manager.getGlobalConfiguration();
            final @NotNull Profile profile = manager.getCurrentProfile();
            final @NotNull Audio audio = profile.getAudio();
            final @NotNull Codec codec = profile.getCodec();
            final @NotNull Volume volume = profile.getVolume();

            return getString(R.string.aboutactivity_copy_string_debug, base,
                    globalConfiguration.getDeveloperMode(),
                    globalConfiguration.getCurrentProfileName(),
                    audio.getSampleRate(),
                    audio.getChannels(),
                    codec.getType(),
                    codec.getQuality(),
                    profile.getVUMeter().getInterval(),
                    volume.getLeft(),
                    volume.getRight(),
                    CMTS.isCMTSConnection(profile),
                    Wrapper.getStaticInitializationState()
            );
        }
    }

    private void onCMDAboutCopy(View view) {
        ClipData myClip = ClipData.newPlainText("text", getInfoAsString(false));
        myClipboard.setPrimaryClip(myClip);
        Toast.makeText(getApplicationContext(), R.string.aboutactivity_copied_string, Toast.LENGTH_SHORT).show();
    }

    private void onCMDDebugCopy(View view) {
        ClipData myClip = ClipData.newPlainText("text", getInfoAsString(true));
        myClipboard.setPrimaryClip(myClip);
        Toast.makeText(getApplicationContext(), R.string.aboutactivity_copied_string, Toast.LENGTH_SHORT).show();
    }

    private void goToURI(@StringRes int uri) {
        final Intent helpIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(uri)));
        startActivity(helpIntent);
    }

    private void onCMDAboutOpenPP(View view) {
        goToURI(R.string.url_privacy_policy);
    }

    private void onCMDAboutOpenLicenses(View view) {
        goToURI(R.string.url_licenses);
    }

    private void onCMDAboutOpenSponsor(View view) {
        goToURI(R.string.url_sponsor);
    }

}
