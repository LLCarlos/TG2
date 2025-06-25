package com.leo.articleimporterservice.utils;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;

public class Utils {

    /**
     * Creates and returns a customized `RestTemplate` instance with a secure SSL configuration.
     * The custom `RestTemplate` is configured to:
     * - Trust all SSL certificates (using a `TrustAllStrategy`).
     * - Disable hostname verification (using `NoopHostnameVerifier`).
     * - Use a custom HTTP client with connection pooling for better performance on multiple requests.
     *
     * This method is useful when dealing with self-signed certificates or environments where SSL verification
     * needs to be bypassed (e.g., in testing environments). However, it should be used with caution in production
     * environments due to potential security risks.
     *
     * @return A `RestTemplate` configured with custom SSL settings and connection pooling.
     * @throws RuntimeException If an error occurs during the creation of the `RestTemplate`.
     */
    public static RestTemplate getCustomRestTemplate() {
        try {
            SSLContext sslContext = SSLContextBuilder.create()
                    .loadTrustMaterial(null, TrustAllStrategy.INSTANCE) // Trust all certificates
                    .build();

            SSLConnectionSocketFactory sslSocketFactory = SSLConnectionSocketFactoryBuilder.create()
                    .setSslContext(sslContext)
                    .setHostnameVerifier(NoopHostnameVerifier.INSTANCE) // Disable hostname verification
                    .build();

            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("https", sslSocketFactory)
                    .build();

            PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);


            CloseableHttpClient httpClient = HttpClients.custom()
                    .setConnectionManager(connectionManager)
                    .build();

            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
            return new RestTemplate(requestFactory);

        } catch (Exception e) {
            throw new RuntimeException("Error creating custom RestTemplate", e);
        }
    }
}
