package theater;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

/**
 * This class generates a statement for a given invoice of performances.
 */
public class StatementPrinter {

    private final Invoice invoice;
    private final Map<String, Play> plays;

    /**
     * Create a new StatementPrinter for the given invoice and plays.
     *
     * @param invoice the invoice to print
     * @param plays   the mapping from play id to play
     */
    public StatementPrinter(Invoice invoice, Map<String, Play> plays) {
        this.invoice = invoice;
        this.plays = plays;
    }

    /**
     * Returns a formatted statement of the invoice associated with this printer.
     *
     * @return the formatted statement
     * @throws RuntimeException if one of the play types is not known
     */
    public String statement() {
        final StringBuilder result = new StringBuilder(
                String.format("Statement for %s%n", invoice.getCustomer()));

        for (Performance performance : invoice.getPerformances()) {
            // print line for this order
            result.append(String.format("  %s: %s (%s seats)%n",
                    getPlay(performance).getName(),
                    usd(getAmount(performance)),
                    performance.getAudience()));
        }

        result.append(String.format("Amount owed is %s%n", usd(getTotalAmount())));
        result.append(String.format("You earned %s credits%n", getTotalVolumeCredits()));
        return result.toString();
    }

    /**
     * Return the play corresponding to the given performance.
     *
     * @param performance the performance
     * @return the play for this performance
     */
    private Play getPlay(Performance performance) {
        return plays.get(performance.getPlayID());
    }

    /**
     * Return the base amount (in cents) for this performance.
     *
     * @param performance the performance
     * @return the amount in cents
     * @throws RuntimeException if the play type is unknown
     */
    private int getAmount(Performance performance) {
        final Play play = getPlay(performance);
        final String type = play.getType();
        int result;

        switch (type) {
            case "tragedy":
                result = Constants.TRAGEDY_BASE_AMOUNT;
                if (performance.getAudience() > Constants.TRAGEDY_AUDIENCE_THRESHOLD) {
                    result += Constants.TRAGEDY_OVER_BASE_CAPACITY_PER_PERSON
                            * (performance.getAudience()
                            - Constants.TRAGEDY_AUDIENCE_THRESHOLD);
                }
                break;
            case "comedy":
                result = Constants.COMEDY_BASE_AMOUNT;
                if (performance.getAudience() > Constants.COMEDY_AUDIENCE_THRESHOLD) {
                    result += Constants.COMEDY_OVER_BASE_CAPACITY_AMOUNT
                            + Constants.COMEDY_OVER_BASE_CAPACITY_PER_PERSON
                            * (performance.getAudience()
                            - Constants.COMEDY_AUDIENCE_THRESHOLD);
                }
                result += Constants.COMEDY_AMOUNT_PER_AUDIENCE * performance.getAudience();
                break;
            default:
                throw new RuntimeException(
                        String.format("unknown type: %s", type));
        }

        return result;
    }

    /**
     * Return the volume credits earned for this performance.
     *
     * @param performance the performance
     * @return the volume credits for this performance
     */
    private int getVolumeCredits(Performance performance) {
        int result = 0;
        result += Math.max(performance.getAudience()
                        - Constants.BASE_VOLUME_CREDIT_THRESHOLD,
                0);
        if ("comedy".equals(getPlay(performance).getType())) {
            result += performance.getAudience() / Constants.COMEDY_EXTRA_VOLUME_FACTOR;
        }
        return result;
    }

    /**
     * Format an amount in cents as a US dollar string.
     *
     * @param amount the amount in cents
     * @return the formatted string
     */
    private String usd(int amount) {
        final NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
        return format.format(amount / (double) Constants.PERCENT_FACTOR);
    }

    /**
     * Return the total amount owed for this invoice.
     *
     * @return total amount in cents
     */
    private int getTotalAmount() {
        int result = 0;
        for (Performance performance : invoice.getPerformances()) {
            result += getAmount(performance);
        }
        return result;
    }

    /**
     * Return the total volume credits earned for this invoice.
     *
     * @return total volume credits
     */
    private int getTotalVolumeCredits() {
        int result = 0;
        for (Performance performance : invoice.getPerformances()) {
            result += getVolumeCredits(performance);
        }
        return result;
    }
}
