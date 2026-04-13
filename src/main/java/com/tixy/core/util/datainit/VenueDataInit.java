package com.tixy.core.util.datainit;

import com.tixy.api.venue.entity.Venue;
import com.tixy.api.venue.enums.Location;
import com.tixy.api.venue.enums.VenueStatus;
import com.tixy.api.venue.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class VenueDataInit {

    private final VenueRepository venueRepository;

    private static final List<VenueSeed> VENUE_SEEDS = List.of(
            new VenueSeed("올림픽공원 체조경기장", Location.SEOUL, 300L),
            new VenueSeed("KSPO DOME", Location.SEOUL, 500L),
            new VenueSeed("잠실종합운동장 주경기장", Location.SEOUL, 500L),
            new VenueSeed("고척스카이돔", Location.SEOUL, 500L),
            new VenueSeed("인스파이어 아레나", Location.GYEONGGI, 400L),
            new VenueSeed("수원월드컵경기장", Location.GYEONGGI, 500L),
            new VenueSeed("성남아트센터", Location.GYEONGGI, 100L),
            new VenueSeed("부산아시아드주경기장", Location.BUSAN, 500L),
            new VenueSeed("부산 벡스코", Location.BUSAN, 200L),
            new VenueSeed("사직야구장", Location.BUSAN, 500L),
            new VenueSeed("강릉아이스아레나", Location.GANGWON,100L),
            new VenueSeed("원주치악체육관", Location.GANGWON, 100L),
            new VenueSeed("청주야구장",  Location.CHUNGBUK, 400L),
            new VenueSeed("충주체육관", Location.CHUNGBUK, 300L),
            new VenueSeed("천안유관순체육관", Location.CHUNGNAM, 200L),
            new VenueSeed("아산이순신체육관", Location.CHUNGNAM,200L ),
            new VenueSeed("전주월드컵경기장", Location.JEONBUK, 500L),
            new VenueSeed("익산실내체육관", Location.JEONBUK,300L),
            new VenueSeed("광주월드컵경기장", Location.JEONNAM,400L),
            new VenueSeed("여수엑스포컨벤션센터", Location.JEONNAM, 200L),
            new VenueSeed("포항스틸야드", Location.GYEONGBUK, 200L),
            new VenueSeed("안동체육관", Location.GYEONGBUK, 100L),
            new VenueSeed("창원종합운동장", Location.GYEONGNAM, 100L),
            new VenueSeed("진주실내체육관", Location.GYEONGNAM, 200L),
            new VenueSeed("제주월드컵경기장", Location.JEJU, 500L),
            new VenueSeed("서귀포칼호텔 야외공연장", Location.JEJU, 200L),
            new VenueSeed("청평리조트 야외무대", Location.GANGWON, 100L),
            new VenueSeed("구 장충체육관", Location.SEOUL, 200L),
            new VenueSeed("대전구장", Location.CHUNGNAM, 400L),
            new VenueSeed("동대구역 광장무대", Location.GYEONGBUK, 100L)
    );

    @Transactional
    public void initVenues() {
        if (venueRepository.count() > 0) return;

        for (VenueSeed seed : VENUE_SEEDS){
            Venue venue = Venue.builder()
                    .name(seed.name)
                    .venueStatus(VenueStatus.ACTIVE)
                    .totalSeatCount(seed.totalSeatCount)
                    .location(seed.location)
                    .build();

            venueRepository.save(venue);
        }

        System.out.printf("테스트용 Venue %d개 세팅 완료!\n",VENUE_SEEDS.size());
    }


    private record VenueSeed(
            String name, Location location, Long totalSeatCount
    ) {}
}
