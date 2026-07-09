package org.example.zenvybackend.order.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.zenvybackend.order.enums.ReturnStatus;

@Data
public class UpdateReturnStatusRequest {

    @NotNull
    private ReturnStatus status;
}
