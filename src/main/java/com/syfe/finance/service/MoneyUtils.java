package com.syfe.finance.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

final class MoneyUtils {
    private MoneyUtils() {
    }

    static BigDecimal money(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : value.setScale(2, RoundingMode.HALF_UP);
    }
}
