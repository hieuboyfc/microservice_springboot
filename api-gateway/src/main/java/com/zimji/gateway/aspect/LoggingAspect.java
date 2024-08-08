package com.zimji.gateway.aspect;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LoggingAspect {

    static Logger LOGGER = LoggerFactory.getLogger(LoggingAspect.class);

    @Before("execution(* com.zimji.gateway.configuration..*(..)) || " +
            "execution(* com.example.gateway.service..*(..)) || " +
            "execution(* com.example.gateway.client..*(..))")
    public void logBefore(JoinPoint joinPoint) {
        LOGGER.warn("---> Log Before ---> Entering method: {}", joinPoint.getSignature().toShortString());
    }

    @After("execution(* com.example.gateway.configuration..*(..)) || " +
            "execution(* com.example.gateway.service..*(..)) || " +
            "execution(* com.example.gateway.client..*(..))")
    public void logAfter(JoinPoint joinPoint) {
        LOGGER.warn("---> Log After ---> Exiting method: {}", joinPoint.getSignature().toShortString());
    }

    @AfterReturning(pointcut = "execution(* com.example.gateway.configuration..*(..)) || " +
            "execution(* com.example.gateway.service..*(..)) || " +
            "execution(* com.example.gateway.client..*(..))", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        LOGGER.warn("---> Log After Returning ---> After executing method: {}, Result: {}",
                joinPoint.getSignature().toShortString(), result);
    }

    @AfterThrowing(pointcut = "execution(* com.example.gateway.configuration..*(..)) || " +
            "execution(* com.example.gateway.service..*(..)) || " +
            "execution(* com.example.gateway.client..*(..))", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
        LOGGER.warn("---> Log After Throwing ---> Exception in method: {}, Exception: {}",
                joinPoint.getSignature().toShortString(), e.getMessage());
    }

    @Around("execution(* com.example.gateway.configuration..*(..)) || " +
            "execution(* com.example.gateway.service..*(..)) || " +
            "execution(* com.example.gateway.client..*(..))")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        LOGGER.warn("---> Log Around ---> Before method: {}", joinPoint.getSignature().toShortString());
        Object result = joinPoint.proceed(); // Continue the method execution
        LOGGER.warn("---> Log Around ---> After method: {}", joinPoint.getSignature().toShortString() + ", Result: " + result);
        return result;
    }

}
