package services;

import models.Currency;
import models.response.ExchangeResponse;
import models.ExchangeRate;
import repositories.ExchangeRateRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;

public class ExchangeService {

    private final ExchangeRateRepository exchangeRateRepository = new ExchangeRateRepository();

    public ExchangeResponse convertCurrency(String baseCurrencyCode, String targetCurrencyCode, BigDecimal amount) throws SQLException {

        ExchangeResponse exchange;


        exchange = getFromDirectExchangeRate(baseCurrencyCode, targetCurrencyCode, amount);

        if (exchange == null) {
            exchange = getFromReverseExchangeRate(targetCurrencyCode, baseCurrencyCode, amount);
        }

        if (exchange == null) {
            exchange = getFromCrossExchangeRate(baseCurrencyCode, targetCurrencyCode, amount);
        }

        return exchange;

    }

    private ExchangeResponse getFromDirectExchangeRate(String baseCurrencyCode, String targetCurrencyCode, BigDecimal amount) throws SQLException {

        ExchangeRate exchangeRate = exchangeRateRepository.getByCodes(baseCurrencyCode, targetCurrencyCode);

        if (exchangeRate == null) {
            return null;
        }

        BigDecimal convertedAmount = exchangeRate.getRate().multiply(amount).setScale(2, RoundingMode.HALF_UP);

        return getExchange(exchangeRate.getBaseCurrency(), exchangeRate.getTargetCurrency(), exchangeRate.getRate(), amount, convertedAmount);
    }

    private ExchangeResponse getFromReverseExchangeRate(String baseCurrencyCode, String targetCurrencyCode, BigDecimal amount) throws SQLException {

        ExchangeRate exchangeRate = exchangeRateRepository.getByCodes(baseCurrencyCode, targetCurrencyCode);

        if (exchangeRate == null) {
            return null;
        }

        BigDecimal convertedAmount = BigDecimal.valueOf(1).
                divide(exchangeRate.getRate(), 10, RoundingMode.HALF_UP).
                multiply(amount).
                setScale(2, RoundingMode.HALF_UP);

        return getExchange(exchangeRate.getBaseCurrency(), exchangeRate.getTargetCurrency(), exchangeRate.getRate(), amount, convertedAmount);
    }

    private ExchangeResponse getFromCrossExchangeRate(String baseCurrencyCode, String targetCurrencyCode, BigDecimal amount) throws SQLException {

        ExchangeRate baseUsdExchangeRate = exchangeRateRepository.getByCodes("USD", baseCurrencyCode);
        ExchangeRate targetUsdExchangeRate = exchangeRateRepository.getByCodes("USD", targetCurrencyCode);

        if (baseUsdExchangeRate == null || targetUsdExchangeRate == null) {
            return null;
        }

        BigDecimal baseToTargetCurrencyRate = targetUsdExchangeRate.getRate().
                divide(baseUsdExchangeRate.getRate(), 10, RoundingMode.HALF_UP).
                setScale(2, RoundingMode.HALF_UP);

        System.out.println(baseToTargetCurrencyRate);

        BigDecimal convertedAmount = baseToTargetCurrencyRate.multiply(amount).setScale(2, RoundingMode.HALF_UP);

        return getExchange(baseUsdExchangeRate.getTargetCurrency(), targetUsdExchangeRate.getTargetCurrency(), baseToTargetCurrencyRate, amount, convertedAmount);
    }


    private static ExchangeResponse getExchange(Currency baseCurrency, Currency targetCurrency, BigDecimal rate, BigDecimal amount, BigDecimal convertedAmount) {

        return new ExchangeResponse(
                baseCurrency,
                targetCurrency,
                rate,
                amount,
                convertedAmount
        );
    }


}
