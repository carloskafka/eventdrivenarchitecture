
package br.com.backend.strategy;

import br.com.backend.model.payment.Payment;
import br.com.backend.adapters.out.PaymentRepository;
import br.com.backend.model.payment.PaymentStatus;
import br.com.libdomain.model.Event;
import br.com.libdomain.strategy.EventStrategy;

public class PaymentApprovedStrategy implements EventStrategy {

    private final PaymentRepository paymentRepository;

    public PaymentApprovedStrategy(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public boolean supports(Event event) {
        return "PAYMENT_APPROVED".equals(event.type());
    }

    @Override
    public void execute(Event event) {
        String paymentId = (String) event.payload().get("paymentId");

        // Recupera agregado
        Payment payment = paymentRepository.findById(paymentId)
                .orElseGet(() -> new Payment(paymentId, PaymentStatus.CREATED));

        // Aplica evento (idempotente + transição de estado)
        boolean applied = payment.applyEvent(event.eventId(), PaymentStatus.APPROVED);

        if (applied) {
            paymentRepository.save(payment); // persiste alterações
            System.out.println("Pagamento aprovado: " + paymentId);
        } else {
            System.out.println("Evento ignorado (duplicado ou transição inválida): " + paymentId);
        }
    }
}
