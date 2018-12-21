package org.example.refactoring;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static org.apache.commons.lang3.StringUtils.substringBetween;
import static org.assertj.core.api.Assertions.*;
import static org.example.refactoring.Movie.*;

class CustomerTest {
    private static final Movie REGULAR_MOVIE = new Movie("A Regular Movie", REGULAR);
    private static final Movie NEW_RELEASE_MOVIE = new Movie("A New Release Movie", NEW_RELEASE);
    private static final Movie CHILDRENS_MOVIE = new Movie("A Children's Movie", CHILDRENS);

    private final Customer customer = new Customer("Curly");
    private final int ONE_DAY = 1;

    @DisplayName("A regular movie costs $2 for the first 2 days + $1.5 for each additional day")
    @ParameterizedTest(name = "A regular movie rented for {0} day(s) costs ${1}")
    @CsvSource({
            "1, 2.0",
            "2, 2.0",
            "3, 3.5",
            "4, 5.0"
    })
    void regularMoviePrice(int days, double expectedAmount) {
        customerRents(REGULAR_MOVIE).forPeriodOf(days);
        assertThat(amountOwed()).isEqualTo(expectedAmount);
    }

    @DisplayName("A regular movie always earns 1 point")
    @ParameterizedTest(name = "A regular movie rented for {0} day(s) earns 1 point")
    @ValueSource(ints = {1, 5, 10})
    void regularMoviePoints(int days) {
        customerRents(REGULAR_MOVIE).forPeriodOf(days);
        assertThat(frequentRenterPoints()).isEqualTo(1);
    }

    @DisplayName("A new release movie costs $3 per day")
    @ParameterizedTest(name = "A new release movie rented for {0} day(s) costs ${1}")
    @CsvSource({
            "1, 3.0",
            "5, 15.0",
            "10, 30.0"
    })
    void newReleaseMoviePrice(int days, double expectedAmount) {
        customerRents(NEW_RELEASE_MOVIE).forPeriodOf(days);
        assertThat(amountOwed()).isEqualTo(expectedAmount);
    }

    @DisplayName("A new release movie earns 1 point + a bonus point for more than 1 day")
    @ParameterizedTest(name = "A new release movie rented for {0} day(s) earns {1} point(s)")
    @CsvSource({
            "1, 1",
            "2, 2",
            "3, 2",
            "10, 2"
    })
    void newReleaseMoviePoints(int days, int expectedPoints) {
        customerRents(NEW_RELEASE_MOVIE).forPeriodOf(days);
        assertThat(frequentRenterPoints()).isEqualTo(expectedPoints);
    }

    @DisplayName("A children's movie costs $1.5 for the first 3 days + $1.5 for each additional day")
    @ParameterizedTest(name = "A childrens movie rented for {0} day(s) costs ${1}")
    @CsvSource({
            "1, 1.5",
            "2, 1.5",
            "3, 1.5",
            "4, 3.0",
            "5, 4.5"
    })
    void childrensMoviePrice(int days, double expectedAmount) {
        customerRents(CHILDRENS_MOVIE).forPeriodOf(days);
        assertThat(amountOwed()).isEqualTo(expectedAmount);
    }

    @DisplayName("A children's movie always earns 1 point")
    @ParameterizedTest(name = "A childrens movie rented for {0} day(s) earns 1 point")
    @ValueSource(ints = {1, 5, 10})
    void childrensMoviePoints(int days) {
        customerRents(CHILDRENS_MOVIE).forPeriodOf(days);
        assertThat(frequentRenterPoints()).isEqualTo(1);
    }

    @DisplayName("Multiple rentals are summed to give a total price")
    @ParameterizedTest(name = "One of each movie rented for {0} day(s) costs ${1}")
    @CsvSource({
            "1, 6.5",
            "2, 9.5"
    })
    void multipleRentalsSumPriceCorrectly(int days, double expectedAmount) {
        customerRents(REGULAR_MOVIE).forPeriodOf(days);
        customerRents(NEW_RELEASE_MOVIE).forPeriodOf(days);
        customerRents(CHILDRENS_MOVIE).forPeriodOf(days);
        assertThat(amountOwed()).isEqualTo(expectedAmount);
    }

    @DisplayName("Multiple rentals are summed to give the total points")
    @ParameterizedTest(name = "One of each movie rented for {0} day(s) earns {1} points")
    @CsvSource({
            "1, 3",
            "2, 4"
    })
    void multipleRentalsSumPointsCorrectly(int days, double expectedPoints) {
        customerRents(REGULAR_MOVIE).forPeriodOf(days);
        customerRents(NEW_RELEASE_MOVIE).forPeriodOf(days);
        customerRents(CHILDRENS_MOVIE).forPeriodOf(days);
        assertThat(frequentRenterPoints()).isEqualTo(expectedPoints);
    }

    @DisplayName("A customer with no rentals has an empty statement")
    @Test
    void noRentals() {
        assertThat(customerStatement()).isEqualTo(emptyStatement());
    }

    @DisplayName("A statement with a single row is formatted correctly")
    @Test
    void statementWithSingleRow() {
        customerRents(REGULAR_MOVIE).forPeriodOf(ONE_DAY);

        String expectedStatement = expectedStatement()
                .withRow(REGULAR_MOVIE, 2.0)
                .withAmountOwed(2.0)
                .withFrequentRenterPoints(1)
                .asText();

        assertThat(customerStatement()).isEqualTo(expectedStatement);
    }

    @DisplayName("A statement with multiple rows is formatted correctly")
    @Test
    void statementWithMultipleRows() {
        customerRents(REGULAR_MOVIE).forPeriodOf(ONE_DAY);
        customerRents(NEW_RELEASE_MOVIE).forPeriodOf(ONE_DAY);
        customerRents(CHILDRENS_MOVIE).forPeriodOf(ONE_DAY);

        String expectedStatement = expectedStatement()
                .withRow(REGULAR_MOVIE, 2.0)
                .withRow(NEW_RELEASE_MOVIE, 3.0)
                .withRow(CHILDRENS_MOVIE, 1.5)
                .withAmountOwed(6.5)
                .withFrequentRenterPoints(3)
                .asText();

        assertThat(customerStatement()).isEqualTo(expectedStatement);
    }

    private CustomerRental customerRents(Movie movie) {
        return new CustomerRental(customer, movie);
    }

    private static class CustomerRental {
        private Customer customer;
        private Movie movie;

        private CustomerRental(Customer customer, Movie movie) {
            this.customer = customer;
            this.movie = movie;
        }

        private void forPeriodOf(int daysRented) {
            customer.addRental(new Rental(movie, daysRented));
        }
    }

    private double amountOwed() {
        return amountOwedIn(customerStatement());
    }

    private double frequentRenterPoints() {
        return frequentRenterPointsIn(customerStatement());
    }

    private String customerStatement() {
        return customer.statement();
    }

    private ExpectedStatement expectedStatement() {
        return ExpectedStatement.forCustomer(customer);
    }

    private String emptyStatement() {
        return ExpectedStatement.forCustomer(customer).asText();
    }

    private static class ExpectedStatement {
        private final Customer customer;
        private List<ExpectedStatementRow> rows = new ArrayList<>();
        private double amountOwed = 0.0;
        private int frequentRenterPoints = 0;

        private ExpectedStatement(Customer customer) {
            this.customer = customer;
        }

        static ExpectedStatement forCustomer(Customer customer) {
            return new ExpectedStatement(customer);
        }

        ExpectedStatement withRow(Movie movie, double amount) {
            this.rows.add(new ExpectedStatementRow(movie.getTitle(), amount));
            return this;
        }

        ExpectedStatement withAmountOwed(double amountOwed) {
            this.amountOwed = amountOwed;
            return this;
        }

        ExpectedStatement withFrequentRenterPoints(int frequentRenterPoints) {
            this.frequentRenterPoints = frequentRenterPoints;
            return this;
        }

        String asText() {
            String result = "Rental Record for " + customer.getName() + "\n";
            for (ExpectedStatementRow row : rows) {
                result += "\t" + row.getTitle() + "\t" + row.getAmount() + "\n";
            }
            result += "Amount owed is " + amountOwed + "\n";
            result += "You earned " + frequentRenterPoints + " frequent renter points";
            return result;
        }

        private class ExpectedStatementRow {
            private final String title;
            private final double amount;

            private ExpectedStatementRow(String title, double amount) {
                this.title = title;
                this.amount = amount;
            }

            String getTitle() {
                return title;
            }

            double getAmount() {
                return amount;
            }
        }
    }

    private static double amountOwedIn(String statement) {
        return parseDouble(substringBetween(statement, "Amount owed is ", "\n"));
    }

    private static int frequentRenterPointsIn(String statement) {
        return parseInt(substringBetween(statement, "You earned ", " frequent renter points"));
    }
}
