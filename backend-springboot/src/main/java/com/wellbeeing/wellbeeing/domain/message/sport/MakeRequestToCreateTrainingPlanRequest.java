package com.wellbeeing.wellbeeing.domain.message.sport;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MakeRequestToCreateTrainingPlanRequest {
    private UUID trainerId;
    private String message;
    private LocalDateTime beginningDate;
}
