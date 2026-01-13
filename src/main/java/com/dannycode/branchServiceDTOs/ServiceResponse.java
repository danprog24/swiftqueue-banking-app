package com.dannycode.branchServiceDTOs;

import lombok.*;

@Getter
@Setter
public class ServiceResponse {
    private Long id;
    private String name;
    private String description;
   private Integer defaultDurationMinutes;
}
