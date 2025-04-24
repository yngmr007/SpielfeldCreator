
package client;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

public class AuthClient {
    private static final String LOGIN_URL = "https://localhost:8443/auth";

    public static String login(String username, String password) throws IOException {
        disableSslVerification(); //prüft nicht, ob zertifikat im lokalen speicher vorhanden ist, da es das nicht ist :)

        URL loginUrl = new URL(LOGIN_URL);
        HttpsURLConnection loginConn = (HttpsURLConnection) loginUrl.openConnection();
        loginConn.setRequestMethod("POST");
        loginConn.setDoOutput(true);
        loginConn.setInstanceFollowRedirects(false); // Wichtig: Redirects manuell behandeln
        loginConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        String body = "username="+ username +"&password=" + password;
        try (OutputStream os = loginConn.getOutputStream()) {
            os.write(body.getBytes());
        }

        // 2. Header auswerten
        System.out.println(loginConn.getHeaderFields());
        Map<String, List<String>> headers = loginConn.getHeaderFields();
        String setCookieHeader = loginConn.getHeaderField("Set-Cookie");

        return setCookieHeader;//sessionCookie;
    }


    static void disableSslVerification() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() { return null; }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                        public void checkServerTrusted(X509Certificate[] certs, String authType) { }
                    }
            };

            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Hostname-Check deaktivieren (z. B. localhost ohne gültiges Zertifikat)
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}