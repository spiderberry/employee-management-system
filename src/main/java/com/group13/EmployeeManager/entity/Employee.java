package com.group13.EmployeeManager.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private LocalDate hireDate;
    private double salary;
    private String socialSecurityNumber;
    @ManyToOne
    @JoinColumn(name = "job_title_id")
    private Job jobTitle;
    @ManyToOne
    @JoinColumn(name = "division_id")
    private Division division;
    @OneToOne
    @JoinColumn(name = "payroll_id")
    private Payroll payroll;

    public Employee() {}
    public Employee(Long id, String name, String email, LocalDate hireDate, double salary, String socialSecurityNumber) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.hireDate = hireDate;
        this.salary = salary;
        this.socialSecurityNumber = socialSecurityNumber;
    }

    public Division getDivision() {
        return division;
    }

    public void setDivision(Division division) {
        this.division = division;
    }

    public Job getJobTitle() {
        return jobTitle;
    }

    public double getPayForMonthByJob() {
        return salary + (salary * 0.1);
    }

    public double getPayForMonthByDivision() {
        return salary + (salary * 0.1);
    }

    public void setJobTitle(Job jobTitle) {
        this.jobTitle = jobTitle;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public Payroll getPayroll() {
        return payroll;
    }

    public void setPayroll(Payroll payroll) {
        this.payroll = payroll;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public String getSocialSecurityNumber() {
        return socialSecurityNumber;
    }

    public void setSocialSecurityNumber(String socialSecurityNumber) {
        this.socialSecurityNumber = socialSecurityNumber;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "\nid: " + id +
                "\nname: " + name + '\'' +
                "\nemail: " + email + '\'' +
                "\nhireDate: " + hireDate +
                "\nsalary: " + salary +
                "\nsocialSecurityNumber: " + socialSecurityNumber + '\'' +
                "\njobTitle: " + jobTitle +
                "\ndivision: " + division +
                "\npayroll: " + payroll +
                '}';
    }
}
