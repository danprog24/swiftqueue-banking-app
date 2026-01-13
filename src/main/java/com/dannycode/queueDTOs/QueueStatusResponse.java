package com.dannycode.queueDTOs;

import com.dannycode.queueMGT.PriorityLevel;
import com.dannycode.queueMGT.QueueStatus;

import lombok.*;

@Getter
@Setter
public class QueueStatusResponse {

    private String ticketNumber;
    private QueueStatus status;
    private PriorityLevel priorityLevel;
    private Integer position;
    private Integer estimatedWaitTime;

}
