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
public class GeoTargeting implements Serializable {
    @Size(max = 400, message = "Maximum of 400 regions allowed")
    @jakarta.validation.constraints.NotNull
    private List<String> regions;
}
