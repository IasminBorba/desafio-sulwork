# Regras de negócio

Este documento consolida as regras de negócio do sistema de eventos de alimentação da Sulwork. Cada regra tem um identificador (`RN-xx`), usado nos [casos de uso](CASOS-DE-USO.md) e nos testes.

**Origem de cada regra:**
- **Enunciado:** exigida pelo desafio.
- **Decisões:** registrada no [`DECISOES.md`](DECISOES.md).
- **Plano:** recomendação do plano de implementação, **ainda a confirmar**. Ao confirmar, registrar no `DECISOES.md`.

---

## 1. Conta e acesso

| ID | Regra | Origem |
|---|---|---|
| RN-01 | O cadastro exige **nome, CPF, e-mail e senha**. As restrições alimentares são opcionais e escolhidas do catálogo. | Enunciado + Decisões |
| RN-02 | O **CPF** precisa ter **11 dígitos** e **dígitos verificadores válidos**. Sequências repetidas (ex.: `111.111.111-11`) são inválidas. Ele é aceito com ou sem máscara e armazenado só com os dígitos. | Enunciado + Plano |
| RN-03 | **CPF e e-mail são únicos** no sistema. | Decisões |
| RN-04 | A senha é armazenada apenas como **hash**, nunca em texto puro. Tamanho mínimo: 8 caracteres. | Decisões (hash) + Plano (tamanho) |
| RN-05 | Pessoa e usuário são a mesma entidade: o **colaborador se cadastra sozinho**. | Decisões |
| RN-06 | CPF e e-mail **não podem ser alterados** depois do cadastro. No perfil, a pessoa edita apenas o nome e as restrições alimentares. | Plano |
| RN-07 | Nas telas e respostas que mostram outras pessoas, o **CPF aparece mascarado** (ex.: `***.456.789-**`). | Plano |
| RN-08 | Todas as ações exigem **login**, exceto cadastro, login, consulta aos catálogos e documentação da API. | Decisões |

## 2. Papéis

| ID | Regra | Origem |
|---|---|---|
| RN-09 | **Organizador não é um tipo de usuário**, é um papel em relação a um evento: quem cria o evento é o organizador dele. A mesma pessoa pode organizar um evento e participar de outro. | Decisões |
| RN-10 | Somente o **organizador do evento** edita os dados do evento, gerencia itens e metas e executa as transições de status. | Decisões |
| RN-11 | Cada pessoa gerencia **apenas as próprias** participações e contribuições. O organizador também pode cancelar a participação de qualquer participante do evento dele. | Decisões + Plano (organizador cancelar) |
| RN-12 | O organizador **pode participar do próprio evento** e contribuir como qualquer participante. | Plano |

## 3. Evento: dados

| ID | Regra | Origem |
|---|---|---|
| RN-13 | Um evento tem **nome, descrição, tipo, data de início, data de fim, prazo para alterações e status**. Todos são obrigatórios. | Enunciado + Decisões |
| RN-14 | O tipo é um valor fixo: `CHURRASCO`, `ALMOCO`, `JANTAR`, `CAFE_DA_MANHA`, `LANCHE`, `FEIJOADA`, `PIQUENIQUE`, `FESTA_TEMATICA`, `CONFRATERNIZACAO`, `OUTRO`. | Decisões |
| RN-15 | Datas: o **prazo para alterações** precisa ser **igual ou anterior à data de início**, e a **data de fim** precisa ser **igual ou posterior à data de início**. | Decisões |
| RN-16 | Todo evento **nasce como `RASCUNHO`**, e quem o criou se torna o organizador. | Decisões |
| RN-17 | Um evento em `RASCUNHO` é **invisível para quem não é o organizador**. Para os demais, ele é tratado como inexistente. | Decisões + Plano (tratar como inexistente) |
| RN-18 | Os dados do evento só podem ser editados em `RASCUNHO` ou `ABERTO`. O evento só pode ser **excluído em `RASCUNHO`**. | Plano |

## 4. Evento: status e transições

**Significado dos status:**

| Status | Significado |
|---|---|
| `RASCUNHO` | O organizador está montando o evento. |
| `ABERTO` | Evento publicado; aceita participações e contribuições até o prazo. |
| `ENCERRADO` | Fase de contribuições fechada; o evento ainda não aconteceu. |
| `REALIZADO` | O evento aconteceu. Estado final. |
| `CANCELADO` | O evento foi cancelado. Estado final. |

**Transições permitidas:**

| De | Para | Ação | Condições |
|---|---|---|---|
| `RASCUNHO` | `ABERTO` | Publicar | RN-20 |
| `RASCUNHO` | `CANCELADO` | Cancelar | — |
| `ABERTO` | `ENCERRADO` | Encerrar | Manual (organizador) ou automática (RN-25) |
| `ABERTO` | `CANCELADO` | Cancelar | — |
| `ENCERRADO` | `ABERTO` | Reabrir | RN-22 |
| `ENCERRADO` | `REALIZADO` | Concluir | RN-21 |
| `ENCERRADO` | `CANCELADO` | Cancelar | — |

| ID | Regra | Origem |
|---|---|---|
| RN-19 | **Qualquer transição fora da tabela acima é rejeitada.** `REALIZADO` e `CANCELADO` não têm saída. | Decisões |
| RN-20 | **Publicar** exige prazo para alterações **no futuro** e **pelo menos um item** cadastrado. | Plano |
| RN-21 | **Concluir** só é permitido **depois da data de início** do evento. | Decisões ("após a data/hora do evento") + Plano (usar a data de início) |
| RN-22 | **Reabrir:**<br>a) o novo prazo para alterações é **obrigatório** e **não pode ser anterior ao momento atual**;<br>b) se a **data de início já passou**, é obrigatório informar **novas datas de início e de fim**, e a nova data de início também não pode ser anterior ao momento atual;<br>c) se a data de início ainda não passou, as novas datas são opcionais;<br>d) os valores finais respeitam a RN-15. | Decisões |

## 5. Prazo para alterações

| ID | Regra | Origem |
|---|---|---|
| RN-23 | **Regra central:** participações, cancelamentos de participação e contribuições (incluir, alterar, remover) só são permitidos quando o evento está **`ABERTO`** **e** o momento atual é **igual ou anterior ao prazo**. | Enunciado + Decisões |
| RN-24 | O **prazo é a regra de verdade**: mesmo que o status ainda esteja `ABERTO`, nenhuma alteração é aceita depois do prazo. | Decisões |
| RN-25 | **Encerramento automático:** eventos `ABERTO` com prazo vencido passam para `ENCERRADO` na próxima consulta ao sistema, sem agendador. No instante exato do prazo, o evento ainda está aberto. | Decisões |
| RN-26 | O prazo **não bloqueia o organizador**: ele pode ajustar itens e metas enquanto o evento estiver em `RASCUNHO` ou `ABERTO`. | Plano |
| RN-27 | O horário de referência das regras de prazo é o de **America/Recife**. | Decisões |

## 6. Categorias e itens

| ID | Regra | Origem |
|---|---|---|
| RN-28 | **Categorias** são um catálogo fixo do sistema; não há cadastro pelo usuário. | Decisões |
| RN-29 | Um **item** pertence a um evento e a uma categoria e tem **nome, quantidade necessária, se é obrigatório ou opcional e seus alérgenos** (zero ou mais, do catálogo). | Enunciado + Decisões |
| RN-30 | A **quantidade necessária** é um número **inteiro maior que zero**. A unidade fica implícita no nome do item (ex.: "Refrigerante 2L"). | Plano |
| RN-31 | Itens são gerenciados apenas pelo organizador, com o evento em `RASCUNHO` ou `ABERTO`. | Decisões (organizador) + Plano (status) |
| RN-32 | A quantidade necessária **não pode ser reduzida abaixo do total já contribuído** para o item. | Plano |
| RN-33 | Um item **com contribuições não pode ser excluído**. | Plano |
| RN-34 | **O último item de uma categoria não pode ser excluído enquanto existir meta para essa categoria**, com mensagem clara. Pelo mesmo motivo, ele também não pode ser movido para outra categoria. | Decisões + Plano (mover de categoria) |

## 7. Metas por categoria

| ID | Regra | Origem |
|---|---|---|
| RN-35 | Cada evento pode ter **no máximo uma meta por categoria**. A meta é uma quantidade **inteira maior que zero**. | Decisões |
| RN-36 | **Só é possível criar uma meta se já existir item da mesma categoria** no evento. | Decisões |
| RN-37 | A meta é um valor **independente**, definido pelo organizador: não é derivada da soma dos itens nem dividida entre eles. Ela representa o total mínimo de unidades contribuídas na categoria. | Plano |
| RN-38 | Metas são gerenciadas apenas pelo organizador, com o evento em `RASCUNHO` ou `ABERTO`. | Decisões (organizador) + Plano (status) |

## 8. Participações

| ID | Regra | Origem |
|---|---|---|
| RN-39 | Um colaborador pode participar de vários eventos, mas **só uma vez em cada evento**. | Enunciado + Decisões |
| RN-40 | A participação segue a regra de prazo e status (RN-23). | Enunciado + Decisões |
| RN-41 | A participação **não tem status**: participar cria o registro e cancelar o apaga. Não há histórico de quem saiu. | Decisões |
| RN-42 | Ao cancelar uma participação, **as contribuições da pessoa nesse evento são removidas junto**, e os itens voltam a aparecer como faltantes. | Plano |
| RN-43 | A lista de participantes é visível para os usuários logados que podem ver o evento, com as restrições alimentares de cada um e o CPF mascarado (RN-07). | Plano |

## 9. Contribuições

| ID | Regra | Origem |
|---|---|---|
| RN-44 | Uma contribuição associa **participante (via participação), item e quantidade**. A quantidade é um **inteiro maior que zero**. | Enunciado + Decisões |
| RN-45 | Só pode contribuir quem **participa do evento**, e apenas com **itens do mesmo evento**. | Decisões (participação) + Plano (mesmo evento) |
| RN-46 | Cada pessoa tem **no máximo uma contribuição por item**. Para mudar, ela altera a quantidade. | Decisões |
| RN-47 | Várias pessoas podem contribuir com o mesmo item, mas **a soma das contribuições nunca pode ultrapassar a quantidade necessária**. A verificação é feita com bloqueio do item, para que duas pessoas contribuindo ao mesmo tempo não ultrapassem o limite. | Enunciado + Decisões |
| RN-48 | Incluir, alterar e remover contribuições segue a regra de prazo e status (RN-23). Após o prazo ou o encerramento, nenhuma alteração é permitida. | Enunciado + Decisões |
| RN-49 | Somente o **dono da contribuição** pode alterá-la ou removê-la. | Decisões |

## 10. Restrições alimentares e alérgenos

| ID | Regra | Origem |
|---|---|---|
| RN-50 | **Restrições alimentares** e **alérgenos** são catálogos fixos do sistema; não há cadastro pelo usuário. | Decisões |
| RN-51 | Cada restrição está ligada a um ou mais alérgenos que a pessoa precisa evitar (ex.: Intolerância à lactose → Leite e derivados). | Decisões |
| RN-52 | **Vegetarianos e veganos** usam o mesmo mecanismo: o catálogo de alérgenos inclui **Carne** e **Mel**. Exemplos: Vegetariano → Carne, Peixe, Crustáceos; Vegano → Carne, Peixe, Crustáceos, Leite e derivados, Ovo, Mel. | Decisões |
| RN-53 | Há **conflito** quando o item contém algum alérgeno ligado a alguma restrição de um participante. | Decisões |
| RN-54 | Para cada evento, o sistema mostra:<br>a) quais **itens não servem para quais participantes** (e quantos);<br>b) quais **restrições dos participantes não têm nenhum item compatível**;<br>c) um resumo de quantos participantes têm cada restrição. | Decisões (a, b) + Plano (c) |
| RN-55 | O alerta de conflito **não impede** a contribuição: o item pode ser necessário para outras pessoas. Ele aparece como aviso no momento de contribuir. | Plano |

## 11. Cobertura

| ID | Regra | Origem |
|---|---|---|
| RN-56 | Valores derivados (total contribuído, faltante, coberturas) **nunca são armazenados**; são sempre calculados na consulta. | Decisões |
| RN-57 | **Fórmula:**<br>`cobertura_item = min(contribuído, necessária) / necessária`<br>`A` = média ponderada de `cobertura_item` (obrigatório peso 2, opcional peso 1)<br>`B` = média de `cobertura_categoria`, com `cobertura_categoria = min(contribuído na categoria, meta) / meta`, só para categorias com meta<br>`C` = itens obrigatórios 100% atendidos / total de obrigatórios<br>`cobertura_geral = 0,5·A + 0,3·B + 0,2·C` | Decisões |
| RN-58 | **Casos-limite:**<br>sem metas → `(0,5·A + 0,2·C) / 0,7`;<br>sem obrigatórios → `(0,5·A + 0,3·B) / 0,8`;<br>sem metas e sem obrigatórios → `A`;<br>evento sem itens → 0%. | Decisões (a confirmar) |
| RN-59 | Além do percentual, o sistema mostra a **lista de itens obrigatórios ainda sem cobertura completa** e, por item e por categoria, o necessário, o contribuído e o faltante. | Decisões + Plano (detalhamento) |

## 12. Auditoria

| ID | Regra | Origem |
|---|---|---|
| RN-60 | Usuário, evento, item, meta e contribuição registram **data de criação e de última atualização**. Tabelas de ligação e catálogos não têm auditoria. | Decisões |

## 13. Painel do organizador

| ID | Regra | Origem |
|---|---|---|
| RN-61 | O **painel do evento** (detalhes, percentuais e o que cada participante vai levar) é acessível **somente ao organizador** do evento, em qualquer status. | Decisões |
| RN-62 | No painel, as contribuições aparecem **agrupadas por participante**, e quem ainda não contribuiu com nenhum item aparece **destacado**. Todos os valores são calculados na consulta (RN-56). | Decisões |

---

## Como as violações são respondidas

| Situação | Resposta da API |
|---|---|
| Dado ausente ou em formato inválido (ex.: CPF inválido, quantidade zero) | **400**, com a lista dos campos inválidos |
| Sem login ou sessão expirada | **401** |
| Logado, mas sem permissão (ex.: não é o organizador) | **403** |
| Recurso inexistente ou invisível para a pessoa (ex.: rascunho de outro usuário) | **404** |
| Conflito com o estado atual: transição não permitida, prazo vencido, evento fora de `ABERTO`, duplicidade, soma acima do necessário, exclusões bloqueadas | **409** |
| Dados válidos, mas a regra de negócio impede (ex.: não participante, item de outro evento, meta sem item da categoria, reabertura sem as novas datas) | **422** |

Todas as respostas de erro trazem uma mensagem em português explicando o motivo, por exemplo: "Restam 3 unidades para este item".
