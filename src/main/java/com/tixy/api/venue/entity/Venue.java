package com.tixy.api.venue.entity;

import com.tixy.api.venue.enums.Location;
import com.tixy.api.venue.enums.VenueStatus;
import com.tixy.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Table(name = "venues")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Venue extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private VenueStatus venueStatus;

    @Enumerated(EnumType.STRING)
    private Location location;

    @Column(nullable = false)
    private Long totalSeatCount;


}


