package br.com.backend.adapters.in;

import br.com.backend.adapters.out.PaymentRepository;
import br.com.backend.application.usecases.ProcessPaymentEventUseCase;
import br.com.backend.model.payment.PaymentStatus;
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

    private final ProcessPaymentEventUseCase useCase;
    private final PaymentRepository repository;

    public StartupRunner(
            ProcessPaymentEventUseCase useCase,
            PaymentRepository repository
    ) {
        this.useCase = useCase;
        this.repository = repository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        scenario1_concurrentSameEvent();
        scenario2_idempotentReplay();
        scenario3_outOfOrderEvent();
        scenario4_concurrentDifferentEvents();
    }

    /* =======================================================
       SCENARIO 1 — Real concurrency (same event, 2 threads)
       ======================================================= */
    private void scenario1_concurrentSameEvent() throws Exception {
        System.out.println("\n=== SCENARIO 1: CONCURRENT SAME EVENT ===");

        String paymentId = "payment-1";
        UUID eventId = UUID.randomUUID();

        ExecutorService executor = Executors.newFixedThreadPool(2);

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

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        logFinalState(paymentId);
    }

    /* =======================================================
       SCENARIO 2 — Idempotency (same event replayed)
       ======================================================= */
    private void scenario2_idempotentReplay() {
        System.out.println("=== SCENARIO 2: IDEMPOTENT REPLAY ===");

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
        System.out.println("=== SCENARIO 3: OUT OF ORDER EVENT ===");

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
        System.out.println("=== SCENARIO 4: CONCURRENT DIFFERENT EVENTS ===");

        String paymentId = "payment-4";

        ExecutorService executor = Executors.newFixedThreadPool(2);

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

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        logFinalState(paymentId);
    }

    /* =======================================================
       FINAL LOG
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
}
