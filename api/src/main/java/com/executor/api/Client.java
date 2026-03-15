package com.executor.api;

import com.executor.common.JobDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class Client {

    private final RestTemplate restTemplate;

    @Value("${worker.base-url}")
    private String executorBaseUrl;

    public Client(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean send(JobDTO job) {
        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(
                    executorBaseUrl + "/tasks",
                    job,
                    Void.class
            );

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}