package br.com.backend;

import br.com.backend.adapters.out.OrderRepository;
import br.com.backend.adapters.out.StockRepository;
import br.com.backend.model.order.Order;
import br.com.backend.model.order.OrderStatus;
import br.com.backend.model.stock.Stock;
import br.com.backend.model.payment.PaymentStatus;
import br.com.backend.adapters.out.PaymentRepository;
import br.com.backend.application.usecases.ProcessPaymentEventUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DomainFlowsIntegrationTest {

    private final ProcessPaymentEventUseCase useCase;

    private final PaymentRepository paymentRepository;

    private final OrderRepository orderRepository;

    private final StockRepository stockRepository;

    @Autowired
    public DomainFlowsIntegrationTest(ProcessPaymentEventUseCase useCase,
                                      PaymentRepository paymentRepository,
                                      OrderRepository orderRepository,
                                      StockRepository stockRepository) {
        this.useCase = useCase;
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.stockRepository = stockRepository;
    }

    @Test
    @DisplayName("Payment -> Order -> Stock reservation flow")
    public void paymentThenOrderThenStock() {
        String paymentId = "flow-payment-1";
        String orderId = "flow-order-1";
        String productId = "prod-1";

        // initial stock
        Stock stock = new Stock(productId, 10);
        stockRepository.save(stock);

        // pay
        useCase.execute(UUID.randomUUID(), paymentId, PaymentStatus.AUTHORIZED);

        // create order and reserve stock
        Order order = new Order(orderId);
        order.addItem(productId, 2);
        order.applyStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        Stock s = stockRepository.findById(productId).orElseThrow();
        boolean reserved = s.reserve(2);
        assertTrue(reserved);
        stockRepository.save(s);

        // verify payment and order
        var p = paymentRepository.findById(paymentId).orElseThrow();
        assertEquals(PaymentStatus.AUTHORIZED, p.getStatus());

        var o = orderRepository.findById(orderId).orElseThrow();
        assertEquals(OrderStatus.CONFIRMED, o.getStatus());
    }

    @Test
    @DisplayName("Concurrent payment apply (idempotency)")
    public void concurrentPaymentConflict() throws Exception {
        String paymentId = "flow-payment-2";
        UUID eventId = UUID.randomUUID();

        Thread t1 = new Thread(() -> useCase.execute(eventId, paymentId, PaymentStatus.AUTHORIZED));
        Thread t2 = new Thread(() -> useCase.execute(eventId, paymentId, PaymentStatus.AUTHORIZED));

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        var p = paymentRepository.findById(paymentId).orElseThrow();
        assertEquals(PaymentStatus.AUTHORIZED, p.getStatus());
    }

    @Test
    @DisplayName("Out-of-order: APPROVED before AUTHORIZED")
    public void outOfOrderApprovedBeforeAuthorized() {
        String paymentId = "flow-payment-3";

        useCase.execute(UUID.randomUUID(), paymentId, PaymentStatus.APPROVED);
        useCase.execute(UUID.randomUUID(), paymentId, PaymentStatus.AUTHORIZED);

        var p = paymentRepository.findById(paymentId).orElseThrow();
        assertEquals(PaymentStatus.APPROVED, p.getStatus());
    }
}
