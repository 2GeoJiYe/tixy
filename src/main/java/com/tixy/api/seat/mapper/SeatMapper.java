package com.tixy.api.seat.mapper;

import com.tixy.api.seat.dto.response.SeatSectionResponse;
import com.tixy.api.seat.entity.SeatSection;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SeatMapper {

    SeatSectionResponse toSeatSectionResponse(SeatSection seatSection);
}
