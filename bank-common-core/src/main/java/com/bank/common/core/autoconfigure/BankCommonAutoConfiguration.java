package com.bank.common.core.autoconfigure;

import com.bank.common.core.context.BankRequestContextInterceptor;
import com.bank.common.core.context.BankRequestContextTaskDecorator;
import com.bank.common.core.sensitive.PreServiceSensitiveFieldHandler;
import com.bank.common.sensitive.AesGcmTextEncryptor;
import com.bank.common.sensitive.SensitiveFieldProcessor;
import com.bank.common.sensitive.TextEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.Executor;

@AutoConfiguration
public class BankCommonAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    TextEncryptor textEncryptor(
            @Value("${bank.common.sensitive.encryption-key:MDEyMzQ1Njc4OWFiY2RlZg==}") String encryptionKey) {
        return new AesGcmTextEncryptor(encryptionKey);
    }

    @Bean
    @ConditionalOnMissingBean
    SensitiveFieldProcessor sensitiveFieldProcessor(TextEncryptor textEncryptor) {
        return new SensitiveFieldProcessor(textEncryptor);
    }

    @Bean
    @ConditionalOnMissingBean
    PreServiceSensitiveFieldHandler preServiceSensitiveFieldHandler(SensitiveFieldProcessor sensitiveFieldProcessor) {
        return new PreServiceSensitiveFieldHandler(sensitiveFieldProcessor);
    }

    @Bean
    @ConditionalOnMissingBean
    BankRequestContextInterceptor bankRequestContextInterceptor() {
        return new BankRequestContextInterceptor();
    }

    @Bean
    @ConditionalOnMissingBean(name = "bankAsyncExecutor")
    Executor bankAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setTaskDecorator(new BankRequestContextTaskDecorator());
        executor.initialize();
        return executor;
    }

    @Bean
    WebMvcConfigurer bankRequestContextMvcConfigurer(BankRequestContextInterceptor interceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(interceptor);
            }
        };
    }
}
