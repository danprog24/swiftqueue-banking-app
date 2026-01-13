package com.dannycode.service;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRepo extends JpaRepository<ServiceEntity, Long> {

    List<ServiceEntity> findByBranchId(Long branchId);

}
