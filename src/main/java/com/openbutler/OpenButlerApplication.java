package com.openbutler;

import com.openbutler.cli.terminal.encoding.EncodingManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * OpenButlerApplication 是 Spring Boot 应用的主类。
 *
 * 本类功能：
 * - 注册 EncodingManager 作为 ApplicationContextInitializer 以便提前设置编码
 * - 配置异步执行器用于响应式处理
 * - 启动 Spring 应用上下文
 *
 * 验证要求：4.8, 10.3
 */
@SpringBootApplication
@EnableAsync
public class OpenButlerApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(OpenButlerApplication.class);
        
        // 注册 EncodingManager 作为 ApplicationContextInitializer
        // 这确保编码在任何其他 bean 初始化之前就已设置
        app.addInitializers(new EncodingManager());
        
        app.run(args);
    }
    
    /**
     * 配置异步执行器用于响应式处理。
     * 用于流式 AI 响应等非阻塞操作。
     *
     * 验证要求：10.3
     *
     * @return 配置好的线程池任务执行器
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}
