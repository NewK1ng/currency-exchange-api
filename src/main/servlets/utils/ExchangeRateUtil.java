package servlets.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import models.Currency;
import models.ExchangeRate;
import models.response.ErrorRersponse;
import repositories.CurrencyRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;

public class ExchangeRateUtil {

    public static ExchangeRate createExchangeRate(HttpServletResponse response, CurrencyRepository currencyRepository,
                                                  String baseCurrencyCode, String targetCurrencyCode, String rateParam, ObjectMapper mapper) throws IOException {

        Currency baseCurrency;
        Currency targetCurrency;
        BigDecimal rate;

        try {
            rate = BigDecimal.valueOf(Double.parseDouble(rateParam));
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            mapper.writeValue(response.getWriter(), new ErrorRersponse("Wrong value for rate parameter"));
            return null;
        }

        try {
            baseCurrency = currencyRepository.getByCode(baseCurrencyCode);

            if (baseCurrency == null) {
                throw new SQLException();
            }
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            mapper.writeValue(response.getWriter(), new ErrorRersponse("Base currency code was not found in the database"));
            return null;
        }

        try {
            targetCurrency = currencyRepository.getByCode(targetCurrencyCode);

            if (targetCurrency == null) {
                throw new SQLException();
            }
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            mapper.writeValue(response.getWriter(), new ErrorRersponse("Target currency code was not found in the database"));
            return null;
        }

        return new ExchangeRate(baseCurrency, targetCurrency, rate);

    }

}
