package com.tixy.api.venue.enums;

import com.tixy.core.exception.event.EventErrorCode;
import com.tixy.core.exception.event.EventServiceException;
import lombok.Getter;

@Getter
public enum Category {
    MUSICAL("뮤지컬"),
    CONCERT("콘서트"),
    PLAY("연극"),
    EXHIBITION("전시"),
    SPORT("스포츠")
    ;

    private final String name;

    Category(String name) {
        this.name = name;
    }

    public static Category from(String category){
        for (Category c : Category.values()){
            if (category.equals(c.name)) return c;
            if (category.equals(c.toString())) return c;
        }
        throw new EventServiceException(EventErrorCode.INVALID_EVENT_CATEGORY);
    }
}
