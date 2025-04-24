package client;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;


import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.ssl.SSLContextBuilder;


public class ApiClient {

    public static void uploadWithHttpClient(String token, String xmlData, String jsonData) throws IOException {
        try {

            SSLContext sslContext = SSLContextBuilder.create()
                    .loadTrustMaterial(new TrustAllStrategy())
                    .build();

            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext,
                    (hostname, session) -> true);

            try (CloseableHttpClient httpClient = HttpClients.custom()
                    .setConnectionManager(
                            PoolingHttpClientConnectionManagerBuilder.create()
                                    .setSSLSocketFactory(sslSocketFactory)
                                    .build()
                    )
                    .build()) {

                HttpPost post = new HttpPost("https://localhost:8443/api/xml/uploadgame");
                post.setHeader("Cookie", "JSESSIONID=" + token);

                MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                builder.addBinaryBody("xml", xmlData.getBytes(StandardCharsets.UTF_8), ContentType.APPLICATION_XML, "game.xml");
                builder.addTextBody("json", jsonData, ContentType.APPLICATION_JSON);

                post.setEntity(builder.build());

                httpClient.execute(post);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public static String sendAuthenticatedRequest(String url, String method, String requestBody) throws IOException {
        String token = TokenStorage.getToken();
        if (token == null) {
            throw new IllegalStateException("No authentication token available");
        }

        URL apiUrl = new URL(url);
        HttpsURLConnection conn = (HttpsURLConnection) apiUrl.openConnection();

        // Set up the connection
        conn.setRequestMethod(method);
        conn.setRequestProperty("Content-Type", "application/json");
        System.out.println("Token: " + token);
        conn.setRequestProperty("Cookie", "JSESSIONID=" + token);

        if (requestBody != null && (method.equals("POST") || method.equals("PUT"))) {
            conn.setDoOutput(true);

            try(OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

        }

        // Get response
        try(BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            System.out.println("HTTP Status: " + conn.getResponseCode());
            return response.toString();
        }
    }
}
