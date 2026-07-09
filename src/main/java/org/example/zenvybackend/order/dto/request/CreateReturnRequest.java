package org.example.zenvybackend.order.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateReturnRequest {

    @NotNull
    private UUID orderItemId;

    private String reason;
}
