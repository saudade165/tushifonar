package com.example.tushifonar;

import android.os.AsyncTask;
import android.util.Log;
import android.webkit.JavascriptInterface;

import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.tls.*;


public class Client{
    private OkHttpClient.Builder builder;
    private OkHttpClient client;
    private final String url = "https://130.162.253.73:8443";
    private final MainActivity activity;
    private String token;

    public Client(MainActivity activity) {
        this.activity = activity;
        try {
            InputStream inputStream = activity.getResources().openRawResource(R.raw.certificate);
            byte[] data = new byte[inputStream.available()];
            inputStream.read(data);

            X509Certificate certificate = Certificates.decodeCertificatePem(new String(data));
            HandshakeCertificates clientCertificates = new HandshakeCertificates.Builder()
                    .addTrustedCertificate(certificate)
                    .build();

            builder = new OkHttpClient.Builder()
                    .sslSocketFactory(clientCertificates.sslSocketFactory(), clientCertificates.trustManager())
                    .hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String s, SSLSession sslSession) {
                            return s.equals("130.162.253.73");
                        }
                    });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        client = builder.build();
    }

    @JavascriptInterface
    public void setToken(String token){
        this.token = token;
    }

    @JavascriptInterface
    public String userLogin(String phone, String password){
        Map<String, String> body = new HashMap<String, String>() {{
            put("phone", phone);
            put("password", password);
        }};

        Map<String, String> method = new HashMap<String, String>() {{
           put("method", "/auth/log");
        }};

        String id = UUID.randomUUID().toString();
        AsyncRequest request = new AsyncRequest(id);
        request.execute(method, body);
        return id;
    }

    @JavascriptInterface
    public String userRegister(String phone, String password){
        Map<String, String> body = new HashMap<String, String>() {{
            put("phone", phone);
            put("password", password);
        }};

        Map<String, String> method = new HashMap<String, String>() {{
            put("method", "/auth/reg");
        }};

        String id = UUID.randomUUID().toString();
        AsyncRequest request = new AsyncRequest(id);
        request.execute(method, body);
        return id;
    }

    @JavascriptInterface
    public String userCheckSession(){
        Map<String, String> body = new HashMap<String, String>() {{
            put("token", token);
        }};

        Map<String, String> method = new HashMap<String, String>() {{
            put("method", "/auth/check");
        }};

        String id = UUID.randomUUID().toString();
        AsyncRequest request = new AsyncRequest(id);
        request.execute(method, body);
        return id;
    }

    @JavascriptInterface
    public String reviewAdd(String phone, String rate, String text){
        Map<String, String> body = new HashMap<String, String>() {{
            put("token", token);
            put("phone", phone);
            put("rate", rate);
            put("text", text);
        }};

        Map<String, String> method = new HashMap<String, String>() {{
            put("method", "/review/add");
        }};

        String id = UUID.randomUUID().toString();
        AsyncRequest request = new AsyncRequest(id);
        request.execute(method, body);
        return id;
    }

    @JavascriptInterface
    public String reviewGet(String phone){
        Map<String, String> body = new HashMap<String, String>() {{
            put("token", token);
            put("phone", phone);
        }};

        Map<String, String> method = new HashMap<String, String>() {{
            put("method", "/review/get");
        }};

        String id = UUID.randomUUID().toString();
        AsyncRequest request = new AsyncRequest(id);
        request.execute(method, body);
        return id;
    }

    class AsyncRequest extends AsyncTask<Map<String, String>, Void, Void> {
        private String id;
        private String response;
        public AsyncRequest(String id) {
            this.id = id;
        }

        @Override
        protected Void doInBackground (Map<String,String>... args){
            FormBody.Builder body = new FormBody.Builder();
            for(Map.Entry<String, String> entry: args[1].entrySet()){
                body.add(entry.getKey(), entry.getValue());
            }

            Request request = new Request.Builder()
                    .url(url + args[0].get("method"))
                    .post(body.build())
                    .build();
            try{
                Response response = client.newCall(request).execute();
                this.response = response.body().string();
            } catch (Exception e){
                this.response = "{\"status\":\"error\", \"code\": 0}";
                Log.println(Log.DEBUG, "CLIENT", e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            activity.web.loadUrl("javascript:addResponse('" + id + "', '" + response + "');");
        }
    }
}
