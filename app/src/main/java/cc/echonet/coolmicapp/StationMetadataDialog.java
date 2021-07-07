/*
 *      Copyright (C) Jordan Erickson                     - 2014-2020,
 *      Copyright (C) Löwenfelsen UG (haftungsbeschränkt) - 2015-2021
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import cc.echonet.coolmicapp.Configuration.Profile;
import cc.echonet.coolmicapp.Configuration.Station;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StationMetadataDialog {
    private final static class Adapter extends ArrayAdapter<String> {
        private final @NotNull Profile profile;

        private static @NotNull List<@NotNull String> getKeys(@NotNull Profile profile) {
            return new ArrayList<>(profile.getStation().getMetadata().keySet());
        }

        public Adapter(@NonNull Context context, @NotNull Profile profile) {
            super(context, R.layout.metadata_list_entry, R.id.metadata_key, getKeys(profile));
            this.profile = profile;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            final @NonNull View view = super.getView(position, convertView, parent);
            final @NonNull String key = Objects.requireNonNull(super.getItem(position));
            final @NonNull String value = profile.getStation().getValue(key, "");
            final @NonNull String keyDisplayName = Station.getKeyDisplayName(key);

            ((TextView)view.findViewById(R.id.metadata_key)).setText(keyDisplayName);
            ((TextView)view.findViewById(R.id.metadata_value)).setText(value);

            view.setOnClickListener(v -> PromptDialog.prompt(getContext(), keyDisplayName, value, result -> setMetadata(key, result)));

            view.findViewById(R.id.metadata_delete_key).setOnClickListener(v -> setMetadata(key, null));

            return view;
        }

        public void reloadData() {
            clear();
            addAll(getKeys(profile));
            notifyDataSetChanged();
        }

        public void setMetadata(@NotNull String key, @Nullable String value) {
            profile.edit();
            profile.getStation().setValue(key, value);
            profile.apply();
            reloadData();
        }
    }

    private final @NotNull AlertDialog.Builder builder;
    private @Nullable Runnable onDone = null;

    private void done() {
        if (onDone != null)
            onDone.run();
    }

    public StationMetadataDialog(@NotNull Context context, @NotNull Profile profile) {
        final @NotNull Adapter adapter = new Adapter(context, profile);
        final @NotNull ListView listView;

        this.builder = new AlertDialog.Builder(context);

        listView = new ListView(context);

        listView.setAdapter(adapter);

        builder.setView(listView);

        builder.setCancelable(true);

        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> done());

        builder.setOnCancelListener(dialog -> done());
    }

    public void show() {
        final @NotNull AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void setOnDone(@Nullable Runnable onDone) {
        this.onDone = onDone;
    }
}
