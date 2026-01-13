package com.dannycode.AppointmentDTOs;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppointmentRequest {

    private Long branchId;
    private Long serviceId;
    private String scheduledAt; 

}
