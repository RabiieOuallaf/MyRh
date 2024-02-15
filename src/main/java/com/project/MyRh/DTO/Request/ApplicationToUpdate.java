package com.project.MyRh.DTO.Request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationToUpdate {
    private int applicant_id;
    private int jobOffer_id;
    private String status;
}
