package com.example.testtask;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class Api {

    private final RestTemplate restTemplate;
    private final Semaphore semaphore;

    public Api(TimeUnit timeUnit, int requestLimit) {
        this.semaphore = new Semaphore(requestLimit, true);

        long timeout = timeUnit.toMillis(1);
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(timeout);
                    semaphore.release(requestLimit - semaphore.availablePermits());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();

        ClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        this.restTemplate = new RestTemplate(factory);
    }

    public String createDocument(Object document, String signature) throws InterruptedException {
        semaphore.acquire();

        String url = "https://ismp.crpt.ru/api/v3/lk/documents/create";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Signature", signature);

        HttpEntity<Object> requestEntity = new HttpEntity<>(document, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

        return response.getBody();
    }
}
