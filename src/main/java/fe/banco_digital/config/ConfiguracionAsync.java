package fe.banco_digital.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class ConfiguracionAsync {

    @Bean(name = "executorAuditoria")
    public Executor executorAuditoria() {
        ThreadPoolTaskExecutor ejecutor = new ThreadPoolTaskExecutor();
        ejecutor.setCorePoolSize(2);
        ejecutor.setMaxPoolSize(5);
        ejecutor.setQueueCapacity(100);
        ejecutor.setThreadNamePrefix("auditoria-");
        ejecutor.initialize();
        return ejecutor;
    }
}
