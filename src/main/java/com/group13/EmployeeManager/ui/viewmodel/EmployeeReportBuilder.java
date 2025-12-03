package com.group13.EmployeeManager.ui.viewmodel;

import com.group13.EmployeeManager.entity.Employee;

import java.util.LinkedHashMap;
import java.util.List;

public class EmployeeReportBuilder {

    public String fullEmployeePay(List<Employee> employees) {
        StringBuilder sb = new StringBuilder();
        for (Employee e : employees) {
            sb.append("Employee #").append(e.getId() == null ? "?" : e.getId())
                    .append(" - ").append(defaultString(e.getName()))
                    .append(" | Job: ").append(e.getJobTitle() != null ? e.getJobTitle().getTitle() : "N/A")
                    .append(" | Division: ").append(e.getDivision() != null ? e.getDivision().getName() : "N/A")
                    .append(" | Salary: ").append(e.getSalary())
                    .append(" | Hire Date: ").append(e.getHireDate())
                    .append("\n");
            if (e.getPayroll() != null) {
                var p = e.getPayroll();
                sb.append("   Payroll ID ").append(p.getPayId())
                        .append(" | Pay Date: ").append(p.getPayDate())
                        .append(" | Earnings: ").append(p.getEarnings())
                        .append(" | State Tax: ").append(p.getStateTax())
                        .append(" | 401k: ").append(p.getRetire401k())
                        .append(" | Health: ").append(p.getHealthCare())
                        .append(" | Fed Tax: ").append(p.getFedTax())
                        .append(" | Fed Medical: ").append(p.getFedMedical())
                        .append(" | Fed SS: ").append(p.getFedSocialSecurity())
                        .append("\n");
            } else {
                sb.append("   No payroll data available\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public String totalPayByJob(List<Employee> employees) {
        var totals = new LinkedHashMap<String, Double>();
        for (Employee e : employees) {
            String key = e.getJobTitle() != null ? e.getJobTitle().getTitle() : "Unassigned";
            totals.put(key, totals.getOrDefault(key, 0.0) + e.getPayForMonthByJob());
        }
        StringBuilder sb = new StringBuilder("Total pay for month by job title:\n");
        totals.forEach((job, total) -> sb.append(" - ").append(job).append(": ").append(total).append("\n"));
        return sb.toString();
    }

    public String totalPayByDivision(List<Employee> employees) {
        var totals = new LinkedHashMap<String, Double>();
        for (Employee e : employees) {
            String key = e.getDivision() != null ? e.getDivision().getName() : "Unassigned";
            totals.put(key, totals.getOrDefault(key, 0.0) + e.getPayForMonthByDivision());
        }
        StringBuilder sb = new StringBuilder("Total pay for month by division:\n");
        totals.forEach((division, total) -> sb.append(" - ").append(division).append(": ").append(total).append("\n"));
        return sb.toString();
    }

    private String defaultString(String value) {
        return value != null ? value : "";
    }
}
