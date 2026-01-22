package com.dannycode.staff;

import com.dannycode.branch.Branch;
import com.dannycode.service.ServiceEntity;
import com.dannycode.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "staff_profiles")
public class StaffProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "staff_services",
        joinColumns = @JoinColumn(name = "staff_profile_id"),
        inverseJoinColumns = @JoinColumn(name = "service_id")
    )
    private Set<ServiceEntity> servicesHandled;

    @Column(nullable = false)
    private boolean available;

    private Integer avgServiceTimeMinutes;
}
