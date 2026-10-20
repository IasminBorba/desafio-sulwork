# Decisões de Modelagem e Uso de IA

Este documento registra as decisões tomadas na modelagem do sistema de eventos de alimentação e descreve como uma IA (Claude, da Anthropic) foi usada como apoio durante o processo.

Legenda de origem das decisões:

- **[Autor]**: decisão tomada pelo autor do projeto.
- **[IA → adotada]**: sugestão da IA que o autor avaliou e adotou.
- **[Pendente]**: ainda não definido; precisa ser confirmado antes da implementação.

---

## 1. Visão geral do modelo

O diagrama ER (classes) tem três grupos de entidades, identificados por cor:


| Cor      | Grupo                    | Características                                                |
| -------- | ------------------------ | -------------------------------------------------------------- |
| Roxo     | Entidades de negócio     | Possuem auditoria (`criado_em`, `atualizado_em`) e CRUD        |
| Azul     | Tabelas de ligação (N:N) | Sem auditoria; apenas `criar()` e `excluir()`                  |
| Verde    | Catálogos fixos          | Populados por seed/migration; **não há cadastro pelo usuário** |
| Vermelho | Enums                    | `StatusEvento` e `TipoEvento`                                  |


**Entidades:** `Usuario`, `Evento`, `Participacao`, `Item`, `Contribuicao`, `MetaCategoria`, `Categoria`, `RestricaoAlimentar`, `Alergeno`, `UsuarioRestricao`, `ItemAlergeno`, `RestricaoAlergeno`.

**Convenções:** nomes de classes no singular; toda tabela possui `id` como PK; valores calculados (contribuído, faltante, cobertura) **nunca são armazenados**, sempre calculados na consulta.

---



## 2. Usuários, papéis e login


| Decisão                                                                                                                                                                                     | Origem         |
| ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------- |
| O sistema exige **login** (e-mail + `senha_hash`).                                                                                                                                          | [IA → adotada] |
| Pessoa e Usuário são **a mesma entidade** (`Usuario`): o colaborador é o próprio usuário e se cadastra sozinho (`cadastrar()`).                                                             | [Autor]        |
| **Organizador não é um tipo de usuário**, é um papel em relação a um evento: `Evento.organizador_id` (FK → `Usuario`). A mesma pessoa pode organizar um evento e participar de outro. | [IA → adotada] |
| O papel se chama **organizador** (e não "administrador"), por ser o termo natural para quem monta um evento de comida, de uma confraternização a uma ceia em família. | [Autor] |
| O **CPF** é validado pelos **dígitos verificadores (Módulo 11)** e rejeita sequências com todos os dígitos iguais (ex.: `111.111.111-11`), que passam no cálculo mas não são emitidas pela Receita Federal. Aceita com ou sem máscara e é **armazenado só com os 11 dígitos**. | [Autor] |


A unicidade de CPF e e-mail fica no banco. A validação matemática do CPF fica na aplicação (`CpfUtils` + anotação `@CpfValido` no DTO de cadastro), não na entidade JPA: o banco só garante o formato de 11 dígitos (`CHECK`) e a unicidade.

---



## 3. Eventos: status e transições



### 3.1 Significado dos status


| Status      | Significado                                                                             | Aceita contribuições? |
| ----------- | --------------------------------------------------------------------------------------- | --------------------- |
| `RASCUNHO`  | Organizador ainda está montando o evento (itens, metas). Invisível aos participantes. | Não                   |
| `ABERTO`    | Evento publicado; participantes entram no evento e contribuem.                              | Sim, até o prazo      |
| `ENCERRADO` | Fase de contribuições fechada, mas o evento ainda não aconteceu.                        | Não                   |
| `REALIZADO` | O evento já aconteceu. Estado final.                                                    | Não                   |
| `CANCELADO` | Evento cancelado. Estado final.                                                         | Não                   |




### 3.2 Transições permitidas


| De          | Para        | Ação                                                  |
| ----------- | ----------- | ----------------------------------------------------- |
| `RASCUNHO`  | `ABERTO`    | `publicar()`                                          |
| `RASCUNHO`  | `CANCELADO` | `cancelar()`                                          |
| `ABERTO`    | `ENCERRADO` | `encerrar()` (manual ou automático ao passar o prazo) |
| `ABERTO`    | `CANCELADO` | `cancelar()`                                          |
| `ENCERRADO` | `ABERTO`    | `reabrir()` (exige novo prazo; ver 3.3)               |
| `ENCERRADO` | `REALIZADO` | `concluir()` (após a data/hora do evento)             |
| `ENCERRADO` | `CANCELADO` | `cancelar()`                                          |


Qualquer transição fora desta tabela deve ser rejeitada. `REALIZADO` e `CANCELADO` não têm saída.

Funções do `Evento` no diagrama: `criar`, `editar`, `publicar`, `reabrir`, `concluir`, `encerrar`, `cancelar`. **[Autor]**

### 3.3 Interação entre prazo e status

> Uma contribuição só é permitida se `status == ABERTO` **e** `agora <= prazo_alteracao`.

- O **prazo é a regra de verdade**; o status é reflexo dele.
- Se o prazo passou e o status ainda é `ABERTO`, a validação rejeita a alteração mesmo assim, e o evento transiciona para `ENCERRADO` de forma *lazy* (na próxima consulta), sem depender de agendador.
- Validação no cadastro/edição do evento: `prazo_alteracao <= data_inicio` e `data_fim >= data_inicio`.

**Implementação do encerramento lazy [IA → adotada]:**

- Uma **consulta SQL nativa** encerra, de uma vez, todos os eventos abertos com prazo vencido:
`UPDATE evento SET status = 'ENCERRADO', atualizado_em = :agora WHERE status = 'ABERTO' AND prazo_alteracao < :agora`.
- `:agora` vem do `Clock` da aplicação (fuso America/Recife), e não do `now()` do banco, para que os testes possam fixar e avançar o horário.
- A comparação é `<`: no instante exato do prazo o evento ainda aceita alterações, coerente com `agora <= prazo_alteracao`.
- A consulta é idempotente (o `WHERE status = 'ABERTO'` não mexe no que já foi encerrado) e roda no início das leituras de evento, cobertura e restrições e das operações de participação e contribuição.
- Fica em um serviço próprio (`EncerramentoPorPrazoService`), porque o `@Transactional` do Spring não funciona quando um bean chama um método dele mesmo.
- Um índice parcial em `prazo_alteracao` com `WHERE status = 'ABERTO'` mantém a consulta barata.
- Alternativas descartadas: um job agendado (`@Scheduled`), em que o status pode atrasar até o intervalo do job e várias instâncias exigiriam trava; e um status só calculado na resposta, em que a coluna `status` deixaria de ser confiável para filtros.



### 3.3.1 Reabertura de evento **[Autor]**

`reabrir()` **existe** e só vale para eventos `ENCERRADO`:

1. O novo `prazo_alteracao` é **obrigatório** e **não pode ser anterior ao momento atual**.
2. Se a `data_inicio` do evento **já passou**, é obrigatório informar **novas** `data_inicio` **e** `data_fim`. Sem elas, a reabertura é recusada com a mensagem "o evento já passou da data de início; informe novas datas". A nova `data_inicio` também não pode ser anterior ao momento atual.
3. Se a `data_inicio` ainda não passou, as novas datas são opcionais.
4. Os valores finais respeitam as regras gerais: `prazo_alteracao <= data_inicio` e `data_fim >= data_inicio`.

Motivo: sem estender o prazo, o encerramento lazy fecharia o evento de novo na consulta seguinte; e um evento cuja data já passou só faz sentido reaberto se for remarcado.

### 3.4 Campos do evento

`nome` (String), `descricao` (Text), `tipo` (enum `TipoEvento`), `data_inicio` (DateTime), `data_fim` (DateTime), `prazo_alteracao` (**DateTime**, não Time, pois é um momento e precisa ser comparado com "agora"), `status` (enum `StatusEvento`), `organizador_id` (FK → `Usuario`).

### 3.5 Enum `TipoEvento` **[Autor]**

O autor optou por **enum** em vez de tabela. Valores sugeridos pela IA e adotados:

`CHURRASCO`, `ALMOCO`, `JANTAR`, `CAFE_DA_MANHA`, `LANCHE`, `FEIJOADA`, `PIQUENIQUE`, `FESTA_TEMATICA`, `CONFRATERNIZACAO`, `OUTRO`.

---



## 4. Participação em evento


| Decisão                                                                                                                                       | Origem         |
| --------------------------------------------------------------------------------------------------------------------------------------------- | -------------- |
| Entidade `Participacao` liga `Usuario` a `Evento`.                                                                                               | [IA → adotada] |
| A entidade se chama `Participacao` (a primeira versão do diagrama usava `Inscricao`): a pessoa **participa** do evento, não se inscreve em algo formal. | [Autor] |
| `UNIQUE(usuario_id, evento_id)`: um colaborador participa de vários eventos, mas **uma única vez** em cada.                                   | [IA → adotada] |
| **Sem campo** `status` na participação: `participar()` cria a linha e `cancelar()` a apaga. Consequência assumida: não há histórico de quem saiu. | [Autor]        |


---



## 5. Categorias, itens e metas


| Decisão                                                                                                                                                                                                        | Origem                               |
| -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------ |
| `Item` pertence a um evento (`evento_id`) e a uma categoria (`categoria_id`); campos: `nome`, `quantidade_necessaria`, `obrigatorio` (Bo olean).                                                               | [Autor]                              |
| `UNIQUE(evento_id, categoria_id)` em `MetaCategoria`.                                                                                                                                                          | [IA → adotada]                       |
| **Só é possível cadastrar uma meta se já houver item da mesma categoria cadastrado no evento.** Uma meta de categoria sem item nunca poderia ser atingida, já que as contribuições são sempre feitas em itens. | [Autor] + refinamento [IA → adotada] |
| **Excluir o último item de uma categoria é bloqueado enquanto existir meta para ela**, com mensagem clara.                                                                                                     | [Autor]                              |


---



## 6. Contribuições


| Decisão                                                                                                                                                                                   | Origem         |
| ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------- |
| `Contribuicao` aponta para `Participacao` (garante que a pessoa participa do evento) e para `Item`.                                                                                      | [IA → adotada] |
| A soma das contribuições de um item não pode ultrapassar `quantidade_necessaria`. A validação ocorre **dentro de transação com lock** no item, evitando corrida entre dois participantes. | [IA → adotada] |
| Criar/alterar/remover contribuição só é permitido conforme a regra de prazo × status (seção 3.3).                                                                                         | enunciado      |


---



## 7. Restrições alimentares e alérgenos


| Decisão                                                                                                                                                 | Origem         |
| ------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------- |
| `RestricaoAlimentar` e `Alergeno` são **catálogos fixos** (seed), sem cadastro pelo usuário.                                                            | [Autor]        |
| `UsuarioRestricao` (N:N): um usuário pode ter várias restrições. `UNIQUE(usuario_id, restricao_id)`.                                                    | [Autor]        |
| `ItemAlergeno` (N:N): um item pode ter vários alérgenos. `UNIQUE(item_id, alergeno_id)`.                                                                | [Autor]        |
| `RestricaoAlergeno` (N:N): **uma restrição pode estar ligada a vários alérgenos** (ex.: Vegano → leite, ovo, mel). `UNIQUE(restricao_id, alergeno_id)`. | [IA → adotada] |




### Estratégia de cruzamento

A tabela `RestricaoAlergeno` faz as restrições dos participantes e os alérgenos dos itens **compartilharem o mesmo vocabulário**. Com isso o sistema consegue, por evento:

- avisar que um item **não serve** para N participantes;
- indicar restrições de participantes **sem nenhum item compatível** no evento.



### Vegetarianos e veganos **[IA → adotada]**

Restrições de *origem* (não são alergias) entram no mesmo cruzamento: `Carne` **e** `Mel` **são incluídos no catálogo** `Alergeno`, **sem renomear a tabela**. Exemplos: Vegetariano → Carne, Peixe, Crustáceos; Vegano → Carne, Peixe, Crustáceos, Leite e derivados, Ovo, Mel. Premissa documentada: o catálogo `Alergeno` representa "alérgenos e componentes" que alguém pode precisar evitar.

### Lista do seed **[IA → adotada]**

Base: alergênicos de rotulagem obrigatória da **RDC 26/2015 (Anvisa)**, mais `Carne` e `Mel`.

**`Alergeno`:** Glúten, Leite e derivados, Ovo, Amendoim, Castanhas e nozes, Soja, Peixe, Crustáceos, Gergelim, Carne, Mel.

**`RestricaoAlimentar` → alérgenos:**

| Restrição | Alérgenos que evita |
| --- | --- |
| Doença celíaca / sem glúten | Glúten |
| Intolerância à lactose | Leite e derivados |
| Alergia a leite | Leite e derivados |
| Alergia a ovo | Ovo |
| Alergia a amendoim | Amendoim |
| Alergia a castanhas | Castanhas e nozes |
| Alergia a soja | Soja |
| Alergia a peixe | Peixe |
| Alergia a crustáceos | Crustáceos |
| Alergia a gergelim | Gergelim |
| Vegetariano | Carne, Peixe, Crustáceos |
| Vegano | Carne, Peixe, Crustáceos, Leite e derivados, Ovo, Mel |

**`Categoria`:** Bebidas, Salgados, Doces e sobremesas, Pratos principais, Acompanhamentos, Carnes, Frutas, Pães e frios, Descartáveis e utensílios, Outros.

---



## 8. Cobertura **[Ideia do autor, reformulado pela IA]**

Fórmula sugerida (pesos editáveis; o importante é a coerência e a documentação):

```
cobertura_item         = min(contribuido, necessaria) / necessaria
cobertura_categoria    = min(contribuido_na_categoria, meta) / meta
cobertura_obrigatorios = obrigatórios 100% atendidos / total de obrigatórios

cobertura_geral = 0,5 × média ponderada de cobertura_item (obrigatório peso 2, opcional peso 1)
                + 0,3 × média de cobertura_categoria
                + 0,2 × cobertura_obrigatorios
```

Além do percentual, exibir a lista de **itens obrigatórios ainda sem cobertura**, que é a informação mais acionável para o usuário.

Casos-limite propostos (a confirmar junto com a fórmula):

- Sem metas no evento: o termo das categorias sai e os pesos são renormalizados: `(0,5·A + 0,2·C) / 0,7`.
- Sem itens obrigatórios: o termo dos obrigatórios sai: `(0,5·A + 0,3·B) / 0,8`.
- Sem metas e sem obrigatórios: a cobertura geral é a média ponderada dos itens.
- Evento sem itens: 0%.

---



## 9. Feature livre: contas de colaborador e permissões por evento **[Autor]**

O enunciado não exige autenticação. O login (seção 2) passa a ser a funcionalidade adicional do sistema. A ideia de usar o login como feature livre foi do autor.

- **Problema:** sem identificação, qualquer pessoa poderia editar ou cancelar o evento de outra, alterar a contribuição de um colega ou contribuir em nome dele, e ver eventos ainda em montagem.
- **Como funciona:**
  - cadastro com CPF validado e senha com hash; login com token (JWT);
  - quem cria o evento é o organizador daquele evento;
  - só o organizador edita o evento, os itens e as metas e muda o status;
  - cada pessoa gerencia apenas as próprias participações e contribuições;
  - o `RASCUNHO` fica invisível para os demais;
  - as restrições alimentares vêm do perfil do usuário logado.
- **Plano B:** se o JWT não ficar pronto a tempo, usar HTTP Basic com os mesmos usuários e regras.

Ideias descartadas como feature livre:

- **Alertas de conflito alimentar:** já fazem parte do requisito obrigatório de apresentar restrições e alérgenos de forma útil.
- **Sugestão de itens faltantes:** fica como extra, se sobrar tempo.

---



## 10. Auditoria e unicidade

**Auditoria [Autor]:** `criado_em` e `atualizado_em` em todas as tabelas **roxas**. Tabelas azuis (ligação) e verdes (catálogos) não têm auditoria.

**Restrições de unicidade:**


| Tabela              | Restrição                           |
| ------------------- | ----------------------------------- |
| `Usuario`           | `cpf` único; `email` único          |
| `Participacao`         | `UNIQUE(usuario_id, evento_id)`     |
| `Contribuicao`      | `UNIQUE(participacao_id, item_id)`     |
| `MetaCategoria`     | `UNIQUE(evento_id, categoria_id)`   |
| `UsuarioRestricao`  | `UNIQUE(usuario_id, restricao_id)`  |
| `ItemAlergeno`      | `UNIQUE(item_id, alergeno_id)`      |
| `RestricaoAlergeno` | `UNIQUE(restricao_id, alergeno_id)` |


---

## 11. Painel do organizador **[Autor]**

O organizador tem **uma única tela** que consolida o evento:
- detalhes do evento (status, datas, prazo e se ainda aceita alterações);
- percentuais de cobertura (geral, por categoria e por item);
- **o que cada participante vai levar**, com as contribuições agrupadas por pessoa;
- quem ainda não contribuiu, em destaque;
- itens obrigatórios pendentes e alertas de restrição.

Decisões:
- O painel é exclusivo do organizador.
- A tela só consulta: as alterações continuam nas telas de cada funcionalidade.
- Todos os valores são calculados na consulta (seção 1).

---





