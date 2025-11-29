package com.group13.EmployeeManager.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;

    public Job() {}

    public Job(Long id, String title) {
        this.id = id;
        this.title = title;
    }

    public Long getId() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }
}
