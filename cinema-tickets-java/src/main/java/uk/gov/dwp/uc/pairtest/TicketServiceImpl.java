package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type.*;

public class TicketServiceImpl implements TicketService {
    private static final int MAX_TICKET_NUMBER = 25;
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
        validate(accountId, ticketTypeRequests);

        Map<TicketTypeRequest.Type, Integer> ticketTotals = mapTicketTypeTotals(ticketTypeRequests);

        int total = calculateTotalPrice(ticketTotals);
        int seats = calculateSeats(ticketTotals);

        ticketPaymentService.makePayment(accountId, total);
        seatReservationService.reserveSeat(accountId, seats);
    }

    private int calculateTotalPrice(Map<TicketTypeRequest.Type, Integer> ticketTotals) {
        return ticketTotals.entrySet().stream().mapToInt(t -> t.getValue() * ticketPrices.get(t.getKey())).sum();
    }

    private int calculateSeats(Map<TicketTypeRequest.Type, Integer> ticketTotals) {
        return ticketTotals.entrySet().stream().filter(t -> t.getKey() != INFANT).mapToInt(Map.Entry::getValue).sum();
    }

    private void validate(Long accountId, TicketTypeRequest... ticketTypeRequests) {
        if (accountId == null || accountId == 0L) {
            throw new InvalidPurchaseException();
        }
        if (ticketTypeRequests == null || ticketTypeRequests.length == 0) {
            throw new InvalidPurchaseException();
        }
        if (!Arrays.stream(ticketTypeRequests).allMatch(t -> t.getNoOfTickets() > 0)) {
            throw new InvalidPurchaseException();
        }
        Map<TicketTypeRequest.Type, Integer> ticketTotals = mapTicketTypeTotals(ticketTypeRequests);
        if (ticketTotals.getOrDefault(ADULT, 0) <= 0) {
            throw new InvalidPurchaseException();
        }
        if (ticketTotals.getOrDefault(ADULT, 0) < ticketTotals.getOrDefault(INFANT, 0)) {
            throw new InvalidPurchaseException();
        }
        int total = ticketTotals.values().stream().mapToInt(Integer::intValue).sum();
        if (total > MAX_TICKET_NUMBER) {
            throw new InvalidPurchaseException();
        }
    }

    private Map<TicketTypeRequest.Type, Integer> mapTicketTypeTotals(TicketTypeRequest... ticketTypeRequests) {
        return Arrays.stream(ticketTypeRequests)
                .filter(t -> ticketPrices.containsKey(t.getTicketType()) && t.getTicketType() != null)
                .collect(Collectors.toMap(
                        TicketTypeRequest::getTicketType,
                        TicketTypeRequest::getNoOfTickets
                ));
    }

}
