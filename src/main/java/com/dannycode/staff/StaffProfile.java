package com.dannycode.staff;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

import com.dannycode.branch.Branch;
import com.dannycode.user.User;
import com.dannycode.service.ServiceEntity;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "staff_profiles")
public class StaffProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "branch_id")
    private Branch branch;


    
    @ManyToMany
    @JoinTable(
            name = "staff_services",
            joinColumns = @JoinColumn(name = "staff_id"),
            inverseJoinColumns = @JoinColumn(name = "service_id")
    )
    private Set<ServiceEntity> servicesHandled;

    private Boolean isAvailable = true;

    private Integer avgServiceTimeMinutes;

}
