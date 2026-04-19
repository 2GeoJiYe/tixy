package com.tixy.core.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class DateTimeUtils {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    public static LocalDateTime epochMilliToKst(long epochMilli) {
        return Instant.ofEpochMilli(epochMilli)
                .atZone(KST)
                .toLocalDateTime();
    }
}