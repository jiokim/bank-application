package com.bank.common.core.context;

import com.bank.common.context.BankRequestContext;
import com.bank.common.context.BankRequestContextHolder;
import org.springframework.core.task.TaskDecorator;

public class BankRequestContextTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        BankRequestContext ctx = BankRequestContextHolder.current();
        return () -> {
            BankRequestContextHolder.set(ctx);
            try {
                runnable.run();
            } finally {
                BankRequestContextHolder.clear();
            }
        };
    }
}
