package com.dannycode.branch;
 
import java.time.LocalTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "branches")
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String address;

    private LocalTime openingTime;

    private LocalTime closingTime;

    private Boolean isOpen = true;

}
