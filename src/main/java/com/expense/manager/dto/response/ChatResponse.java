package com.expense.manager.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatResponse {
    private String message;
    private String response;
    private String intent;
    private long timestamp;
}
