package com.group13.EmployeeManager.service;

import com.group13.EmployeeManager.entity.Division;
import com.group13.EmployeeManager.repository.DivisionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DivisionService {
    private final DivisionRepository divisionRepository;


    public DivisionService(DivisionRepository divisionRepository) {
        this.divisionRepository = divisionRepository;
    }

    public Division findByName(String divisionName) {
        return divisionRepository.findByDivisionName(divisionName);
    }

    public List<Division> findAllDivisions() {
        return divisionRepository.findAll();
    }

    public Division addDivision(Division division) {
        return divisionRepository.save(division);
    }

    public void deleteDivision(Division division) {
        divisionRepository.deleteById(division.getId());
    }
}
