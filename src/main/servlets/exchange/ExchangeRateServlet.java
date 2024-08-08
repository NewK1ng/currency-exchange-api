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
import java.util.Enumeration;

@WebServlet(name = "ExchangeRateServlet", value = "/exchangeRate/*")
public class ExchangeRateServlet extends HttpServlet {

    private final ExchangeRateRepository exchangeRateRepository = new ExchangeRateRepository();
    private final CurrencyRepository currencyRepository = new CurrencyRepository();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String method = req.getMethod();

        if ("PATCH".equalsIgnoreCase(method)) {
            doPatch(req, resp);
        } else {
            super.service(req, resp);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


        String currencyCodes = request.getPathInfo().replaceAll("/", "");

        if (currencyCodes.length() != 6) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(response.getWriter(), new ErrorRersponse("Currency codes must be 6 characters"));
            return;
        }

        String baseCurrency = currencyCodes.substring(0, 3);
        String targetCurrency = currencyCodes.substring(3);

        try {

            ExchangeRate exchangeRate = exchangeRateRepository.getByCodes(baseCurrency, targetCurrency);

            if (exchangeRate == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                mapper.writeValue(response.getWriter(), new ErrorRersponse("There is no exchange rate for this currency pair"));
                return;
            }

            mapper.writeValue(response.getWriter(), exchangeRate);

        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(response.getWriter(), new ErrorRersponse("Something went wrong with database"));
        }

    }

    @Override
    protected void doPatch(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String currencyCodes = request.getPathInfo().replaceAll("/", "");

        String parameter = request.getReader().readLine();

        if (parameter == null || !parameter.contains("rate")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(response.getWriter(), new ErrorRersponse("Rate parameter was not provided"));
            return;
        }

        String rateParam = parameter.replace("rate=", "");

        if (currencyCodes.length() != 6) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(response.getWriter(), new ErrorRersponse("Currency codes must be 6 characters"));
            return;
        }

        String baseCurrencyCode = currencyCodes.substring(0, 3);
        String targetCurrencyCode = currencyCodes.substring(3);

        ExchangeRate exchangeRate = ExchangeRateUtil.createExchangeRate(response, currencyRepository, baseCurrencyCode, targetCurrencyCode, rateParam, mapper);

        if (exchangeRate == null) {
            return;
        }

        try {

            int id = exchangeRateRepository.update(exchangeRate);
            exchangeRate.setId(id);

            mapper.writeValue(response.getWriter(), exchangeRate);

        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            mapper.writeValue(response.getWriter(), new ErrorRersponse("Something went wrong with database"));
        }

    }

}