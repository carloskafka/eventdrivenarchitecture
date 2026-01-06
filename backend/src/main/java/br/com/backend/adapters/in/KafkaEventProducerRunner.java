package br.com.backend.adapters.in;

import br.com.backend.model.payment.PaymentStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import br.com.libintegration.adapters.kafka.KafkaSender;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Producer runner that simulates sending payment events to Kafka.
 *
 * Disabled by default. To enable, set the environment variable:
 *
 * KAFKA_PRODUCER_ENABLED=true
 *
 * The runner publishes to topic `payment-events` (default used by the listener).
 */
@Component
public class KafkaEventProducerRunner implements ApplicationRunner {

    private final KafkaSender kafkaSender;
    private final ObjectMapper mapper = new ObjectMapper();
    private final boolean enabled;
    private final String topic = "payment-events";

    public KafkaEventProducerRunner(KafkaSender kafkaSender) {
        this.kafkaSender = kafkaSender;
        String env = System.getenv("KAFKA_PRODUCER_ENABLED");
        this.enabled = env != null && (env.equalsIgnoreCase("1") || env.equalsIgnoreCase("true"));
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!enabled) {
            System.out.println("[KAFKA-PRODUCER] Disabled (set KAFKA_PRODUCER_ENABLED=true to enable)");
            return;
        }

        System.out.println("[KAFKA-PRODUCER] Starting scenarios (topic: " + topic + ")");

        scenario1_concurrentSameEvent();
        scenario2_idempotentReplay();
        scenario3_outOfOrderEvent();
        scenario4_concurrentDifferentEvents();

        System.out.println("[KAFKA-PRODUCER] Finished scenarios");
    }

    private void sendEvent(UUID eventId, String paymentId, PaymentStatus status) throws Exception {
        PaymentEventDto dto = new PaymentEventDto();
        dto.setEventId(eventId);
        dto.setPaymentId(paymentId);
        dto.setStatus(status.name());
        String json = mapper.writeValueAsString(dto);
        kafkaSender.send(topic, json);
        System.out.println("[KAFKA-PRODUCER] Sent: " + json);
    }

    private void scenario1_concurrentSameEvent() throws Exception {
        System.out.println("[KAFKA-PRODUCER] SCENARIO 1 - CONCURRENT SAME EVENT");
        String paymentId = "kafka-payment-1";
        UUID eventId = UUID.randomUUID();

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            CountDownLatch latch = new CountDownLatch(1);
            Callable<Void> task = () -> {
                latch.await();
                sendEvent(eventId, paymentId, PaymentStatus.AUTHORIZED);
                return null;
            };

            Future<Void> f1 = executor.submit(task);
            Future<Void> f2 = executor.submit(task);
            latch.countDown();
            f1.get();
            f2.get();
        }
    }

    private void scenario2_idempotentReplay() throws Exception {
        System.out.println("[KAFKA-PRODUCER] SCENARIO 2 - IDEMPOTENT REPLAY");
        String paymentId = "kafka-payment-2";
        UUID eventId = UUID.randomUUID();
        sendEvent(eventId, paymentId, PaymentStatus.AUTHORIZED);
        // replay same event
        sendEvent(eventId, paymentId, PaymentStatus.AUTHORIZED);
    }

    private void scenario3_outOfOrderEvent() throws Exception {
        System.out.println("[KAFKA-PRODUCER] SCENARIO 3 - OUT OF ORDER: APPROVED before AUTHORIZED");
        String paymentId = "kafka-payment-3";
        // APPROVED first
        sendEvent(UUID.randomUUID(), paymentId, PaymentStatus.APPROVED);
        // then AUTHORIZED
        sendEvent(UUID.randomUUID(), paymentId, PaymentStatus.AUTHORIZED);
    }

    private void scenario4_concurrentDifferentEvents() throws Exception {
        System.out.println("[KAFKA-PRODUCER] SCENARIO 4 - CONCURRENT DIFFERENT EVENTS (AUTHORIZED vs FAILED)");
        String paymentId = "kafka-payment-4";

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            CountDownLatch latch = new CountDownLatch(1);
            Callable<Void> authorize = () -> {
                latch.await();
                sendEvent(UUID.randomUUID(), paymentId, PaymentStatus.AUTHORIZED);
                return null;
            };
            Callable<Void> fail = () -> {
                latch.await();
                sendEvent(UUID.randomUUID(), paymentId, PaymentStatus.FAILED);
                return null;
            };

            Future<Void> f1 = executor.submit(authorize);
            Future<Void> f2 = executor.submit(fail);
            latch.countDown();
            f1.get();
            f2.get();
        }
    }
}
