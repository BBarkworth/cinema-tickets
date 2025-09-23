import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.TicketServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;
import static uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type.*;

public class TicketServiceImplTest {

    @Mock
    private TicketPaymentService ticketPaymentService;

    @Mock
    private SeatReservationService seatReservationService;

    @InjectMocks
    private TicketServiceImpl victim;

    @BeforeEach
    public void setup() {
        openMocks(this);
    }

    @Test
    void shouldPurchaseAdultTicket() {
        victim.purchaseTickets(1L, createTicketRequest(ADULT, 1));

        verify(ticketPaymentService, times(1)).makePayment(1L, 25);
        verify(seatReservationService, times(1)).reserveSeat(1L, 1);
    }

    @Test
    void shouldThrowExceptionForNullAccountId() {
        assertThrows(InvalidPurchaseException.class, () -> {
            victim.purchaseTickets(null, createTicketRequest(ADULT, 1));
        });

        verifyNoInteractions(ticketPaymentService, seatReservationService);
    }

    @Test
    void shouldThrowExceptionForZeroAccountId() {
        assertThrows(InvalidPurchaseException.class, () -> {
            victim.purchaseTickets(0L, createTicketRequest(ADULT, 1));
        });

        verifyNoInteractions(ticketPaymentService, seatReservationService);
    }

    @Test
    void shouldPurchaseMultipleTickets() {
        victim.purchaseTickets(2L,
                createTicketRequest(ADULT, 2),
                createTicketRequest(CHILD, 2),
                createTicketRequest(INFANT, 2));

        verify(ticketPaymentService, times(1)).makePayment(2L, 80);
        verify(seatReservationService, times(1)).reserveSeat(2L, 4);
    }

    @Test
    void shouldThrowExceptionForChildTicket() {
        assertThrows(InvalidPurchaseException.class, () -> {
            victim.purchaseTickets(1L, createTicketRequest(CHILD, 1));
        });

        verifyNoInteractions(ticketPaymentService, seatReservationService);
    }

    @Test
    void shouldThrowExceptionForInfantTicket() {
        assertThrows(InvalidPurchaseException.class, () -> {
            victim.purchaseTickets(1L, createTicketRequest(INFANT, 1));
        });

        verifyNoInteractions(ticketPaymentService, seatReservationService);
    }

    @Test
    void shouldThrowExceptionForInfantAndChildTickets() {
        assertThrows(InvalidPurchaseException.class, () -> {
            victim.purchaseTickets(1L,
                    createTicketRequest(CHILD, 1),
                    createTicketRequest(INFANT, 1));
        });

        verifyNoInteractions(ticketPaymentService, seatReservationService);
    }

    @Test
    void shouldPurchaseMaximumNumberOfTickets() {
        victim.purchaseTickets(1L,
                createTicketRequest(ADULT, 10),
                createTicketRequest(CHILD, 15));

        verify(ticketPaymentService, times(1)).makePayment(1L, 475);
        verify(seatReservationService, times(1)).reserveSeat(1L, 25);
    }

    @Test
    void shouldThrowExceptionForOverTwentyFiveTickets() {
        assertThrows(InvalidPurchaseException.class, () -> {
            victim.purchaseTickets(1L,
                    createTicketRequest(CHILD, 6),
                    createTicketRequest(INFANT, 10),
                    createTicketRequest(ADULT, 10));
        });

        verifyNoInteractions(ticketPaymentService, seatReservationService);
    }

    @Test
    void shouldThrowExceptionForMoreInfantsThanAdults() {
        assertThrows(InvalidPurchaseException.class, () -> {
            victim.purchaseTickets(1L,
                    createTicketRequest(INFANT, 6),
                    createTicketRequest(ADULT, 5));
        });

        verifyNoInteractions(ticketPaymentService, seatReservationService);
    }

    @Test
    void shouldThrowExceptionForNullTicketRequest() {
        TicketTypeRequest[] ticketArray = null;
        assertThrows(InvalidPurchaseException.class, () -> {
            victim.purchaseTickets(1L, ticketArray);
        });

        verifyNoInteractions(ticketPaymentService, seatReservationService);
    }

    @Test
    void shouldThrowExceptionForEmptyTicketRequest() {
        assertThrows(InvalidPurchaseException.class, () -> {
            victim.purchaseTickets(1L);
        });

        verifyNoInteractions(ticketPaymentService, seatReservationService);
    }

    @Test
    void shouldThrowExceptionForNegativeTicketsRequest() {
        assertThrows(InvalidPurchaseException.class, () -> {
                    victim.purchaseTickets(1L,
                            createTicketRequest(ADULT, 2),
                            createTicketRequest(INFANT, 1),
                            createTicketRequest(CHILD, -2));
        });
        verifyNoInteractions(ticketPaymentService, seatReservationService);
    }

    public TicketTypeRequest createTicketRequest(TicketTypeRequest.Type ticketType, int ticketNumber) {
        return new TicketTypeRequest(ticketType, ticketNumber);
    }
}
