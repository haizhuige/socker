package com.liuhu.socket.common.config;

import com.liuhu.socket.enums.ResponseEnum;
import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author liuhu
 * @date 2019-08-15
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@RequestMapping
public @interface RequestHandler {

    @AliasFor(annotation = RequestMapping.class, value = "value")
    String value();

    String descriptor();

    ResponseEnum responseEnum() default ResponseEnum.EXCEPTION;

}
