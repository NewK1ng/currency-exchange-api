package servlets.currency;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import models.Currency;
import models.response.ErrorRersponse;
import repositories.CurrencyRepository;

import java.io.IOException;
import java.sql.*;
import java.util.List;

@WebServlet(name = "CurrenciesServlet", value = "/currencies")
public class CurrenciesServlet extends HttpServlet {

    private final CurrencyRepository currencyRepository = new CurrencyRepository();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String UNIQUE_VIOLATION_CODE = "23505";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        try {

            List<Currency> currencyList = currencyRepository.getAll();
            mapper.writeValue(response.getWriter(), currencyList);

        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(response.getWriter(), new ErrorRersponse("Something went wrong with database"));
        }

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String name = request.getParameter("name");
        String code = request.getParameter("code");
        String sign = request.getParameter("sign");

        if (name == null || name.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(response.getWriter(), new ErrorRersponse("Name is required"));
            return;
        }

        if (code == null || code.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(response.getWriter(), new ErrorRersponse("Code is required"));
            return;
        }

        if (sign == null || sign.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(response.getWriter(), new ErrorRersponse("Sign is required"));
            return;
        }

        try {

            Currency currency = new Currency(name, code, sign);

            int savedId = currencyRepository.create(currency);
            currency.setId(savedId);

            response.setStatus(HttpServletResponse.SC_CREATED);
            mapper.writeValue(response.getWriter(), currency);

        } catch (SQLException e) {

            if (e.getSQLState().equals(UNIQUE_VIOLATION_CODE)) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                mapper.writeValue(response.getWriter(), new ErrorRersponse("Currency with this code already exists"));
                return;
            }

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(response.getWriter(), new ErrorRersponse("Something went wrong with database"));
        }

    }

}