# Desafio Sulwork: organização de eventos de alimentação

Aplicação para organizar eventos de alimentação, como cafés da manhã, almoços e confraternizações da empresa, ou uma ceia em família. Os participantes combinam **o que cada um vai levar** e acompanham **o que ainda falta**.

## O problema

Em todo evento em que cada um leva alguma coisa, as mesmas perguntas aparecem:
- Quem vai levar o quê?
- Quanto já está garantido?
- Vai sobrar refrigerante e faltar sobremesa?
- Tem alguma opção para quem é celíaco, intolerante à lactose ou vegano?

Sem uma lista compartilhada, a combinação fica espalhada em mensagens, e o organizador só descobre o que faltou no dia.

## A solução

- O **organizador** cria o evento, define os itens necessários por categoria (com quantidade, obrigatoriedade e alérgenos), as metas por categoria e um **prazo** para as combinações.
- Os **participantes** entram no evento e registram o que vão levar. Várias pessoas podem dividir um item, mas a soma nunca passa do necessário.
- O sistema mostra em tempo real a **cobertura** do evento (quanto das necessidades já foi atendido) e os **conflitos entre as restrições alimentares dos participantes e os alérgenos dos itens**.
- O organizador tem um **painel** com os detalhes do evento, os percentuais e **o que cada participante vai levar**.
- Depois do prazo, ou com o evento encerrado, nenhuma alteração é aceita.

## Funcionalidades previstas

- **Contas de colaborador e permissões por evento** (funcionalidade livre):
  - cadastro com CPF validado e login;
  - quem cria o evento é o organizador dele;
  - cada pessoa gerencia apenas as próprias participações e contribuições.
- **Eventos com ciclo de vida:** `RASCUNHO` → `ABERTO` → `ENCERRADO` → `REALIZADO`, além de `CANCELADO`. As ações permitidas dependem do status, e o encerramento acontece automaticamente quando o prazo vence.
- **Itens e metas por categoria**, com itens obrigatórios ou opcionais e seus alérgenos.
- **Contribuições** com limite pela quantidade necessária e controle de concorrência.
- **Cobertura do evento:** percentual geral, por categoria e por item, e lista dos obrigatórios pendentes.
- **Restrições alimentares x alérgenos:** itens que não atendem algum participante e restrições sem nenhum item compatível.
- **Painel do organizador:** visão consolidada do evento e das contribuições por participante.

## Tecnologias

| Camada | Tecnologia |
|---|---|
| Backend | Java 21, Spring Boot 4.1, Spring Data JPA/Hibernate, Spring Security (JWT), Bean Validation |
| Banco de dados | PostgreSQL 17, com migrations versionadas pelo Flyway |
| Documentação da API | OpenAPI / Swagger (springdoc-openapi) |
| Testes | JUnit 5, Mockito, MockMvc, Testcontainers, JaCoCo |
| Frontend | React, Vite, TypeScript; testes com Vitest e Testing Library |
| Infraestrutura | Docker e Docker Compose |

## Arquitetura (planejada)

- **Monorepo:** `backend/` (API REST), `frontend/` (SPA) e `docker-compose.yml` para subir frontend, backend e banco.
- **Backend organizado por funcionalidade** (`evento`, `item`, `meta`, `participacao`, `contribuicao`, `cobertura`, ...). Cada pacote reúne controller, service, repository, entidade e DTOs.
- **Padrões aplicados:**
  - Service Layer, Repository, DTO e Mapper;
  - **State** para as transições de status do evento;
  - **Strategy** para o cálculo da cobertura;
  - tratamento global de erros no formato `ProblemDetail`.
- **Consultas SQL nativas** para o cálculo da cobertura e para o encerramento automático dos eventos com prazo vencido.

## Documentação

| Documento | Conteúdo |
|---|---|
| [Decisões de modelagem](docs/DECISOES.md) | Decisões tomadas sobre as regras abertas do desafio, com a origem de cada uma (autora ou sugestão da IA adotada) |
| [Regras de negócio](docs/REGRAS-DE-NEGOCIO.md) | Regras numeradas (`RN-xx`), com origem e respostas da API para cada violação |
| [Casos de uso](docs/CASOS-DE-USO.md) | Atores, fluxos principais, fluxos alternativos e exceções |
| [Uso de IA](docs/USO-DE-IA.md) | Como a IA foi usada em cada etapa, sugestões aceitas e alteradas, e onde houve julgamento próprio |
| [Diagrama ER](docs/diagrama.jpg) | Modelo de dados |

## Testes do backend

Na pasta `backend/`:

```bash
./mvnw test
```

O Surefire inclui as classes `*Test`, `*Tests` e `*IT`. O relatório do JaCoCo fica em `backend/target/site/jacoco/index.html` depois da execução.

## Dados de demonstração

A migration `V3` cria três contas com a senha `senha123`:

| Nome | E-mail | Papel no exemplo |
|---|---|---|
| Ana Souza | ana.souza@sulwork.demo | organiza o churrasco aberto e um rascunho |
| Bia Lima | bia.lima@sulwork.demo | participa do churrasco (celíaca) |
| Carlos Dias | carlos.dias@sulwork.demo | organiza um almoço já encerrado (vegano) |

## Aplicação publicada

| Peça | URL |
|---|---|
| Frontend | https://desafio-sulwork-omega.vercel.app |
| API | https://desafio-sulwork.onrender.com/api/v1 |
| Swagger | https://desafio-sulwork.onrender.com/docs |

Contas de demonstração: `ana.souza@sulwork.demo`, `bia.lima@sulwork.demo` e `carlos.dias@sulwork.demo`, senha `senha123`.

No plano gratuito o backend no Render dorme depois de alguns minutos sem acesso. A primeira chamada pode levar cerca de um minuto.

## Próximas seções

Conforme o desenvolvimento avançar, este README vai incluir:
- instruções de execução (com e sem Docker);
- cálculo da cobertura com um exemplo numérico;
- percentual de cobertura de testes medido pelo JaCoCo.
