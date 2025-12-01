package com.group13.EmployeeManager.repository;

import com.group13.EmployeeManager.entity.Division;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DivisionRepository extends JpaRepository<Division, Long> {
    @Query("SELECT d FROM Division d WHERE d.name = :divisionName")
    Division findByDivisionName(String divisionName);
}
