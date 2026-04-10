package com.tixy.api.ticket.controller;

import com.tixy.api.ticket.dto.request.CreateTicketTypeRequest;
import com.tixy.api.ticket.dto.response.CreateTicketTypeResponse;
import com.tixy.api.ticket.service.TicketTypeService;
import com.tixy.core.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ticket-types")
@RequiredArgsConstructor
public class TicketTypeController {

    private final TicketTypeService ticketTypeService;

    @PostMapping("/v1")
    public ResponseEntity<ApiResponse<CreateTicketTypeResponse>> addTicketType(@RequestBody CreateTicketTypeRequest createTicketTypeRequest){

        return ResponseEntity.ok().body(ApiResponse.success(ticketTypeService.saveTicketType(createTicketTypeRequest)));
    }
}
