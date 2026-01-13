package com.dannycode.apointment;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.dannycode.branch.Branch;


public interface AppointmentRepo extends JpaRepository<Appointment, Long> {

    List<Appointment> findByUserId(Long userId);

    List<Appointment> findByBranch(Branch branchId);

    boolean existByUserIdAndScheduledAt(Long userId, LocalDateTime scheduledAt);

}
