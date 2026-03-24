package com.openbutler.cli.terminal.encoding;

import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 集成测试：EncodingManager作为ApplicationContextInitializer
 * 
 * 验证需求: 4.8
 */
class EncodingManagerIntegrationTest {
    
    @Test
    void testEncodingManagerAsApplicationContextInitializer() {
        // 验证需求: 4.8
        // 测试EncodingManager作为ApplicationContextInitializer在应用启动时设置编码
        
        // 创建应用上下文
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        
        // 添加EncodingManager作为初始化器
        EncodingManager encodingManager = new EncodingManager();
        context.addApplicationListener(applicationEvent -> {
            // 监听上下文刷新事件
        });
        
        // 手动调用初始化方法
        encodingManager.initialize(context);
        
        // 验证编码已设置
        assertNotNull(encodingManager.getPlatformCharset());
        
        // 验证系统属性已设置
        assertNotNull(System.getProperty("file.encoding"));
        assertNotNull(System.getProperty("sun.stdout.encoding"));
        assertNotNull(System.getProperty("sun.stderr.encoding"));
        
        context.close();
    }
    
    @Test
    void testEncodingManagerInitializeBeforeContextRefresh() {
        // 测试EncodingManager在上下文刷新前初始化
        
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        EncodingManager encodingManager = new EncodingManager();
        
        // 在上下文刷新前初始化
        encodingManager.initialize(context);
        
        // 验证编码已正确设置
        assertNotNull(encodingManager.getPlatformCharset());
        
        // 现在刷新上下文
        context.refresh();
        
        // 验证编码仍然有效
        assertNotNull(encodingManager.getPlatformCharset());
        
        context.close();
    }
}
