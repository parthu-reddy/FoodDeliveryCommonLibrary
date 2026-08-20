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
public class ContextualKeywords implements Serializable {
    @Size(max = 400, message = "Maximum of 400 keywords allowed")
    private List<String> keywords;
}
