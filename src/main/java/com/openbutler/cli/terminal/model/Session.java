package com.openbutler.cli.terminal.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.openbutler.core.memory.Message;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Session model representing a conversation session in the CLI system.
 * 
 * Validates: Requirements 6.1, 6.2, 6.3, 6.4, 6.10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Session {
    /**
     * 会话唯一标识符
     * Validates: Requirement 6.1
     */
    private String id;
    
    /**
     * 会话名称（用户可自定义）
     * Validates: Requirement 6.4
     */
    private String name;
    
    /**
     * 创建时间
     * Validates: Requirement 6.2
     */
    private LocalDateTime createdAt;
    
    /**
     * 最后活动时间
     * Validates: Requirement 6.3
     */
    private LocalDateTime lastActiveAt;
    
    /**
     * 消息数量
     */
    private int messageCount;
    
    /**
     * 是否是当前活动会话
     */
    private boolean active;
    
    /**
     * 会话元数据
     */
    @Builder.Default
    private Map<String, String> metadata = new HashMap<>();
    
    /**
     * 消息列表（懒加载，不序列化）
     * 使用 transient 关键字避免序列化
     */
    private transient List<Message> messages;
    
    /**
     * 获取消息列表（懒加载实现）
     * 
     * @return 消息列表
     */
    public List<Message> getMessages() {
        if (messages == null) {
            messages = new ArrayList<>();
        }
        return messages;
    }
}
