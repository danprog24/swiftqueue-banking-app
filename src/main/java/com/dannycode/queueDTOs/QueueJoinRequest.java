package com.dannycode.queueDTOs;

import lombok.*;

@Getter
@Setter
public class QueueJoinRequest {

    private Long branchId;
    private Long serviceId;
    private Long userId;

}
