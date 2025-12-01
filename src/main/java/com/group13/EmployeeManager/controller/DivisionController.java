package com.group13.EmployeeManager.controller;

import com.group13.EmployeeManager.entity.Division;
import com.group13.EmployeeManager.service.DivisionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/divisions")
class DivisionController {
    private final DivisionService divisionService;

    @Autowired
    public DivisionController(DivisionService divisionService) {
        this.divisionService = divisionService;
    }

    @PostMapping
    public Division addDivision(@RequestBody Division division) {
        return divisionService.addDivision(division);
    }

    @GetMapping
    public List<Division> getAllDivisions() {
        return divisionService.findAllDivisions();
    }

    @DeleteMapping
    public void deleteDivision(@RequestBody Division division) {
        divisionService.deleteDivision(division);
    }

    @GetMapping("/{name}")
    public Division getDivisionById(@PathVariable String name) {
        return divisionService.findByName(name);
    }


}
