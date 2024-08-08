package repositories;

import models.Currency;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CurrencyRepository {

    public List<Currency> getAll() throws SQLException {

        final String query = "SELECT * FROM currencies";

        try (Connection con = DBConnector.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            List<Currency> currencyList = new ArrayList<>();

            while (rs.next()) {

                currencyList.add(getCurrency(rs));
            }

            return currencyList;
        }

    }

    public Currency getByCode(String code) throws SQLException {

        final String query = "SELECT * FROM currencies WHERE code = ?";

        try (Connection con = DBConnector.getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setString(1, code);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    return getCurrency(rs);
                } else {
                    return null;
                }
            }

        }

    }

    public int create(Currency currency) throws SQLException {

        final String query = "INSERT INTO currencies (code, fullname, sign) VALUES (?, ?, ?)";

        try (Connection con = DBConnector.getConnection();
             PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, currency.getCode());
            stmt.setString(2, currency.getName());
            stmt.setString(3, currency.getSign());

            stmt.execute();

            try (ResultSet savedCurrency = stmt.getGeneratedKeys()) {

                savedCurrency.next();
                int id = savedCurrency.getInt("id");

                return id;
            }

        }
    }

    private static Currency getCurrency(ResultSet rs) throws SQLException {

        return new Currency(
                rs.getInt("id"),
                rs.getString("fullname"),
                rs.getString("code"),
                rs.getString("sign")
        );
    }

}
