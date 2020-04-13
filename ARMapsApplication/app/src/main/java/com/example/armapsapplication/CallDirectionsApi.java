package com.example.armapsapplication;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class CallDirectionsApi extends AsyncTask<String,Void,String> {
    Context mContext;

    public CallDirectionsApi(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    protected String doInBackground(String... strings) {
        String data = "";

        try {
            data = getJsonRoutes(strings[0]);
            Log.d("CallDirectionsApi", "doInBackground: obtained data from jsonroutes");
        } catch (Exception e) {
            Log.d("CallDirectionsApi", "doInBackground: ERROR " + e.toString());
        }

        return data;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

    }

    protected String getJsonRoutes(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;

        try {
            URL url = new URL(strUrl);

            // create http connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();

            // read data from url
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();

            String line = "";
            while ( (line = br.readLine()) != null ) {
                sb.append(line);
            }
            data = sb.toString();

            Log.d("CallDirectionsApi", "getJsonRoutes: obtained url " + data.toString());
            br.close();
        } catch (Exception e) {
            Log.d("CallDirectionsApi", "Error obtaining data from URL " + e.getLocalizedMessage());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }

        return data;
    }
}
