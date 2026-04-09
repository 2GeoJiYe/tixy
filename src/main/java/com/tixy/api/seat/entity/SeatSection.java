package com.tixy.api.seat.entity;

import com.tixy.api.seat.enums.Grade;
import com.tixy.api.venue.entity.Venue;
import jakarta.persistence.*;
import lombok.*;

@Table
@Entity(name = "seat_sections")
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class SeatSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    private String sectionName;

    @Enumerated(EnumType.STRING)
    private Grade grade;
}
