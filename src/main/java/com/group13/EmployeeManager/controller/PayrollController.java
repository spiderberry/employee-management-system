package com.group13.EmployeeManager.controller;

import com.group13.EmployeeManager.entity.Payroll;
import com.group13.EmployeeManager.service.PayrollService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
class PayrollController {
    private final PayrollService payrollService;

    @Autowired
    public  PayrollController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    @PostMapping
    public Payroll createPayroll(@RequestBody Payroll payroll) {
        return payrollService.updatePayroll(payroll);
    }

    @PutMapping
    public Payroll updatePayroll(@RequestBody Payroll payroll) {
        return payrollService.updatePayroll(payroll);
    }
}
