package com.dannycode.staff;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffProfileRepo extends JpaRepository<StaffProfile, Long> {

    List<StaffProfile> findByBranchId(Long branchId);

    List<StaffProfile> findByIsAvailableTrue();

}
