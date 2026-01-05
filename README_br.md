# eventdrivenarchitecture

Projeto multi-módulo demonstrando uma arquitetura orientada a eventos (event-driven) em Java/Spring Boot.
Os módulos são organizados como bibliotecas internas que, no futuro, podem ser transformadas em repositórios independentes:

- `lib-domain` — Definições de domínio e contratos (modelos de eventos, ports, interfaces de strategy).
- `lib-integration` — Integrações e adaptação de infra (ex.: JPA, camada de persistência, db em memória para testes).
- `lib-router` — Roteador/selector de estratégias para encaminhar eventos para as strategies apropriadas.
- `backend` — Aplicação Spring Boot que usa as libs acima para demonstrar processamento de eventos (cenários de teste).

---

Sumário

- Visão geral
- Módulos e responsabilidades
- Como construir
- Como executar (desenvolvimento / produção)
- Principais classes e fluxos
- Como contribuir / próximos passos

Visão geral

Este projeto é um exemplo didático de como estruturar uma aplicação event-driven em módulos separados. A ideia é manter contratos e modelos de domínio (lib-domain) desacoplados das implementações de infraestrutura e roteamento, permitindo que cada parte seja evoluída ou substituída independentemente.

Módulos

1. lib-domain
   - Conteúdo: modelos genéricos (ex: `Event`), interfaces/ports (ex: `RepositoryPort`) e contratos de estratégia (ex: `EventStrategy`).
   - Propósito: definir o núcleo do domínio sem dependências de infra.
   - Local: `lib-domain/src/main/java/br/com/libdomain`

2. lib-integration
   - Conteúdo: integrações e utilitários de infra que dependem de `lib-domain`.
   - Dependências: `lib-domain` e Spring Boot starter (para utilidades de infra).
   - Observação: inclui dependências de JPA e H2 (somente para testes/emulação).
   - Local: `lib-integration`

3. lib-router
   - Conteúdo: router de eventos (`EventRouter`), interface `StrategySelector` e implementação `DefaultStrategySelector`.
   - Propósito: localizar e executar as `EventStrategy` apropriadas para cada `Event`.
   - Local: `lib-router`

4. backend
   - Conteúdo: aplicação Spring Boot que demonstra uso das bibliotecas.
   - Comportamento demonstrativo: `StartupRunner` roda cenários de teste simulando concorrência, replay de eventos, eventos fora de ordem e conflitos de versão.
   - Observações:
     - A aplicação escaneia pacotes de `lib-integration` e `libdomain.strategy` para carregar estratégias e integrações via Spring (veja `Application.java` e `IntegrationAutoConfig`).
     - Usa um repositório em memória (`PaymentRepositoryInMemory`) para demonstrar controle de versão otimista e idempotência.
   - Local: `backend`

Requisitos

- JDK 21 (configurado no POM pai)
- Maven (projeto multi-módulo)
- (Opcional) Docker, se quiser experimentar integrações externas

Build

Construção do projeto inteiro (a partir da raiz do repo):

```powershell
mvn -T 1C clean install
```

Construir apenas o módulo `backend` (útil durante desenvolvimento):

```powershell
mvn -pl backend -am clean package
```

Executando a aplicação (desenvolvimento)

Executar o backend via Spring Boot (a aplicação roda cenários de demonstração na inicialização):

```powershell
mvn -pl backend spring-boot:run
```

Ou executar o jar gerado (após `mvn package`):

```powershell
java -jar backend/target/backend-1.0.0-SNAPSHOT.jar
```

O `StartupRunner` dentro do `backend` irá imprimir no console uma sequência de cenários demonstrando:
- Concorrência real (mesmo evento sendo processado por duas threads)
- Reaplicação idempotente do mesmo evento
- Eventos aplicados fora de ordem
- Eventos diferentes concorrentes causando possíveis conflitos de versão otimista

Principais classes e fluxo

- `br.com.libdomain.model.Event` (lib-domain): estrutura imutável do evento (UUID, tipo, payload).
- `br.com.libdomain.strategy.EventStrategy` (lib-domain): contrato para estratégias que processam eventos.
- `br.com.libdomain.router.EventRouter` (lib-router): roteia eventos para estratégias selecionadas.
- `br.com.backend.Application` (backend): configuração principal do Spring Boot — observe `scanBasePackages` para incluir pacotes das libs.
- `br.com.backend.config.RoutingConfig` (backend): monta `StrategySelector` e `EventRouter` com beans Spring.
- `br.com.backend.application.usecases.ProcessPaymentEventUseCase` (backend): aplica eventos de pagamento de forma idempotente.
- `br.com.backend.adapters.out.PaymentRepositoryInMemory` (backend): repositório em memória que simula controle otimista de versão.
- `br.com.backend.adapters.in.StartupRunner` (backend): executa cenários de demo na inicialização.

Observações técnicas importantes

- A idempotência é tratada no agreggate `Payment` (marca eventos processados) e também há um controle simplificado de versão (simulado pelo `version` e pelo `PaymentRepositoryInMemory`) para demonstrar conflitos otimistas.
- O roteador `EventRouter` exige que exista ao menos uma `EventStrategy` que suporte o evento; caso contrário lança erro. Em produção, você pode querer mudar esse comportamento para um fallback silencioso ou fila de dead-letter.

Separação futura em múltiplos repositórios

O monorepo atual mantém os 4 módulos juntos para facilitar desenvolvimento. Quando migrar para repositórios separados, recomenda-se:

- Transformar cada módulo (`lib-domain`, `lib-integration`, `lib-router`) em um repositório Git próprio com versionamento semântico.
- Publicar os artefatos em um repositório Maven (Nexus/Artifactory) ou usar Git submodules/subtrees conforme sua estratégia.
- No `backend`, atualizar dependências para apontar para versões publicadas das libs.

Contribuindo

1. Crie uma branch com o nome do recurso/fix: `feature/descrição` ou `fix/descrição`.
2. Faça commits pequenos e com mensagens claras.
3. Abra PR para revisão.

Licença

Este repositório é um exemplo e não contém uma licença explícita (adicione uma LICENSE se for compartilhá-lo publicamente).

Contato / Dúvidas

Se precisar que eu gere um README em inglês, exemplos de testes unitários, ou que eu prepare scripts para CI (GitHub Actions) para publicar módulos em um registry Maven, diga qual tarefa você prefere em seguida.
