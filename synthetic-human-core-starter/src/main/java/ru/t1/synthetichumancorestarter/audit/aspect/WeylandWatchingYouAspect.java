package ru.t1.synthetichumancorestarter.audit.aspect;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import ru.t1.synthetichumancorestarter.audit.model.AuditEvent;
import ru.t1.synthetichumancorestarter.audit.service.AuditService;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
@RequiredArgsConstructor
public class WeylandWatchingYouAspect {

    private final AuditService auditService;

    @Around("@annotation(ru.t1.synthetichumancorestarter.audit.aspect.WeylandWatchingYou)")
    public Object proceedMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        final var auditEventBuilder = AuditEvent.builder();
        auditEventBuilder.time(DateTimeFormatter.ISO_INSTANT.format(Instant.now()));
        final var method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        auditEventBuilder.method(method.getName());
        auditEventBuilder.params(extractMethodParameters(method, joinPoint.getArgs()));
        try {
            final var result = joinPoint.proceed();
            auditEventBuilder.state(AuditEvent.MethodExecutionState.SUCCESS);
            return result;
        } catch (Throwable e) {
            auditEventBuilder.state(AuditEvent.MethodExecutionState.EXCEPTION);
            throw e;
        } finally {
            auditService.audit(auditEventBuilder.build());
        }
    }

    public Map<String, AuditEvent.Param> extractMethodParameters(Method method, Object[] args) {
        final var params = new HashMap<String, AuditEvent.Param>();
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            params.put(param.getName(), new AuditEvent.Param(param.getType().getSimpleName(), args[i]));
        }
        return params;
    }
}
