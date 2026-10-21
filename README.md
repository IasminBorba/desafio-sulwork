# Desafio Sulwork: organização de eventos de alimentação

Aplicação para organizar eventos de alimentação, como cafés da manhã, almoços e confraternizações da empresa, ou uma ceia em família. Os participantes combinam **o que cada um vai levar** e acompanham **o que ainda falta**.

Repositório: [github.com/IasminBorba/desafio-sulwork](https://github.com/IasminBorba/desafio-sulwork)

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

## Funcionalidades

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

## Aplicação publicada

| Peça | URL |
|---|---|
| Frontend | https://desafio-sulwork-omega.vercel.app |
| API | https://desafio-sulwork.onrender.com/api/v1 |
| Swagger | https://desafio-sulwork.onrender.com/docs |

No plano gratuito o backend no Render dorme depois de alguns minutos sem acesso. A primeira chamada pode levar cerca de um minuto.

### Contas de demonstração

A migration `V3` cria três contas com a senha `senha123`:

| Nome | E-mail | Papel no exemplo |
|---|---|---|
| Ana Souza | ana.souza@sulwork.demo | organiza o churrasco aberto e um rascunho |
| Bia Lima | bia.lima@sulwork.demo | participa do churrasco (celíaca) |
| Carlos Dias | carlos.dias@sulwork.demo | organiza um almoço já encerrado (vegano) |

No Swagger, use `POST /api/v1/auth/login`, copie o `token` da resposta e clique em **Authorize**. Cole só o token (sem a palavra `Bearer`).

## Tecnologias

| Camada | Tecnologia | Versão |
|---|---|---|
| Backend | Java (Temurin), Spring Boot (Web MVC, Data JPA, Security JWT, Validation, Actuator) | Java 21, Boot 4.1.1 |
| Banco | PostgreSQL + Flyway | 17 |
| API docs | springdoc-openapi | 3.1.1 |
| Testes (back) | JUnit 5, Mockito, MockMvc, Testcontainers, JaCoCo | — |
| Frontend | React, Vite, TypeScript, React Router | React 19, Vite 8 |
| Testes (front) | Vitest, Testing Library, jsdom | — |
| Infra | Docker e Docker Compose | — |

## Arquitetura

Monorepo com API REST em `backend/`, SPA em `frontend/` e `docker-compose.yml` para subir os três serviços.

O backend é organizado **por funcionalidade** (`auth`, `evento`, `item`, `meta`, `participacao`, `contribuicao`, `cobertura`, `restricao`, `painel`). Cada pacote reúne controller, service, repository, entidade e DTOs, no estilo de uma “app” do Django. O domínio fica em português (`Evento`, `Participacao`); os sufixos técnicos em inglês (`Controller`, `Service`).

Padrões usados:

| Padrão | Onde |
|---|---|
| Service Layer + Repository | controller → service → repository |
| DTO + Mapper | `record` na API; entidades nunca saem na resposta |
| State | `StatusEvento` e os métodos `publicar()`, `encerrar()`, `reabrir()`, `concluir()`, `cancelar()` |
| Strategy | `CalculadoraCobertura` |
| Auditoria | `EntidadeAuditavel` (`criado_em` / `atualizado_em`) nas entidades de negócio |
| Exception handler | `ProblemDetail` (RFC 9457) |

Consultas **SQL nativas** calculam a cobertura e encerram automaticamente os eventos com prazo vencido (`EncerramentoPorPrazoService`). JPA com `open-in-view: false` e `ddl-auto: validate`; o schema vem só do Flyway.

O frontend é uma SPA (React + Vite). Em produção na Vercel ela chama a API no Render. No Docker Compose, o nginx do front encaminha `/api` para o backend, então o browser usa a mesma origem.

## Como executar

Requisitos: Docker (para o compose ou só o banco), Java 21 e Node 22 se for rodar fora do container.

Copie `.env.example` para `.env` se quiser mudar usuário, senha ou JWT. **Não commite o `.env`.**

### Com Docker (frontend, backend e banco)

Na raiz do repositório:

```bash
docker compose up --build
```

| Serviço | URL |
|---|---|
| Frontend | http://localhost |
| API | http://localhost:8080/api/v1 |
| Swagger | http://localhost:8080/docs |
| Postgres | localhost:5433 |

O primeiro `--build` baixa imagens e compila Maven + npm; demora vários minutos. Para encerrar: `docker compose down`. O volume do banco persiste; `docker compose down -v` apaga os dados.

### Sem Docker (API e front na máquina, banco no Compose)

1. Suba só o Postgres:

```bash
docker compose up -d db
```

2. Backend, na pasta `backend/` (Java 21 no `JAVA_HOME`):

```bash
./mvnw spring-boot:run
```

No Windows: `.\mvnw.cmd spring-boot:run`. A API usa `jdbc:postgresql://localhost:5433/eventos` (usuário/senha `eventos`).

3. Frontend, na pasta `frontend/`:

```bash
npm install
npm run dev
```

O Vite sobe em http://localhost:5173 e chama `http://localhost:8080/api/v1`.

## Testes

Backend (unitários e de integração com Testcontainers; precisa de Docker):

```bash
cd backend
./mvnw test
```

O Surefire inclui `*Test`, `*Tests` e `*IT`. O relatório do JaCoCo fica em `backend/target/site/jacoco/index.html` depois da execução.

Frontend:

```bash
cd frontend
npm test
```

## Documentação

| Documento | Conteúdo |
|---|---|
| [Decisões de modelagem](docs/DECISOES.md) | Decisões sobre as regras abertas do desafio, com a origem de cada uma |
| [Regras de negócio](docs/REGRAS-DE-NEGOCIO.md) | Regras numeradas (`RN-xx`), origem e respostas da API |
| [Casos de uso](docs/CASOS-DE-USO.md) | Atores, fluxos principais, alternativos e exceções |
| [Uso de IA](docs/USO-DE-IA.md) | Ferramentas, etapas, sugestões aceitas e alteradas, julgamento próprio |
| [Diagrama ER](docs/diagrama.jpg) | Modelo de dados |
