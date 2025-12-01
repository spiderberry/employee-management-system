package com.group13.EmployeeManager.controller;

import com.group13.EmployeeManager.entity.Job;
import com.group13.EmployeeManager.repository.JobRepository;
import com.group13.EmployeeManager.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/jobs")
class JobController {
    private final JobService jobService;

    @Autowired
    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping("/{title}")
    public Job findJobByTitle(@PathVariable String title) {
        return jobService.findJobByTitle(title);
    }

    @GetMapping
    public List<Job> findAllJobs() {
        return jobService.findAllJobs();
    }

    @PostMapping
    public Job saveJob(@RequestBody Job job) {
        return jobService.updateJob(job);
    }

    @DeleteMapping
    public void deleteJob(@RequestBody Job job) {
        jobService.deleteJobById(job);
    }

}
