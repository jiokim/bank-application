package com.bank.common.core.context;

import com.bank.common.context.BankRequestContext;
import com.bank.common.context.BankRequestContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.AsyncHandlerInterceptor;

import java.time.LocalDate;

public class BankRequestContextInterceptor implements AsyncHandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        BankRequestContextHolder.set(new BankRequestContext(
                request.getHeader("X-Staff-Id"),
                request.getHeader("X-Channel-Code"),
                LocalDate.now()
        ));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        BankRequestContextHolder.clear();
    }

    @Override
    public void afterConcurrentHandlingStarted(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 비동기 MVC(Callable, DeferredResult)는 요청 스레드가 여기서 반환됨
        BankRequestContextHolder.clear();
    }
}
