package com.devpedia.watchapedia.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticSearchConfig {

    @Value("${elasticsearch.host}")
    public String HOST;
    @Value("${elasticsearch.port}")
    public int PORT;
    @Value("${elasticsearch.scheme}")
    public String SCHEME;

    @Bean
    public RestHighLevelClient restHighLevelClient() {
        return new RestHighLevelClient(RestClient.builder(new HttpHost(HOST, PORT, SCHEME)));
    }
}
