package cc.echonet.coolmicapp;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class AboutActivity extends Activity {

    ClipboardManager myClipboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        this.setupActionBar();

        String shortRev = BuildConfig.GIT_REVISION.substring(BuildConfig.GIT_REVISION.length()-8);

        ((TextView) findViewById(R.id.txtVersion)).setText(BuildConfig.VERSION_NAME);
        ((TextView) findViewById(R.id.txtBuildType)).setText(BuildConfig.BUILD_TYPE);
        ((TextView) findViewById(R.id.txtGITBranch)).setText(BuildConfig.GIT_BRANCH);
        ((TextView) findViewById(R.id.txtGITRevision)).setText(shortRev);
        ((TextView) findViewById(R.id.txtGITDirty)).setText(BuildConfig.GIT_DIRTY);

        myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
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
}
