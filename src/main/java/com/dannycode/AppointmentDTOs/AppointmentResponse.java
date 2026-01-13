package com.dannycode.AppointmentDTOs;

import com.dannycode.apointment.AppointmentStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppointmentResponse {
    private Long id;
    private Long branchId;
    private Long serviceId;
    private String scheduledAt;
    private AppointmentStatus status;

}
