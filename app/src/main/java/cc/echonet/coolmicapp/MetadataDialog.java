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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

abstract class MetadataDialog<A extends MetadataDialog.Adapter> {
    abstract static class Adapter extends ArrayAdapter<String> {
        protected final @NotNull Profile profile;

        abstract protected @NotNull List<@NotNull String> getKeys();
        abstract protected void setMetadata(@NotNull String key, @Nullable String value);
        @Contract("_, !null -> !null")
        abstract protected String getValue(@NotNull String key, @Nullable String def);
        abstract protected String getKeyDisplayName(@NotNull String key);

        public Adapter(@NonNull Context context, @NotNull Profile profile) {
            super(context, R.layout.metadata_list_entry, R.id.metadata_key, new ArrayList<>());
            this.profile = profile;
            reloadData();
        }

        public void reloadData() {
            clear();
            addAll(getKeys());
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            final @NonNull View view = super.getView(position, convertView, parent);
            final @NonNull String key = Objects.requireNonNull(super.getItem(position));
            final @NonNull String value = getValue(key, "");
            final @NonNull String keyDisplayName = getKeyDisplayName(key);

            ((TextView)view.findViewById(R.id.metadata_key)).setText(keyDisplayName);
            ((TextView)view.findViewById(R.id.metadata_value)).setText(value);

            view.setOnClickListener(v -> PromptDialog.prompt(getContext(), keyDisplayName, value, result -> setMetadata(key, result)));

            view.findViewById(R.id.metadata_delete_key).setOnClickListener(v -> setMetadata(key, null));

            return view;
        }
    }

    protected final @NotNull Context context;
    protected final @NotNull Profile profile;
    protected final @NotNull AlertDialog.Builder builder;
    protected final @NotNull A adapter;
    protected @Nullable Runnable onDone = null;

    protected abstract boolean customKeysAllowed();
    protected abstract @NotNull List<@NotNull String> getStandardKeys();

    protected void askKey(@NotNull String key) {
        PromptDialog.prompt(context, adapter.getKeyDisplayName(key), null, result -> adapter.setMetadata(key, result));
    }

    protected void addKey() {
        final @NotNull AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final @NotNull List<@NotNull String> keys = new ArrayList<>(getStandardKeys());
        final @NotNull String[] supported;

        for (Iterator<String> iterator = keys.iterator(); iterator.hasNext(); ) {
            final @NotNull String key = iterator.next();
            final @Nullable String value = adapter.getValue(key, null);
            if (value != null && !value.isEmpty())
                iterator.remove();
        }

        if (customKeysAllowed()) {
            keys.add(context.getString(R.string.metadata_custom));
        }

        supported = keys.toArray(new String[0]);

        for (int i = 0; i < supported.length; i++)
            supported[i] = adapter.getKeyDisplayName(supported[i]);

        builder.setItems(supported, (dialog, which) -> {
            dialog.dismiss();
            if (customKeysAllowed() && which == (supported.length - 1)) {
                PromptDialog.prompt(context, context.getString(R.string.metadata_key_name), null, this::askKey);
            } else {
                askKey(supported[which]);
            }
        });

        builder.show();
    }

    protected void setup() {
        final @NotNull ListView listView = new ListView(context);

        listView.setAdapter(adapter);

        builder.setView(listView);

        builder.setCancelable(true);

        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> done());
        builder.setNeutralButton(R.string.metadata_add, null);

        builder.setOnCancelListener(dialog -> done());
    }

    protected void done() {
        if (onDone != null)
            onDone.run();
    }

    @Contract(pure = true)
    public MetadataDialog(@NotNull Context context, @NotNull Profile profile, @NotNull A adapter) {
        this.context = context;
        this.profile = profile;
        this.builder = new AlertDialog.Builder(context);
        this.adapter = adapter;
        setup();
    }

    public void show() {
        final @NotNull AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> addKey()));
        dialog.show();
    }

    public void setOnDone(@Nullable Runnable onDone) {
        this.onDone = onDone;
    }
}
