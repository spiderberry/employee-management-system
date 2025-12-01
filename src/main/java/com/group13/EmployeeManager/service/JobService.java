package com.group13.EmployeeManager.service;

import com.group13.EmployeeManager.entity.Job;
import com.group13.EmployeeManager.repository.JobRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobService {

    private final JobRepository jobRepository;
    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public Job findJobById(Long id) {
        return jobRepository.findById(id).get();
    }

    public Job findJobByTitle(String jobTitle) {
        return jobRepository.findJobByTitle(jobTitle);
    }

    public List<Job> findAllJobs() {
        return jobRepository.findAll();
    }

    public Job updateJob(Job job) {
        return jobRepository.save(job);
    }

    public void deleteJobById(Job job) {
        jobRepository.deleteById(job.getId());
    }
}
