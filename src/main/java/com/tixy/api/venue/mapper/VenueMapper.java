package com.tixy.api.venue.mapper;

import com.tixy.api.venue.dto.response.VenueResponse;
import com.tixy.api.venue.entity.Venue;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VenueMapper {

    VenueResponse toVenueResponse(Venue venue);
}
