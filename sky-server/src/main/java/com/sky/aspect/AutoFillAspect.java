package com.sky.aspect;


import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义切面实现自动填充
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {

    // 切面就是通知加上切入点
    /**
     *  切入点  对哪些类的哪些方法进行拦截
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut(){}

    /**
     * 前置通知
     */

    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint jointPoint){
        log.info("开始进行工作字段填充");

        // 获取到当前被拦截的方法上的数据库操作类型
        MethodSignature signature = (MethodSignature)jointPoint.getSignature();// 方法签名对象
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);  // 获得方法上的注解对象
        OperationType operationType = autoFill.value();

        // 获取到当前被拦截的方法参数--实体对象
        Object[] args = jointPoint.getArgs();
        if(args == null || args.length == 0){
            return;
        }
        Object entity = args[0];
        // 准备赋值数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();


        // 根据当前不同的操作类型，为对应的属性通过反射赋值

        if(operationType == OperationType.INSERT){
            // 为四个公共字段赋值

            try {
                Method setCreatTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreatUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                // 通过反射来为对象属性赋值
                setCreatTime.invoke(entity, now);
                setCreatUser.invoke(entity, currentId);
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }


        }else{
            // 为两个公共字段赋值
            try {

                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                // 通过反射来为对象属性赋值

                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
    }

}

