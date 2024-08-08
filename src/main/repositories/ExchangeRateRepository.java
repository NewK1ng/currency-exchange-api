package repositories;

import models.Currency;
import models.ExchangeRate;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExchangeRateRepository {

    public List<ExchangeRate> getAll() throws SQLException {

        final String query = """ 
                        SELECT er.id AS id,
                       bc.id AS bc_id,
                       bc.fullname AS bc_fullname,
                       bc.code AS bc_code,
                       bc.sign AS bc_sign,
                       tc.id AS tc_id,
                       tc.fullname AS tc_fullname,
                       tc.code AS tc_code,
                       tc.sign AS tc_sign,
                       er.rate AS rate
                    FROM exchangerates AS er
                    JOIN currencies bc on er.basecurrencyid = bc.id
                    JOIN currencies tc on er.targercurrencyid = tc.id
                """;


        try (Connection con = DBConnector.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            List<ExchangeRate> exchangeRateList = new ArrayList<>();

            while (rs.next()) {
                exchangeRateList.add(getExchangeRate(rs));
            }

            return exchangeRateList;
        }
    }

    public ExchangeRate getByCodes(String baseCurrencyCode, String targetCurrencyCode) throws SQLException {

        final String query = """
                    SELECT er.id AS id,
                       bc.id AS bc_id,
                       bc.fullname AS bc_fullname,
                       bc.code AS bc_code,
                       bc.sign AS bc_sign,
                       tc.id AS tc_id,
                       tc.fullname AS tc_fullname,
                       tc.code AS tc_code,
                       tc.sign AS tc_sign,
                       er.rate AS rate
                    FROM exchangerates AS er
                    JOIN currencies bc on er.basecurrencyid = bc.id
                    JOIN currencies tc on er.targercurrencyid = tc.id
                    WHERE bc.code = ? AND tc.code = ?
                """;

        try (Connection con = DBConnector.getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setString(1, baseCurrencyCode);
            stmt.setString(2, targetCurrencyCode);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    return getExchangeRate(rs);
                } else {
                    return null;
                }

            }
        }

    }

    public int create(ExchangeRate exchangeRate) throws SQLException {

        final String query = """
                INSERT INTO exchangerates
                    (basecurrencyid, targercurrencyid, rate)
                VALUES (?,?,?)
                """;

        try (Connection con = DBConnector.getConnection();
             PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, exchangeRate.getBaseCurrency().getId());
            stmt.setInt(2, exchangeRate.getTargetCurrency().getId());
            stmt.setBigDecimal(3, exchangeRate.getRate());

            stmt.execute();

            try (ResultSet rs = stmt.getGeneratedKeys()) {

                rs.next();
                int createdId = rs.getInt("id");

                return createdId;
            }

        }

    }

    public int update(ExchangeRate exchangeRate) throws SQLException {

        final String query = """
                UPDATE exchangerates 
                SET rate = ? 
                WHERE basecurrencyid = ? AND targercurrencyid = ?;
                """;

        try (Connection con = DBConnector.getConnection();
             PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setBigDecimal(1, exchangeRate.getRate());
            stmt.setInt(2, exchangeRate.getBaseCurrency().getId());
            stmt.setInt(3, exchangeRate.getTargetCurrency().getId());

            stmt.execute();

            try (ResultSet rs = stmt.getGeneratedKeys()) {

                rs.next();
                int updatedId = rs.getInt("id");

                return updatedId;
            }

        }

    }

    private static ExchangeRate getExchangeRate(ResultSet rs) throws SQLException {

        return new ExchangeRate(rs.getInt("id"),
                new Currency(rs.getInt("bc_id"),
                        rs.getString("bc_fullname"),
                        rs.getString("bc_code"),
                        rs.getString("bc_sign")),
                new Currency(rs.getInt("tc_id"),
                        rs.getString("tc_fullname"),
                        rs.getString("tc_code"),
                        rs.getString("tc_sign")),
                rs.getBigDecimal("rate")
        );

    }


}
