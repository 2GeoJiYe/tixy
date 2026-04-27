package com.tixy.core.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedisLock {
    String key(); // 저장할 키 이름 prefix
    int idx() default 0; // AOP에서 몇번째 인덱스에 있는 매개변수를 가져와서 키인덱스로 쓸건지
    long timeout() default 3L; // 초단위
}
