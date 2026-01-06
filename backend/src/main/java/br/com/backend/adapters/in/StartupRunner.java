package br.com.backend.adapters.in;

import br.com.backend.adapters.out.OrderRepository;
import br.com.backend.adapters.out.PaymentRepository;
import br.com.backend.adapters.out.StockRepository;
import br.com.backend.application.usecases.ProcessPaymentEventUseCase;
import br.com.backend.model.order.Order;
import br.com.backend.model.order.OrderStatus;
import br.com.backend.model.payment.PaymentStatus;
import br.com.backend.model.stock.Stock;
import jakarta.persistence.OptimisticLockException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Class to execute demo scenarios on application startup.
 */
@Component
public class StartupRunner implements ApplicationRunner {

    // ANSI color codes for console (will be ignored on consoles that don't support ANSI)
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BOLD = "\u001B[1m";
    private static final String ANSI_CYAN = "\u001B[36m";
    // Enable colors by default; set NO_COLOR=1 in environment to disable
    private static final boolean ENABLE_COLOR = System.getenv("NO_COLOR") == null;

    private final ProcessPaymentEventUseCase useCase;
    private final PaymentRepository repository;
    private final OrderRepository orderRepository;
    private final StockRepository stockRepository;

    public StartupRunner(
            ProcessPaymentEventUseCase useCase,
            PaymentRepository repository,
            OrderRepository orderRepository,
            StockRepository stockRepository
    ) {
        this.useCase = useCase;
        this.repository = repository;
        this.orderRepository = orderRepository;
        this.stockRepository = stockRepository;
    }

    // Helper to print colored scenario headings
    private void printScenarioHeading(String heading) {
        if (ENABLE_COLOR) {
            System.out.println(ANSI_BOLD + ANSI_CYAN + heading + ANSI_RESET);
        } else {
            System.out.println(heading);
        }
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        scenario1_concurrentSameEvent();
        scenario2_idempotentReplay();
        scenario3_outOfOrderEvent();
        scenario4_concurrentDifferentEvents();

        // New scenarios
        scenario5_paymentThenOrderThenStock();
        scenario6_reserveThenCancelReleasesStock();
        scenario7_orderLifecycle();
    }

    /* =======================================================
       SCENARIO 1 — Real concurrency (same event, 2 threads)
       ======================================================= */
    private void scenario1_concurrentSameEvent() throws Exception {
        printScenarioHeading("\n=== SCENARIO 1: CONCURRENT SAME EVENT ===");

        String paymentId = "payment-1";
        UUID eventId = UUID.randomUUID();

        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Runnable task = () -> {
                try {
                    useCase.execute(eventId, paymentId, PaymentStatus.AUTHORIZED);
                    System.out.println(Thread.currentThread().getName() + " SUCCESS");
                } catch (OptimisticLockException e) {
                    System.out.println(Thread.currentThread().getName() + " CONFLICT");
                }
            };

            executor.submit(task);
            executor.submit(task);

        } finally {
            executor.shutdown();
            boolean terminated = executor.awaitTermination(5, TimeUnit.SECONDS);
            if (!terminated) {
                System.out.println("Executor did not terminate in time, forcing shutdown");
                executor.shutdownNow();
            }
        }

        logFinalState(paymentId);
    }

    /* =======================================================
       SCENARIO 2 — Idempotency (same event replayed)
       ======================================================= */
    private void scenario2_idempotentReplay() {
        printScenarioHeading("=== SCENARIO 2: IDEMPOTENT REPLAY ===");

        String paymentId = "payment-2";
        UUID eventId = UUID.randomUUID();

        useCase.execute(eventId, paymentId, PaymentStatus.AUTHORIZED);
        useCase.execute(eventId, paymentId, PaymentStatus.AUTHORIZED);

        logFinalState(paymentId);
    }

    /* =======================================================
       SCENARIO 3 — Out of order event
       APPROVED before AUTHORIZED
       ======================================================= */
    private void scenario3_outOfOrderEvent() {
        printScenarioHeading("=== SCENARIO 3: OUT OF ORDER EVENT ===");

        String paymentId = "payment-3";

        useCase.execute(UUID.randomUUID(), paymentId, PaymentStatus.APPROVED);
        useCase.execute(UUID.randomUUID(), paymentId, PaymentStatus.AUTHORIZED);

        logFinalState(paymentId);
    }

    /* =======================================================
       SCENARIO 4 — Different concurrent events
       AUTHORIZED x FAILED
       ======================================================= */
    private void scenario4_concurrentDifferentEvents() throws Exception {
        printScenarioHeading("=== SCENARIO 4: CONCURRENT DIFFERENT EVENTS ===");

        String paymentId = "payment-4";

        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Runnable authorize = () -> {
                try {
                    useCase.execute(
                            UUID.randomUUID(),
                            paymentId,
                            PaymentStatus.AUTHORIZED
                    );
                    System.out.println("AUTHORIZE SUCCESS");
                } catch (OptimisticLockException e) {
                    System.out.println("AUTHORIZE CONFLICT");
                }
            };

            Runnable fail = () -> {
                try {
                    useCase.execute(
                            UUID.randomUUID(),
                            paymentId,
                            PaymentStatus.FAILED
                    );
                    System.out.println("FAIL SUCCESS");
                } catch (OptimisticLockException e) {
                    System.out.println("FAIL CONFLICT");
                }
            };

            executor.submit(authorize);
            executor.submit(fail);

        } finally {
            executor.shutdown();
            boolean terminated = executor.awaitTermination(5, TimeUnit.SECONDS);
            if (!terminated) {
                System.out.println("Executor did not terminate in time, forcing shutdown");
                executor.shutdownNow();
            }
        }

        logFinalState(paymentId);
    }

    /* =======================================================
       SCENARIO 5 — Payment then Order then Stock reservation
       ======================================================= */
    private void scenario5_paymentThenOrderThenStock() {
        printScenarioHeading("=== SCENARIO 5: PAYMENT -> ORDER -> STOCK ===");

        String paymentId = "payment-5";
        String orderId = "order-5";
        String productId = "prod-5";

        // initial stock
        stockRepository.save(new Stock(productId, 10));

        // payment
        useCase.execute(UUID.randomUUID(), paymentId, PaymentStatus.AUTHORIZED);

        // create order and confirm
        Order order = new Order(orderId);
        order.addItem(productId, 2);
        order.applyStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        // reserve stock
        stockRepository.findById(productId).ifPresent(s -> {
            boolean ok = s.reserve(2);
            if (ok) {
                stockRepository.save(s);
                System.out.println("Stock reserved for order " + orderId);
            } else {
                System.out.println("Insufficient stock for order " + orderId);
            }
        });

        logFinalState(paymentId);
        logOrderState(orderId);
        logStockState(productId);
    }

    /* =======================================================
       SCENARIO 6 — Reserve then cancel releases stock
       ======================================================= */
    private void scenario6_reserveThenCancelReleasesStock() {
        printScenarioHeading("=== SCENARIO 6: RESERVE THEN CANCEL RELEASE ===");

         String orderId = "order-6";
         String productId = "prod-6";

         stockRepository.save(new Stock(productId, 1));

         // simulate order
         Order order = new Order(orderId);
         order.addItem(productId, 1);
         order.applyStatus(OrderStatus.CONFIRMED);
         orderRepository.save(order);

         stockRepository.findById(productId).ifPresent(s -> {
             boolean ok = s.reserve(1);
             if (ok) {
                 stockRepository.save(s);
                 System.out.println("Reserved 1 unit for " + orderId);
             }
         });

         // cancel order and release
         order.applyStatus(OrderStatus.CANCELLED);
         orderRepository.save(order);
         stockRepository.findById(productId).ifPresent(s -> {
             s.release(1);
             stockRepository.save(s);
             System.out.println("Released 1 unit for cancelled order " + orderId);
         });

         logOrderState(orderId);
         logStockState(productId);
     }

    /* =======================================================
       SCENARIO 7 — Order lifecycle
       ======================================================= */
    private void scenario7_orderLifecycle() {
        printScenarioHeading("=== SCENARIO 7: ORDER LIFECYCLE ===");

        String orderId = "order-7";

        Order order = new Order(orderId);
        boolean added = order.addItem("prod-x", 1);
        System.out.println("Add item to NEW order: " + added);

        boolean confirmed = order.applyStatus(OrderStatus.CONFIRMED);
        System.out.println("Confirm order: " + confirmed);

        boolean shipped = order.applyStatus(OrderStatus.SHIPPED);
        System.out.println("Ship order: " + shipped);

        boolean addAfterShip = order.addItem("prod-x", 1);
        System.out.println("Add item after shipped (should be false): " + addAfterShip);

        orderRepository.save(order);
        logOrderState(orderId);
    }

    /* =======================================================
       LOG HELPERS
       ======================================================= */
    private void logFinalState(String paymentId) {
        repository.findById(paymentId).ifPresent(payment ->
                System.out.println(
                        "FINAL STATE -> id=" + paymentId +
                                ", status=" + payment.getStatus() +
                                ", version=" + payment.getVersion()
                )
        );
    }

    private void logOrderState(String orderId) {
        orderRepository.findById(orderId).ifPresent(o ->
                System.out.println("ORDER STATE -> id=" + orderId + ", status=" + o.getStatus())
        );
    }

    private void logStockState(String productId) {
        stockRepository.findById(productId).ifPresent(s ->
                System.out.println("STOCK STATE -> product=" + productId + ", qty=" + s.getQuantity())
        );
    }
}
