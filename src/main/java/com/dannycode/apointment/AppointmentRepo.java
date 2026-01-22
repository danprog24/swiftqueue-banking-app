// package com.dannycode.apointment;

// import java.time.LocalDateTime;
// import java.util.List;

// import org.springframework.data.jpa.repository.JpaRepository;
// import com.dannycode.branch.Branch;


// public interface AppointmentRepo extends JpaRepository<Appointment, Long> {

//     List<Appointment> findByUserId(Long userId);

//     List<Appointment> findByBranch(Branch branchId);

//     boolean existsByUserIdAndScheduledAt(Long userId, LocalDateTime scheduledAt);

// }


package com.dannycode.apointment;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.dannycode.branch.Branch;

public interface AppointmentRepo extends JpaRepository<Appointment, Long> {

    // Find all appointments by user ID
    List<Appointment> findByUserId(Long userId);

    // Find all appointments by branch
    List<Appointment> findByBranch(Branch branch);

    // Check if an appointment exists for a user at a specific time
    boolean existsByUserIdAndScheduledAt(Long userId, LocalDateTime scheduledAt);
}
