
## 3️⃣ Soluções recomendadas

Para microsserviços **escalares** com Kafka e múltiplos pods, normalmente você faz:

### a) Controle de concorrência via banco (Optimistic Lock)

-   O seu Payment já tem `version` ou pode usar **@Version** do JPA.

-   Quando dois pods tentarem atualizar o mesmo Payment ao mesmo tempo, **somente um vai conseguir**, o outro recebe `OptimisticLockException` e deve **re-tentar**.

-   Isso garante **consistência eventual** do estado do Payment.


### b) Idempotência

-   Como você já armazena `processedEventIds`, mesmo que o mesmo pod ou outro pod processe o evento, ele **não aplica duas vezes**.

-   Isso garante que **o Payment não vai transicionar erroneamente**.


### c) Saga distribuída

-   Não use eventos locais do Spring (`ApplicationEvent`) para pods múltiplos.

-   Use **Kafka** (ou outro broker) como mecanismo de pub/sub:

   -   Cada evento `PaymentApprovedDomainEvent` é publicado no tópico da Saga.

   -   **A saga do Order** deve ser **single consumer por Order** para evitar duplicidade.

   -   Ex.:

      -   Configure Kafka com `group.id` único para todos os pods da saga.

      -   Kafka garante que **cada evento será consumido por apenas um pod do grupo**.


### d) PendingEvents persistidos

-   Atualmente você armazena pendingEvents em memória (`List<PendingPaymentEvent>`).

-   Em múltiplos pods, isso **não funciona**, porque outro pod não verá esses eventos.

-   Solução:

   -   Persistir `pendingEvents` junto com `Payment` no banco.

   -   Quando o evento chega fora de ordem, ele é armazenado no banco.

   -   Qualquer pod que receber eventos para o mesmo Payment consegue **reprocessar corretamente**.


----------

## 4️⃣ Fluxo recomendado com múltiplos pods

1.  Evento `PaymentEvent` chega no Kafka.

2.  Kafka entrega para **um dos pods do microsserviço Payment** (consumer group).

3.  Pod carrega Payment do banco:

   -   Verifica `processedEventIds`.

   -   Aplica ou armazena como `pendingEvent`.

   -   Atualiza Payment com **version**.

4.  Caso o Payment mude para `APPROVED`:

   -   Publica `PaymentApprovedDomainEvent` no Kafka.

5.  Saga escuta o tópico do Kafka:

   -   Configurada como **single consumer group** para a saga.

   -   Atualiza a Order para `PAID`.

6.  Próximo evento do mesmo Payment:

   -   Outro pod pega o Payment, aplica idempotentemente.

   -   Reprocessa pendingEvents persistidos.