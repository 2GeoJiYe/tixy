package com.tixy.api.event.entity;

import com.tixy.api.event.dto.request.UpdateEventRequest;
import com.tixy.api.event.enums.EventStatus;
import com.tixy.api.venue.entity.Venue;
import com.tixy.api.venue.enums.Category;
import com.tixy.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "events")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
@Builder
@SQLDelete(sql = "UPDATE events SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
public class Event extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id" , nullable = false)
    private Venue venue;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Category category;

    @Enumerated(EnumType.STRING)
    private EventStatus eventStatus;

    @Column(updatable = false)
    private LocalDateTime openDate;

    @Column(updatable = false)
    private LocalDateTime endDate;

    private LocalDateTime deletedAt;
    private boolean deleted;

    // update method 추가
    public void update(UpdateEventRequest request, Venue venue) {
        if (venue!=null) this.venue = venue;
        if (request.title() != null) this.title = request.title();
        if (request.description() != null) this.description = request.description();
        if (request.openDate() != null) this.openDate = request.openDate();
        if (request.endDate() != null) this.endDate = request.endDate();
    }

    public void updateStatus(EventStatus status){
        this.eventStatus = status;
    }
}
