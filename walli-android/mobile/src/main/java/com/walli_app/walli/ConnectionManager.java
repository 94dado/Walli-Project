package com.walli_app.walli;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;

/**
 * Created by dado on 15/06/16.
 */
public class ConnectionManager {

//    WARNING: for anybody else arriving at this answer, this is a dirty, horrible hack and you must not use it for anything that matters.
//    SSL/TLS without authentication is worse than no encryption at all - reading and modifying your "encrypted" data is trivial for an attacker and you wouldn't even know it was happening.
//    Still with me? I feared so...
//    public ConnectionManager(){
//    http://stackoverflow.com/questions/3761737/https-get-ssl-with-android-and-self-signed-server-certificate/4008166#4008166
//        try {
//            SSLContext ctx = SSLContext.getInstance("TLS");
//            ctx.init(null, new TrustManager[]{new X509TrustManager(){
//                        @Override
//                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
//
//                        }
//                        @Override
//                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
//
//                        }
//
//                        @Override
//                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
//                            return new java.security.cert.X509Certificate[0];
//                        }
//                    }
//            }, null);
//            HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());
//            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
//                public boolean verify(String hostname, SSLSession session) {
//                    return true;
//                }
//            });
//        }catch (Exception e){
//            System.err.println("Errore inizializzazione hpps. Non andrà nulla");
//        }
//    }

    private String getQuery(Map <String,String> params){
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for ( Map.Entry <String,String> entry: params.entrySet())
        {
            if (first)
                first = false;
            else
                result.append("&");
            //provo a fare la decodifica dei valori e generare la stringa dei parametri da mandare
            try{
                result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            }catch(Exception e)
            {
                return "";
            }
        }

        return result.toString();
    }

    public String POST(String uri, Map<String,String> parameters){
        try{
            URL url = new URL(uri);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            String param = getQuery(parameters);
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            DataOutputStream stream = new DataOutputStream(connection.getOutputStream());
            stream.writeBytes(param);
            stream.flush();
            stream.close();
            int responseCode = connection.getResponseCode();
            if(responseCode == 200){
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                StringBuilder builder = new StringBuilder();
                while((line = reader.readLine())!=null){
                    builder.append(line);
                }
                reader.close();
                return builder.toString();
            }else{
                return Integer.toString(responseCode);
            }
        }catch(Exception e){
            System.err.println(e.toString());
            return null;
        }
    }
}

//    public String httpPOST(String uri, Map<String,String> parameters){
//        try{
//            URL url = new URL(uri);
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//            conn.setRequestMethod("POST");
//            conn.setDoInput(true);
//            conn.setDoOutput(true);
//            OutputStream os = conn.getOutputStream();
//            BufferedWriter writer = new BufferedWriter(
//                    new OutputStreamWriter(os, "UTF-8"));
//            writer.write(getQuery(parameters));
//            writer.flush();
//            writer.close();
//            os.close();
//            //eseguo la richiesta
//            conn.connect();
//            //ora mi sbatto a ricevere la risposta
//            int responseCode = conn.getResponseCode();
//            if (responseCode == 200){
//                //tutto andato per il meglio
//                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//                String line;
//                StringBuilder responseOutput = new StringBuilder();
//                while((line = br.readLine()) != null ) {
//                    responseOutput.append(line);
//                }
//                br.close();
//                return responseOutput.toString();
//            }else{
//                return Integer.toString(responseCode);
//            }
//        }catch(Exception e){
//            System.err.println(e.toString());
//            return "";
//        }
