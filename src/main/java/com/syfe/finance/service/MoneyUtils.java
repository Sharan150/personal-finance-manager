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

    static BigDecimal formatPercentage(BigDecimal value) {
        if (value == null) return BigDecimal.ZERO.setScale(1, RoundingMode.UNNECESSARY);
        BigDecimal stripped = value.stripTrailingZeros();
        if (stripped.scale() < 1) {
            return stripped.setScale(1, RoundingMode.UNNECESSARY);
        }
        return stripped;
    }

    static BigDecimal moneyOrZero(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
