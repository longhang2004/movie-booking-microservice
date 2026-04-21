package com.example.theater_service.controller;

import com.example.theater_service.exception.ResourceNotFoundException;
import com.example.theater_service.model.Theater;
import com.example.theater_service.service.TheaterService;
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
@RequestMapping("/theaters")
@Tag(name = "Theaters", description = "Theater and room management APIs")
public class TheaterController {

    private final TheaterService theaterService;

    public TheaterController(TheaterService theaterService) {
        this.theaterService = theaterService;
    }

    @GetMapping
    @Operation(summary = "Get all theaters")
    public List<Theater> getAllTheaters() {
        return theaterService.getAllTheaters();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get theater by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Theater found"),
            @ApiResponse(responseCode = "404", description = "Theater not found")
    })
    public ResponseEntity<Theater> getTheaterById(
            @Parameter(description = "Theater ID") @PathVariable Long id) {
        return theaterService.getTheaterById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Theater not found with id: " + id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new theater")
    @ApiResponse(responseCode = "201", description = "Theater created successfully")
    public Theater createTheater(@Valid @RequestBody Theater theater) {
        return theaterService.createTheater(theater);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing theater")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Theater updated"),
            @ApiResponse(responseCode = "404", description = "Theater not found")
    })
    public ResponseEntity<Theater> updateTheater(
            @PathVariable Long id,
            @Valid @RequestBody Theater theaterDetails) {
        return ResponseEntity.ok(theaterService.updateTheater(id, theaterDetails));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a theater")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Theater deleted"),
            @ApiResponse(responseCode = "404", description = "Theater not found")
    })
    public void deleteTheater(@PathVariable Long id) {
        theaterService.deleteTheater(id);
    }
}
