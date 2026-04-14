package com.tixy.core.util;

import com.github.f4b6a3.tsid.TsidCreator;
import org.springframework.stereotype.Component;

@Component
public class PublicIdGenerator {
    public static String generate(String prefix) {
        return prefix + "-" + TsidCreator.getTsid();
    }
}
