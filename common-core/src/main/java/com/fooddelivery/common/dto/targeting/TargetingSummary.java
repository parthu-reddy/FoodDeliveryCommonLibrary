package com.fooddelivery.common.dto.targeting;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TargetingSummary implements Serializable {
    private GeoTargeting geoTargeting;
    private DaypartingConfig daypartingConfig;
    private ContextualKeywords contextualKeywords;
    private List<String> brandSafetyBlocklist;
    private Integer frequencyCap;
}
