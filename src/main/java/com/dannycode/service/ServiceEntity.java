// package com.dannycode.service;

// import com.dannycode.branch.Branch;

// import jakarta.persistence.*;
// import lombok.*;

// @Entity
// @Getter
// @Setter
// @AllArgsConstructor
// @NoArgsConstructor
// @Builder
// @Table(name = "services")
// public class ServiceEntity {

//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;

//     @Column(nullable = false, unique = true)
//     private String name;

//     private String description;

//     @Column(nullable = false)
//     private Integer defaultDurationMinutes;

//     @ManyToOne
//     @JoinColumn(name = "branch_id", nullable = false)
//     private Branch branch;

// }





package com.dannycode.service;

import com.dannycode.branch.Branch;
import com.dannycode.staff.StaffProfile;
import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "services")
public class ServiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String serviceTyp;

    private String description;

    @Column(nullable = false)
    private Integer defaultDurationMinutes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToMany(mappedBy = "servicesHandled", fetch = FetchType.LAZY)
    private Set<StaffProfile> staffProfiles;
}
