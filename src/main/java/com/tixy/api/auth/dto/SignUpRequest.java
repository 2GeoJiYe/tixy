package com.tixy.api.auth.dto;

import com.tixy.core.annotation.Masking;
import com.tixy.core.annotation.MaskingType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

/**
 * 회원가입 요청 데이터를 담는 DTO입니다.
 *
 * [Record로 변환 이유]
 * DTO는 데이터 전송이 주 목적이며, 불변성을 유지하는 것이 안전합니다.
 * Record를 사용하면 final 필드와 생성자, 접근자(getter) 등을 자동으로 생성해주어 코드가 간결해집니다.
 * 또한 Jackson 라이브러리는 Record의 생성자를 통해 JSON 데이터를 객체로 매핑하는 것을 공식 지원합니다.
 */
@Builder
public record SignUpRequest(
        @NotBlank(message = "이메일은 필수입니다.")
        @Masking(type = MaskingType.EMAIL)
        String email,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
                message = "비밀번호는 8자 이상, 영문, 숫자, 특수문자를 포함해야 합니다.")
        @Masking
        String password,

        @NotBlank(message = "이름은 필수입니다.")
        @Masking(type = MaskingType.NAME)
        String name
) {
}