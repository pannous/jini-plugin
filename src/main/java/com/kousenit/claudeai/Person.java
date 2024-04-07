package com.kousenit.claudeai;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.time.LocalDate;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record Person(String firstName,
                     String lastName,
                     String origin,
                     LocalDate dob) {
}
