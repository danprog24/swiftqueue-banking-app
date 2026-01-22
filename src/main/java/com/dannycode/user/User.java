// package com.dannycode.user;

// import java.time.LocalDateTime;

// import jakarta.persistence.*;
// import lombok.*;
// import java.util.Set;

// @Entity
// @Getter
// @Setter
// @AllArgsConstructor
// @NoArgsConstructor
// @Builder
// @Table(name = "users")
// public class User {

//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;

//     @Column(nullable = false)
//     private String fullname;

//     @Column(nullable = false, unique = true)
//     private String email;

//     @Column(nullable = false, unique = true)
//     private Integer phone;

//     @Column(nullable = false)
//     private String password;

//     @ManyToMany(fetch = FetchType.LAZY)
//     @JoinTable(
//             name = "user_roles",
//             joinColumns = @JoinColumn(name = "user_id"),
//             inverseJoinColumns = @JoinColumn(name = "role_id")
//     )

//     @ElementCollection(fetch = FetchType.EAGER)
//     @Enumerated(EnumType.STRING)
//     private Set<Role> roles;
    
//     private LocalDateTime createdAt;
//     private LocalDateTime updatedAt;

//     @PrePersist
//     protected void onCreate() {
//         createdAt = LocalDateTime.now();
//         updatedAt = createdAt;
//     }

//     @PreUpdate
//     protected void onUpdate() {
//         updatedAt = LocalDateTime.now();
//     }

//     public boolean locked() {
//         return false;
//     }

//     public boolean active() {
//       return true;
//     }
// }




package com.dannycode.user;

import com.dannycode.staff.StaffProfile;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullname;

    @Column(nullable = false, unique = true, length = 50)
    private String email;

    @Column(nullable = false, unique = true, length = 20)
    private String phone;

    @Column(nullable = false)
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Set<Role> roles;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean locked = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private StaffProfile staffProfile;
}
