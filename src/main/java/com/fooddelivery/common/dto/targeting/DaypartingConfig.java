package com.fooddelivery.common.dto.targeting;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.util.List;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DaypartingConfig implements Serializable {
    @Size(max = 400, message = "Maximum of 400 dayparts allowed")
    private List<Daypart> dayparts;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Daypart implements Serializable {
        private String dayOfWeek;
        private String startTime; // HH:mm
        private String endTime; // HH:mm
    }
}
