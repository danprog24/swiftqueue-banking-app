package com.dannycode.queueMGT;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface QueueTicketRepo extends JpaRepository<QueueTicket, Long> {

    List<QueueTicket> findByBranchIdAndStatusOrderByPositionAsc(
            Long branchId,
            String status
    );

}
