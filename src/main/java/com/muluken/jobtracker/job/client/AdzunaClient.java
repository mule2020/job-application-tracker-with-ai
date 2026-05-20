package com.muluken.jobtracker.job.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
@Slf4j
public class AdzunaClient {

    private final RestClient restClient;

    @Value("${adzuna.app-id}")
    private String appId;

    @Value("${adzuna.app-key}")
    private String appKey;

    @Value("${adzuna.base-url}")
    private String baseUrl;

    public AdzunaClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public Map<String, Object> searchJobs(String keyword, String location, int page) {

        log.info("Searching jobs: keyword={}, location={}, page={}", keyword, location, page);

        return restClient.get()
                .uri(baseUrl + "/ca/search/" + page, uriBuilder -> uriBuilder
                        .queryParam("app_id", appId)
                        .queryParam("app_key", appKey)
                        .queryParam("what", keyword)
                        .queryParam("where", location)
                        .queryParam("results_per_page", 10)
                        .queryParam("content-type", "application/json")
                        .build())
                .header("Accept", "application/json")
                .retrieve()
                .body(Map.class);
    }
}