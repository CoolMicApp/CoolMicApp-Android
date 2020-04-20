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

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;

import cc.echonet.coolmicapp.Configuration.DialogIdentifier;
import cc.echonet.coolmicapp.Configuration.DialogState;
import cc.echonet.coolmicapp.Configuration.Profile;

public class Dialog {
    private static final String KEY_TITLE = "title";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_URL = "url";

    private final DialogIdentifier dialogIdentifier;
    private final Context context;
    private final Profile profile;

    public Dialog(DialogIdentifier dialogIdentifier, Context context, Profile profile) {
        this.dialogIdentifier = dialogIdentifier;
        this.context = context;
        this.profile = profile;
    }

    private String getString(String subkey) {
        return Utils.getStringByName(context, "popup_" + dialogIdentifier.toString().toLowerCase() + "_" + subkey);
    }

    public void show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(getString(KEY_TITLE));
        builder.setMessage(Html.fromHtml(getString(KEY_MESSAGE)));
        builder.setPositiveButton(android.R.string.ok, (dialogInterface, i) -> dialogInterface.dismiss());
        builder.setNeutralButton(R.string.popup_any_more, ((dialogInterface, i) -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(KEY_URL)));
            context.startActivity(intent);
        }));
        builder.show();
        profile.getDialogState(dialogIdentifier).shown();
    }

    public void showIfNecessary() {
        DialogState dialogState = profile.getDialogState(dialogIdentifier);
        boolean necessary = false;

        switch (dialogIdentifier) {
            case FIRST_TIME:
                necessary = !dialogState.hasEverShown();
                break;
            case NEW_VERSION:
                necessary = !dialogState.hasShownInThisVersion();
                break;
        }

        if (necessary)
            show();
    }
}
