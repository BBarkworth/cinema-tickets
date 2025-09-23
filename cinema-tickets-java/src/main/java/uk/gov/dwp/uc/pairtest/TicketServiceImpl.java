package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.Arrays;
import java.util.Map;

import static uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type.*;

public class TicketServiceImpl implements TicketService {
    private static final Map<TicketTypeRequest.Type, Integer> ticketPrices = Map.of(
            ADULT, 25,
            CHILD, 15,
            INFANT, 0
    );
    private final TicketPaymentService ticketPaymentService;
    private final SeatReservationService seatReservationService;

    public TicketServiceImpl(TicketPaymentService ticketPaymentService, SeatReservationService seatReservationService) {
        this.ticketPaymentService = ticketPaymentService;
        this.seatReservationService = seatReservationService;
    }
    /**
     * Should only have private methods other than the one below.
     */

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        validate(accountId);

        int total = calculateTotalPrice(ticketTypeRequests);
        int seats = calculateSeats(ticketTypeRequests);

        ticketPaymentService.makePayment(accountId, total);
        seatReservationService.reserveSeat(accountId, seats);
    }

    private int calculateTotalPrice(TicketTypeRequest... ticketTypeRequests) {
        return Arrays.stream(ticketTypeRequests)
                .filter(t -> ticketPrices.containsKey(t.getTicketType()))
                .mapToInt(t -> ticketPrices.get(t.getTicketType()) * t.getNoOfTickets())
                .sum();
    }

    private int calculateSeats(TicketTypeRequest... ticketTypeRequests) {
        return Arrays.stream(ticketTypeRequests)
                .filter(t -> ticketPrices.containsKey(t.getTicketType()) && t.getTicketType() != INFANT)
                .mapToInt(TicketTypeRequest::getNoOfTickets)
                .sum();
    }

    private void validate(Long accountId) {
        if (accountId == null || accountId == 0L) {
            throw new InvalidPurchaseException();
        }
    }

}
