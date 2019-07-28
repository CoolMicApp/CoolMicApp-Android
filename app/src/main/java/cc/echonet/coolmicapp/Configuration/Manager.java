package cc.echonet.coolmicapp.Configuration;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class Manager {
    static final String DEFAULT_PROFILE = "default";

    private Context context;
    private GlobalConfiguration globalConfiguration;

    public Manager(Context context) {
        this.context = context;
        this.globalConfiguration = new GlobalConfiguration(context);
    }

    public List<String> getProfileNames() {
        // TODO: Implement actual reading of the list...
        ArrayList<String> list = new ArrayList<>();
        list.add(DEFAULT_PROFILE);
        return list;
    }

    public Profile getDefaultProfile() {
        return getProfile(DEFAULT_PROFILE);
    }

    public Profile getProfile(String name) {
        return new Profile(context, name);
    }

    public GlobalConfiguration getGlobalConfiguration() {
        return globalConfiguration;
    }
}
