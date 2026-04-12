package com.tixy.api.venue.enums;

import lombok.Getter;

@Getter
public enum Location {
    GYEONGGI("경기도"),
    GANGWON("강원도"),
    CHUNGBUK("충청북도"),
    CHUNGNAM("충청남도"),
    JEONBUK("전라북도"),
    JEONNAM("전라남도"),
    GYEONGBUK("경상북도"),
    GYEONGNAM("경상남도"),
    JEJU("제주도"),
    SEOUL("서울"),
    BUSAN("부산");

    private final String name;

    Location(String name) {
        this.name = name;
    }
}
