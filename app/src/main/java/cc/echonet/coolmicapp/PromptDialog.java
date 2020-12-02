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
import android.view.WindowManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

public final class PromptDialog {
    public interface ResultCallback {
        void result(@Nullable String result);
    }

    public static void prompt(@NonNull Context context, @NonNull String title, @Nullable String value, @NonNull ResultCallback resultCallback) {
        final @NonNull AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final @NonNull EditText editText = new EditText(context);
        final @NonNull AlertDialog dialog;

        if (value == null)
            value = "";

        editText.setText(value);
        editText.setSingleLine();
        editText.requestFocus();

        builder.setTitle(title);
        builder.setView(editText);

        builder.setOnCancelListener(ignored -> resultCallback.result(String.valueOf(editText.getText())));
        builder.setPositiveButton(android.R.string.ok, (dialog1, which) -> resultCallback.result(String.valueOf(editText.getText())));

        dialog = builder.create();
        editText.setOnEditorActionListener((v, actionId, event) -> {
            dialog.cancel();
            return true;
        });
        dialog.show();
        Objects.requireNonNull(dialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }
}
