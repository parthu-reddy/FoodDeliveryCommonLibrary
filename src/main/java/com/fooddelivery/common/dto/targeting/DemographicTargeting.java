package com.fooddelivery.common.dto.targeting;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemographicTargeting implements Serializable {
    private List<String> ageGroups;
    private List<String> genders;
    private List<String> incomeBrackets;
}
