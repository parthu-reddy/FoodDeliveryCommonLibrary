package com.fooddelivery.common.dto.ledger;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BeneficiaryResponse {
    private String accountNumberMasked;
    private String ifsc;
    private String beneficiaryName;
    private boolean verified;
    private String source;
}
