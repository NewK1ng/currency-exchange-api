package servlets.currency;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import models.Currency;
import models.response.ErrorRersponse;
import repositories.CurrencyRepository;

import java.io.IOException;
import java.sql.SQLException;

import static jakarta.servlet.http.HttpServletResponse.*;

@WebServlet(name = "CurrencyServlet", value = "/currency/*")
public class CurrencyServlet extends HttpServlet {

    CurrencyRepository currencyRepository = new CurrencyRepository();
    ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String code = request.getPathInfo().replaceAll("/", "");

        if (code.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(response.getWriter(), new ErrorRersponse("Provide some currency code"));
            return;
        }

        try {

            Currency currency = currencyRepository.getByCode(code);

            if (currency == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                mapper.writeValue(response.getWriter(), new ErrorRersponse("Currency was not found"));
                return;
            }

            mapper.writeValue(response.getWriter(), currency);

        } catch (SQLException e) {

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(response.getWriter(), new ErrorRersponse("Something went wrong with database"));

        }

    }

}