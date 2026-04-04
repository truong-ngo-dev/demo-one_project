package vn.truongngo.apartcom.one.lib.common.domain.context;

public class RequestContextHolder {

    private static final ThreadLocal<RequestContext> CONTEXT = new ThreadLocal<>();

    private RequestContextHolder() {}

    public static void init() {
        CONTEXT.set(RequestContext.create());
    }

    public static RequestContext current() {
        RequestContext ctx = CONTEXT.get();
        if (ctx == null) {
            throw new IllegalStateException("RequestContext is not initialized");
        }
        return ctx;
    }

    public static void clear() {
        RequestContext ctx = CONTEXT.get();
        if (ctx != null) ctx.clear();
        CONTEXT.remove();
    }
}