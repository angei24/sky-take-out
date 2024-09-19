package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义切面类，实现公共字段自动填充
 */
@Slf4j
@Aspect
@Component
public class AutoFillAspect {

    //切入点
//    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
//    @Pointcut("@annotation(com.sky.annotation.AutoFill)")
//    public void autoFillPointCut() {}

    @Before("@annotation(com.sky.annotation.AutoFill)")
    public void autoFill(JoinPoint joinPoint) throws Exception {
        log.info("开始执行公共字段填充...");
        //获取拦截的方法的类型
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();//获取方法签名
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);//获取注解对象
        OperationType value = autoFill.value();//获取操作类型
        //获取拦截方法的实体对象
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0)
            return;
        Object entity = args[0];
        //准备数据
        LocalDateTime now = LocalDateTime.now();
        Long id = BaseContext.getCurrentId();
        //通过反射为对象赋值
        if (value == OperationType.INSERT) {
            Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
            Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
            Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
            //设置对应的值
            setCreateTime.invoke(entity, now);
            setUpdateTime.invoke(entity, now);
            setCreateUser.invoke(entity, id);
            setUpdateUser.invoke(entity, id);
        } else {
            Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
            //设置对应的值
            setUpdateTime.invoke(entity, now);
            setUpdateUser.invoke(entity, id);
        }
    }
}
