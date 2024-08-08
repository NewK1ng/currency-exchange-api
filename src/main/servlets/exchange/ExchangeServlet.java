package servlets.exchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import models.response.ErrorRersponse;
import models.response.ExchangeResponse;
import repositories.CurrencyRepository;
import services.ExchangeService;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;

@WebServlet(name = "ExchangeServlet", value = "/exchange")
public class ExchangeServlet extends HttpServlet {

    private final CurrencyRepository currencyRepository = new CurrencyRepository();
    private final ObjectMapper mapper = new ObjectMapper();
    private final ExchangeService exchangeService = new ExchangeService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String baseCurrencyCode = request.getParameter("from");
        String targetCurrencyCode = request.getParameter("to");
        String amountParam = request.getParameter("amount");

        if (baseCurrencyCode == null || baseCurrencyCode.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(response.getWriter(), new ErrorRersponse("Base currency was not provided"));
            return;
        }

        if (targetCurrencyCode == null || targetCurrencyCode.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(response.getWriter(), new ErrorRersponse("Target currency was not provided"));
            return;
        }

        if (amountParam == null || amountParam.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(response.getWriter(), new ErrorRersponse("Amount of currency was not provided"));
            return;
        }

        BigDecimal amount;

        try {
            amount = BigDecimal.valueOf(Double.parseDouble(amountParam));
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(response.getWriter(), new ErrorRersponse("Wrong value of amount was provided"));
            return;
        }


        try {

            ExchangeResponse exchange = exchangeService.convertCurrency(baseCurrencyCode, targetCurrencyCode, amount);

            if(exchange == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                mapper.writeValue(response.getWriter(), new ErrorRersponse("Exchange rate for this currency pair was not found"));
                return;
            }

            mapper.writeValue(response.getWriter(), exchange);

        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            mapper.writeValue(response.getWriter(), new ErrorRersponse("Something went wrong with database"));
        }


    }

}