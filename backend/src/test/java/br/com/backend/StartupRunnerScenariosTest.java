package br.com.backend;

import br.com.backend.adapters.out.PaymentRepository;
import br.com.backend.application.usecases.ProcessPaymentEventUseCase;
import br.com.backend.model.payment.PaymentStatus;
import br.com.backend.util.AutoCloseableExecutor;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class StartupRunnerScenariosTest {

    private final ProcessPaymentEventUseCase useCase;

    private final PaymentRepository repository;

    @Autowired
    public StartupRunnerScenariosTest(ProcessPaymentEventUseCase useCase, PaymentRepository repository) {
        this.useCase = useCase;
        this.repository = repository;
    }

    @Test
    @DisplayName("Scenario 1 — Concurrent same event (two threads)")
    public void scenario1_concurrentSameEvent() throws Exception {
        String paymentId = "test-payment-1";
        UUID eventId = UUID.randomUUID();

        try (AutoCloseableExecutor ace = new AutoCloseableExecutor(2)) {
            ExecutorService executor = ace.executor();
            CountDownLatch startLatch = new CountDownLatch(1);

            Callable<Void> task = () -> {
                startLatch.await();
                useCase.execute(eventId, paymentId, PaymentStatus.AUTHORIZED);
                return null;
            };

            Future<Void> f1 = executor.submit(task);
            Future<Void> f2 = executor.submit(task);

            // release both tasks at the same time to increase chance of race
            startLatch.countDown();

            // Wait for tasks and rethrow exceptions if any
            try {
                f1.get();
                f2.get();
            } catch (ExecutionException e) {
                // If an underlying task threw OptimisticLockException, unwrap and fail the test accordingly
                Throwable cause = e.getCause();
                if (cause instanceof OptimisticLockException) {
                    // In concurrent scenario an OptimisticLockException is acceptable (one thread may lose the race)
                    System.out.println("Optimistic lock occurred (acceptable): " + cause.getMessage());
                } else {
                    throw e;
                }
            }
        }

        var opt = repository.findById(paymentId);
        assertTrue(opt.isPresent());
        var payment = opt.get();
        assertEquals(PaymentStatus.AUTHORIZED, payment.getStatus());
    }

    @Test
    @DisplayName("Scenario 2 — Idempotent replay of the same event")
    public void scenario2_idempotentReplay() {
        String paymentId = "test-payment-2";
        UUID eventId = UUID.randomUUID();

        useCase.execute(eventId, paymentId, PaymentStatus.AUTHORIZED);
        useCase.execute(eventId, paymentId, PaymentStatus.AUTHORIZED);

        var opt = repository.findById(paymentId);
        assertTrue(opt.isPresent());
        var payment = opt.get();
        assertEquals(PaymentStatus.AUTHORIZED, payment.getStatus());
    }

    @Test
    @DisplayName("Scenario 3 — Out of order events: APPROVED before AUTHORIZED")
    public void scenario3_outOfOrderEvent() {
        String paymentId = "test-payment-3";

        useCase.execute(UUID.randomUUID(), paymentId, PaymentStatus.APPROVED);
        useCase.execute(UUID.randomUUID(), paymentId, PaymentStatus.AUTHORIZED);

        var opt = repository.findById(paymentId);
        assertTrue(opt.isPresent());
        var payment = opt.get();
        // APPROVED cannot transition to AUTHORIZED, so expect the status to be APPROVED
        assertEquals(PaymentStatus.APPROVED, payment.getStatus());
    }

    @Test
    @DisplayName("Scenario 4 — Concurrent different events: AUTHORIZED vs FAILED")
    public void scenario4_concurrentDifferentEvents() throws Exception {
        String paymentId = "test-payment-4";

        try (AutoCloseableExecutor ace = new AutoCloseableExecutor(2)) {
            ExecutorService executor = ace.executor();
            CountDownLatch startLatch = new CountDownLatch(1);

            Callable<Void> authorize = () -> {
                startLatch.await();
                useCase.execute(UUID.randomUUID(), paymentId, PaymentStatus.AUTHORIZED);
                return null;
            };
            Callable<Void> fail = () -> {
                startLatch.await();
                useCase.execute(UUID.randomUUID(), paymentId, PaymentStatus.FAILED);
                return null;
            };

            Future<Void> f1 = executor.submit(authorize);
            Future<Void> f2 = executor.submit(fail);

            // release both tasks together
            startLatch.countDown();

            try {
                f1.get();
                f2.get();
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof OptimisticLockException) {
                    // It's acceptable to have optimistic lock in concurrent scenario; continue to assertion
                    System.out.println("OptimisticLockException occurred in concurrent scenario (acceptable)");
                } else {
                    throw e;
                }
            }
        }

        var opt = repository.findById(paymentId);
        assertTrue(opt.isPresent());
        var payment = opt.get();

        // The final state must be either AUTHORIZED or FAILED depending on interleaving
        assertTrue(payment.getStatus() == PaymentStatus.AUTHORIZED || payment.getStatus() == PaymentStatus.FAILED);
    }
}
