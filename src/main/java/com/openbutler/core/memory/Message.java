package com.openbutler.core.memory;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    private Long id;
    private String sessionId;
    private String role;
    private String content;
    private LocalDateTime timestamp;
    private String metadata;
}
