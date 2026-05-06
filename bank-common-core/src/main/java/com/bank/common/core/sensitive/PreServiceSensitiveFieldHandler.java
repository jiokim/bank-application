package com.bank.common.core.sensitive;

import com.bank.common.sensitive.SensitiveFieldProcessor;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.lang.reflect.Type;

@ControllerAdvice
public class PreServiceSensitiveFieldHandler extends RequestBodyAdviceAdapter {

    private final SensitiveFieldProcessor sensitiveFieldProcessor;

    public PreServiceSensitiveFieldHandler(SensitiveFieldProcessor sensitiveFieldProcessor) {
        this.sensitiveFieldProcessor = sensitiveFieldProcessor;
    }

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
                                Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return sensitiveFieldProcessor.processForStorage(body);
    }
}
