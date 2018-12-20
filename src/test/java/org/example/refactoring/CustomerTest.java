package org.example.refactoring;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class CustomerTest {

    private final Customer customer = new Customer("Curly");

    @DisplayName("A new customer should have an empty statement")
    @Test
    void newCustomerShouldHaveEmptyStatement() {
        assertThat(customer.statement()).isEqualTo(emptyStatement());
    }

    private String emptyStatement() {
        return statement(0.0, 0);
    }

    private String statement(final double amountOwed, final int frequentRenterPoints) {
        return "Rental Record for " + customer.getName() + "\n" +
                "Amount owed is " + amountOwed + "\n" +
                "You earned " + frequentRenterPoints + " frequent renter points";
    }
}
