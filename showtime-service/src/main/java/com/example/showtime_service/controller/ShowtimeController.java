package com.example.showtime_service.controller;

import com.example.showtime_service.exception.ResourceNotFoundException;
import com.example.showtime_service.model.Showtime;
import com.example.showtime_service.service.ShowtimeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/showtimes")
@Tag(name = "Showtimes", description = "Showtime scheduling APIs")
public class ShowtimeController {

    private final ShowtimeService showtimeService;

    public ShowtimeController(ShowtimeService showtimeService) {
        this.showtimeService = showtimeService;
    }

    @GetMapping
    @Operation(summary = "Get all showtimes")
    public List<Showtime> getAllShowtimes() {
        return showtimeService.getAllShowtimes();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get showtime by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Showtime found"),
            @ApiResponse(responseCode = "404", description = "Showtime not found")
    })
    public ResponseEntity<Showtime> getShowtimeById(
            @Parameter(description = "Showtime ID") @PathVariable Long id) {
        return showtimeService.getShowtimeById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Showtime not found with id: " + id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new showtime")
    @ApiResponse(responseCode = "201", description = "Showtime created successfully")
    public Showtime createShowtime(@Valid @RequestBody Showtime showtime) {
        return showtimeService.createShowtime(showtime);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing showtime")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Showtime updated"),
            @ApiResponse(responseCode = "404", description = "Showtime not found")
    })
    public ResponseEntity<Showtime> updateShowtime(
            @PathVariable Long id,
            @Valid @RequestBody Showtime showtimeDetails) {
        return ResponseEntity.ok(showtimeService.updateShowtime(id, showtimeDetails));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a showtime")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Showtime deleted"),
            @ApiResponse(responseCode = "404", description = "Showtime not found")
    })
    public void deleteShowtime(@PathVariable Long id) {
        showtimeService.deleteShowtime(id);
    }
}
