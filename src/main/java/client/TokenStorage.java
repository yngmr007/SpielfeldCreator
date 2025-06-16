package client;

import java.util.prefs.Preferences;

public class TokenStorage {
    private static final String TOKEN_KEY = "auth_token";
    private static final String HOSTNAME = "hostname";
    
    public static void saveToken(String token, String hostname) {
        Preferences.userRoot().put(TOKEN_KEY, token);
        Preferences.userRoot().put(HOSTNAME, hostname);
    }

    public static String getToken() {
        return Preferences.userRoot().get(TOKEN_KEY, null);
    }

    public static String getHostname() { return Preferences.userRoot().get(HOSTNAME, null); }

}
