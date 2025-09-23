import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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

    public TicketTypeRequest createTicketRequest(TicketTypeRequest.Type ticketType, int ticketNumber) {
        return new TicketTypeRequest(ticketType, ticketNumber);
    }
}
