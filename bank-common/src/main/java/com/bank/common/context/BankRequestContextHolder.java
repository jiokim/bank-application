package com.bank.common.context;

public class BankRequestContextHolder {

    private static final ThreadLocal<BankRequestContext> CONTEXT = new ThreadLocal<>();

    public static void set(BankRequestContext ctx) {
        CONTEXT.set(ctx);
    }

    public static BankRequestContext current() {
        BankRequestContext ctx = CONTEXT.get();
        if (ctx == null) {
            throw new IllegalStateException("BankRequestContext is not bound to current thread");
        }
        return ctx;
    }

    public static void clear() {
        CONTEXT.remove();
    }

    private BankRequestContextHolder() {}
}
