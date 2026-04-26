package com.tixy.core.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExchangeRateUtils {

    private final RestTemplate restTemplate = new RestTemplate();

    public BigDecimal convertKrwToUsdt(Long krwAmount) {
        String url = "https://api.coingecko.com/api/v3/simple/price?ids=tether&vs_currencies=krw";

        Map<String, Map<String, Double>> response = restTemplate.getForObject(url, Map.class);
        BigDecimal usdtInKrw = BigDecimal.valueOf(response.get("tether").get("krw"));

        return BigDecimal.valueOf(krwAmount).divide(usdtInKrw, 6, RoundingMode.HALF_UP);
    }
}