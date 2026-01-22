// package com.dannycode.branch;
 
// import java.time.LocalTime;

// import jakarta.persistence.*;
// import lombok.*;



// @Entity
// @Getter
// @Setter
// @AllArgsConstructor
// @NoArgsConstructor
// @Builder
// @Table(name = "branches")
// public class Branch {

//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;

//     private String address;

//     private LocalTime openingTime;

//     private LocalTime closingTime;

//     private Boolean isOpen = true;

//     @ManyToMany(mappedBy = "branches", fetch = FetchType.LAZY)
//     private Set<StaffProfile> staffProfiles;

// }




// package com.dannycode.branch;

// import com.dannycode.staff.StaffProfile;
// import jakarta.persistence.*;
// import lombok.*;

// import java.time.LocalTime;
// import java.util.Set;

// @Entity
// @Getter
// @Setter
// @AllArgsConstructor
// @NoArgsConstructor
// @Builder
// @Table(name = "branches")
// public class Branch {

//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;

//     private String address;

//     private LocalTime openingTime;

//     private LocalTime closingTime;

//     private Boolean isOpen = true;

//     // Add inverse relationship
//     @OneToMany(mappedBy = "branch", fetch = FetchType.LAZY)
//     private Set<StaffProfile> staffProfiles;
// }




package com.dannycode.branch;

import com.dannycode.staff.StaffProfile;
import com.dannycode.service.ServiceEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "branches")
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String address;

    private LocalTime openingTime;
    private LocalTime closingTime;

    @Column(nullable = false)
    private boolean open;

    // Branch has many StaffProfiles
    @OneToMany(mappedBy = "branch", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<StaffProfile> staffProfiles;

    // Branch has many services
    @OneToMany(mappedBy = "branch", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<ServiceEntity> services;
}
