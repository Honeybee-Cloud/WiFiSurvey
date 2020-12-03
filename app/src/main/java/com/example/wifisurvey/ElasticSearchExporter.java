package com.example.wifisurvey;

import com.loopj.android.http.AsyncHttpClient;

public class ElasticSearchExporter {
    private AsyncHttpClient httpClient = null;

    public static class ESExporterOptions {
        protected String domain;
        protected int port;

        protected String username;

        public void ESExporterOptions() {

        }

        public int getPort() {
            return port;
        }

        public String getDomain() {
            return domain;
        }

        public String getUsername() {
            return username;
        }
    }

    private ESExporterOptions curConfig = null;

    public boolean config(ESExporterOptions options) {
        if (httpClient != null) {
            return false;
        }

        curConfig = options;

        httpClient = new AsyncHttpClient();

        return true;
    }

    public void upload() {
        assert curConfig != null;
        assert httpClient != null;


    }
}
