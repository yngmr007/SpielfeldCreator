package client;

import java.util.prefs.Preferences;

public class TokenStorage {
    private static final String TOKEN_KEY = "auth_token";
    
    public static void saveToken(String token) {
        Preferences.userRoot().put(TOKEN_KEY, token);
    }
    
    public static String getToken() {
        return Preferences.userRoot().get(TOKEN_KEY, null);
    }
    
    public static void clearToken() {
        Preferences.userRoot().remove(TOKEN_KEY);
    }
}
