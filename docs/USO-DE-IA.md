# Uso de IA no desenvolvimento

Este documento descreve como a inteligência artificial foi usada na criação do projeto, seguindo as orientações do desafio:
- quais ferramentas foram usadas;
- em quais etapas elas ajudaram;
- uma sugestão aceita e uma alterada ou rejeitada;
- onde foi necessário mais julgamento próprio.

Em todas as etapas, a IA foi usada como **apoio**: as decisões finais, a revisão e a responsabilidade pelo código são minhas.

---

## Ferramentas utilizadas

| Ferramenta | Uso principal |
|---|---|
| **Claude (Anthropic)**, em conversa de chat | Validação do diagrama ER, discussão das decisões de modelagem e geração do documento de decisões |
| **Cursor** (agente de IA no editor) | Plano de implementação, pesquisa de versões da stack, análise de cada etapa, implementação da API e geração do frontend |

---

## Como a IA foi usada, etapa por etapa

### 1. Leitura do desafio (sem IA)

Comecei lendo todas as regras do desafio, os requisitos funcionais e técnicos e os critérios de entrega e avaliação. O objetivo era entender o domínio (eventos de alimentação, participantes, contribuições, prazo e cobertura) antes de envolver qualquer ferramenta, para conseguir avaliar com senso crítico o que a IA viesse a sugerir.

### 2. Diagrama ER e validação com a IA (Claude)

Com base nos requisitos, **desenhei o diagrama ER**: entidades, campos, relacionamentos e cardinalidades, separados por cores (entidades de negócio, tabelas de ligação, catálogos e enums). Depois, pedi à IA que **validasse o diagrama contra os requisitos**.

O diagrama passou por 2 versões. Em cada uma, a IA apontou possíveis inconsistências, como:
- restrições de unicidade ausentes;
- campos de auditoria faltando.

**O diagrama foi desenhado por mim; a IA apenas revisou.** Nem toda observação estava certa:
- **Cardinalidades:** em uma revisão, a IA disse que elas estavam "trocadas" quando o diagrama já estava correto. Questionei, e a IA corrigiu a própria análise.
- **Versão desatualizada:** em outra revisão, a IA comentou um campo que eu já tinha removido (o `status` da participação), porque estava olhando uma versão anterior da imagem.

Esses casos reforçaram a necessidade de conferir cada apontamento antes de aplicar.

### 3. Discussão das decisões e documentação (Claude)

O desafio deixa várias regras abertas de propósito. Discuti com a IA os pontos em que havia mais de um caminho possível, por exemplo:
- o significado de cada status e as transições entre eles;
- a interação entre o prazo e o status do evento;
- a separação entre participante e organizador;
- como relacionar restrições alimentares e alérgenos;
- a relação entre a meta por categoria e a quantidade de cada item.

A IA apresentava opções e prós e contras. Eu decidia: aceitava, ajustava ou recusava. Ao final, pedi à IA que **gerasse um documento em Markdown a partir do histórico da conversa e das decisões tomadas**, o [`DECISOES.md`](DECISOES.md). Cada decisão ali está marcada com a origem: **[Autor]** quando foi minha e **[IA → adotada]** quando foi uma sugestão da IA que avaliei e adotei. Revisei o documento antes de considerá-lo final.

### 4. Casos de uso

Com as decisões consolidadas, pedi à IA (Cursor) que gerasse, a partir do `DECISOES.md` e do enunciado, os documentos de [casos de uso](CASOS-DE-USO.md) e de [regras de negócio](REGRAS-DE-NEGOCIO.md). Eles cobrem:
- cadastro e login;
- criação e organização de eventos;
- participação;
- contribuições;
- acompanhamento da cobertura e das restrições.

Cada regra indica a origem: enunciado, `DECISOES.md` ou recomendação ainda a confirmar. Assim, ficou claro o que eu já tinha decidido e o que a IA estava propondo. Revisei os dois documentos e confirmei ou ajustei as recomendações antes de iniciar a implementação. Os casos de uso serviram de base para os endpoints, as telas e os testes.

### 5. Plano de implementação e análise de cada ponto (Cursor)

Ao iniciar a implementação, pedi à IA um **plano de implementação**, contendo:
- mapeamento do que é obrigatório e do que é diferencial no desafio;
- escolha da stack e das versões (a IA pesquisou as versões atuais do Spring Boot e do springdoc-openapi e a compatibilidade entre elas);
- padrões de projeto;
- estratégia de testes;

O plano não foi aplicado de uma vez. **Analisei cada ponto, fiz ajustes e pedi revisões.** Alguns exemplos:
- Pedi que o plano levasse em conta o `DECISOES.md`, que passou a ser a referência da modelagem.
- Defini as regras de reabertura do evento e o uso do login como funcionalidade livre (detalhes nas seções a seguir).
- Para aprender Java/Spring, pedi que o guia usasse o **Django**, que eu já conheço, como analogia (apps → pacotes por funcionalidade, models → entidades, settings → `application.yml`).

### 6. Implementação e commits

Com o plano definido, implementei o projeto em partes, com o Cursor gerando o código da API e, em seguida, o **frontend (SPA React) a partir dos casos de uso e da API**. A cada parte, li o que foi gerado, ajustei o que não batia com as regras e rodei os testes. Só então **gerei o commit** no padrão Conventional Commits e enviei para o repositório. O objetivo era que o histórico mostrasse a evolução do projeto em passos pequenos, cada um focado em uma funcionalidade ou mudança.

---

## Sugestão aceita

**Encerramento automático do evento quando o prazo vence ("lazy"), feito com uma consulta SQL nativa.**

O prazo é a regra que vale: depois dele, nenhuma alteração é aceita, qualquer que seja o status gravado. Para o status refletir isso, a IA sugeriu uma consulta SQL nativa que muda para `ENCERRADO` todos os eventos `ABERTO` com prazo vencido, executada no início das consultas. O horário vem de um `Clock` da aplicação, e não do relógio do banco, o que permite testar o comportamento fixando e avançando o horário.

Aceitei porque:
- dispensa um agendador;
- a consulta pode rodar várias vezes com o mesmo resultado;
- pode ser testada de forma previsível.

Também aceitei a ideia da tabela `RestricaoAlergeno`, que coloca as restrições alimentares e os alérgenos no mesmo vocabulário e permite o cruzamento automático entre eles.

## Sugestão alterada ou rejeitada

**Alterada: regras de reabertura do evento.** A IA sugeriu apenas exigir um novo prazo no futuro para reabrir um evento encerrado. Ampliei a regra: o novo prazo é obrigatório e não pode ser anterior ao momento atual; e, se a data de início do evento já passou, também é preciso informar **novas datas de início e fim**. Um evento cuja data já passou só faz sentido ser reaberto se for remarcado.

**Rejeitada: renomear a tabela `Alergeno`.** Para tratar vegetarianos e veganos (restrições de origem, não alergias), a IA sugeriu incluir componentes como carne no catálogo e, opcionalmente, renomear a tabela para algo como `Componente`. Aceitei incluir `Carne` e `Mel` no catálogo, mas **mantive o nome `Alergeno`**, documentando a premissa.

**Alterada: escolha da funcionalidade livre.** A IA sugeriu "sugestões personalizadas de contribuição". Decidi usar o **login com permissões por evento** como funcionalidade livre, porque ele resolve um problema real do sistema: sem identificação, qualquer pessoa poderia alterar o evento ou a contribuição de outra. As sugestões ficaram de fora desta entrega.

Outras decisões em que divergi da IA ou decidi por conta própria estão no `DECISOES.md`, por exemplo:
- Pessoa e Usuário como uma única entidade;
- participação sem campo de status;
- `TipoEvento` como enum.

## Onde foi necessário mais julgamento próprio

- **Interação entre prazo e status:** definir qual dos dois é a "regra de verdade" e como mantê-los coerentes sem criar comportamentos inesperados.
- **Reabertura de eventos:** equilibrar flexibilidade para o organizador e consistência das datas.
- **Fórmula de cobertura:** como pesar itens obrigatórios e opcionais, metas por categoria e casos-limite (evento sem metas ou sem itens obrigatórios).
- **Restrições x alérgenos:** como tratar restrições que não são alergias (vegetariano, vegano) sem complicar o modelo.
- **Validação do que a IA apontava:** a IA cometeu erros (cardinalidades, versão desatualizada do diagrama, interpretação sobre o Django), então cada sugestão precisou ser conferida contra os requisitos e o meu próprio entendimento antes de ser aplicada.

---

## Divisão de responsabilidades

- **Feito por mim:**
  - leitura e interpretação do desafio;
  - desenho do diagrama ER;
  - decisões finais de modelagem e de regras de negócio;
  - revisão e validação dos casos de uso e das regras de negócio;
  - revisão de todo o conteúdo gerado;
  - organização dos commits.
- **Apoio da IA:**
  - revisão do diagrama;
  - apresentação de alternativas e trade-offs;
  - consolidação das decisões em documento;
  - geração dos documentos de casos de uso e de regras de negócio a partir das decisões;
  - plano de implementação;
  - pesquisa de versões;
  - implementação da API e geração do frontend, sempre com revisão minha antes do commit.
   
- Todo código e documento gerado com ajuda da IA foi lido, entendido e ajustado por mim antes de entrar no repositório. Sou capaz de explicar cada decisão tomada.
