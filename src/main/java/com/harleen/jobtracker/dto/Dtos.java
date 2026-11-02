package com.harleen.jobtracker.dto;

import com.harleen.jobtracker.model.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public final class Dtos {
    private Dtos() {}

    public record Credentials(
            @NotBlank @Size(min = 3, max = 40) String username,
            @NotBlank @Size(min = 8, max = 100) String password) {}

    public record TokenResponse(String token) {}

    public record JobRequest(
            @NotBlank String company,
            @NotBlank String title,
            String location,
            Status status,
            LocalDate appliedDate,
            LocalDate followUpDate,
            @Size(max = 2000) String notes) {}
}
