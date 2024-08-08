package servlets.exchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import models.ExchangeRate;
import models.response.ErrorRersponse;
import repositories.CurrencyRepository;
import repositories.ExchangeRateRepository;
import servlets.utils.ExchangeRateUtil;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name = "ExchangeRatesServlet", value = "/exchangeRates")
public class ExchangeRatesServlet extends HttpServlet {

    private final ObjectMapper mapper = new ObjectMapper();
    private final ExchangeRateRepository exchangeRateRepository = new ExchangeRateRepository();
    private final CurrencyRepository currencyRepository = new CurrencyRepository();
    private final String UNIQUE_VIOLATION_CODE = "23505";


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        try {

            List<ExchangeRate> exchangeRateList = exchangeRateRepository.getAll();

            mapper.writeValue(response.getWriter(), exchangeRateList);

        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(response.getWriter(), new ErrorRersponse("Something went wrong with database"));
        }

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String baseCurrencyCode = request.getParameter("baseCurrencyCode");
        String targetCurrencyCode = request.getParameter("targetCurrencyCode");
        String rateParam = request.getParameter("rate");

        if (baseCurrencyCode == null || baseCurrencyCode.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(response.getWriter(), new ErrorRersponse("Base currency code was not provided"));
            return;
        }

        if (targetCurrencyCode == null || targetCurrencyCode.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(response.getWriter(), new ErrorRersponse("Target currency code was not provided"));
            return;
        }

        if (rateParam == null || rateParam.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(response.getWriter(), new ErrorRersponse("Rate parameter was not provided"));
            return;
        }

        ExchangeRate exchangeRate = ExchangeRateUtil.createExchangeRate(response, currencyRepository, baseCurrencyCode, targetCurrencyCode, rateParam, mapper);

        if (exchangeRate == null) {
            return;
        }

        try {
            int exchangeRateId = exchangeRateRepository.create(exchangeRate);
            exchangeRate.setId(exchangeRateId);

            response.setStatus(HttpServletResponse.SC_CREATED);
            mapper.writeValue(response.getWriter(), exchangeRate);

        } catch (SQLException e) {

            if (e.getSQLState().equals(UNIQUE_VIOLATION_CODE)) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                mapper.writeValue(response.getWriter(), new ErrorRersponse("This currency pair already exists"));
                return;
            }

            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            mapper.writeValue(response.getWriter(), new ErrorRersponse("Something went wrong with database"));
        }

    }


}