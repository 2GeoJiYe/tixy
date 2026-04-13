package com.tixy.api.seat.entity;

import com.tixy.api.seat.enums.SeatStatus;
import com.tixy.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seats",
        uniqueConstraints = @UniqueConstraint(
                // 같은 섹션에 같은 row_label 이 못들어가게 하는 코드
                // 테스트 결과 같은 섹션에 동일한 값 입력 시 DuplicateKeyException 에러 발생
                columnNames = {"seat_section_id", "row_label"}
        )
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Seat extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_section_id", nullable = false)
    private SeatSection seatSection;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus seatStatus;

    private String rowLabel;
}
