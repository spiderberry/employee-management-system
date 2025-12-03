package com.group13.EmployeeManager.ui.client;

import com.group13.EmployeeManager.entity.Division;
import com.group13.EmployeeManager.entity.Job;
import com.group13.EmployeeManager.entity.Payroll;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class BackendClient {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String baseUrl = "http://localhost:8080";

    public List<Job> fetchJobs() {
        try {
            ResponseEntity<List<Job>> response = restTemplate.exchange(
                    baseUrl + "/jobs",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {});
            return Optional.ofNullable(response.getBody()).orElseGet(List::of);
        } catch (RestClientException ex) {
            return List.of();
        }
    }

    public Job fetchJobByTitle(String title) {
        try {
            return restTemplate.getForObject(baseUrl + "/jobs/{title}", Job.class, title);
        } catch (RestClientException ex) {
            return null;
        }
    }

    public Job createJob(Job job) {
        try {
            return restTemplate.postForObject(baseUrl + "/jobs", job, Job.class);
        } catch (RestClientException ex) {
            return null;
        }
    }

    public List<Division> fetchDivisions() {
        try {
            ResponseEntity<List<Division>> response = restTemplate.exchange(
                    baseUrl + "/divisions",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {});
            return Optional.ofNullable(response.getBody()).orElseGet(List::of);
        } catch (RestClientException ex) {
            return List.of();
        }
    }

    public Division fetchDivisionByName(String name) {
        try {
            return restTemplate.getForObject(baseUrl + "/divisions/{name}", Division.class, name);
        } catch (RestClientException ex) {
            return null;
        }
    }

    public Division createDivision(Division division) {
        try {
            return restTemplate.postForObject(baseUrl + "/divisions", division, Division.class);
        } catch (RestClientException ex) {
            return null;
        }
    }

    public Map<String, Object> postPayroll(Map<String, Object> payload) {
        try {
            return restTemplate.postForObject(baseUrl + "/", payload, Map.class);
        } catch (RestClientException ex) {
            return null;
        }
    }
}
