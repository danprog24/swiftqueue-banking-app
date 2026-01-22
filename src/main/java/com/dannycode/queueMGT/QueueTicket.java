// package com.dannycode.queueMGT;

// import java.time.LocalDateTime;

// import com.dannycode.branch.Branch;
// import com.dannycode.staff.StaffProfile;
// import com.dannycode.user.User;

// import jakarta.persistence.*;
// import lombok.*;

// @Entity
// @Getter
// @Setter
// @AllArgsConstructor
// @NoArgsConstructor
// @Builder
// @Table(name = "queue_tickets")
// public class QueueTicket {

//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;

//     @Column(nullable = false)
//     private String ticketNumber;

//     private User user;

//     private Branch branch;

//     private StaffProfile assignedStaff;

//     @Enumerated(EnumType.STRING)
//     private QueueStatus status = QueueStatus.WAITING;

//     @Enumerated(EnumType.STRING)
//     private PriorityLevel priorityLevel = PriorityLevel.NORMAL;

//     private Integer position;

//     private Integer estimatedWaitMinutes;

//     private LocalDateTime createdAt ;

//     private LocalDateTime calledAt ;

//     private LocalDateTime completedAt ;

//     @PrePersist
//     protected void onCreate() {
//         createdAt = LocalDateTime.now();
//     }
// }





package com.dannycode.queueMGT;

import java.time.LocalDateTime;

import com.dannycode.branch.Branch;
import com.dannycode.staff.StaffProfile;
import com.dannycode.user.User;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "queue_tickets")
public class QueueTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String ticketNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_staff_id")
    private StaffProfile assignedStaff;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private QueueStatus status = QueueStatus.WAITING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PriorityLevel priorityLevel = PriorityLevel.NORMAL;

    private Integer position;

    private Integer estimatedWaitMinutes;

    private LocalDateTime createdAt;
    private LocalDateTime calledAt;
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
