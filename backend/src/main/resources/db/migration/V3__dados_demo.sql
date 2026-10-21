-- Dados de demonstração. Senha de todos: senha123
-- CPFs e e-mails diferentes dos usados nos testes automatizados.

INSERT INTO usuario (nome, cpf, email, senha_hash) VALUES
    ('Ana Souza', '71428793860', 'ana.souza@sulwork.demo',
     '$2a$10$hk7194SbLLOCCx1GaAfERe8tQ4KCqQkyEB4wFUZ5XA/BMVRssGcLO'),
    ('Bia Lima', '24843803480', 'bia.lima@sulwork.demo',
     '$2a$10$hk7194SbLLOCCx1GaAfERe8tQ4KCqQkyEB4wFUZ5XA/BMVRssGcLO'),
    ('Carlos Dias', '39053344705', 'carlos.dias@sulwork.demo',
     '$2a$10$hk7194SbLLOCCx1GaAfERe8tQ4KCqQkyEB4wFUZ5XA/BMVRssGcLO');

INSERT INTO usuario_restricao (usuario_id, restricao_id)
SELECT u.id, r.id
  FROM usuario u
  JOIN restricao_alimentar r ON r.nome = 'Doença celíaca / sem glúten'
 WHERE u.email = 'bia.lima@sulwork.demo';

INSERT INTO usuario_restricao (usuario_id, restricao_id)
SELECT u.id, r.id
  FROM usuario u
  JOIN restricao_alimentar r ON r.nome = 'Vegano'
 WHERE u.email = 'carlos.dias@sulwork.demo';

-- Rascunho da Ana
INSERT INTO evento (nome, descricao, tipo, data_inicio, data_fim, prazo_alteracao, status, organizador_id)
SELECT 'Ceia de fim de ano',
       'Ainda montando a lista.',
       'JANTAR',
       TIMESTAMPTZ '2026-12-24 20:00:00-03',
       TIMESTAMPTZ '2026-12-25 01:00:00-03',
       TIMESTAMPTZ '2026-12-20 18:00:00-03',
       'RASCUNHO',
       id
  FROM usuario
 WHERE email = 'ana.souza@sulwork.demo';

-- Churrasco aberto da Ana
INSERT INTO evento (nome, descricao, tipo, data_inicio, data_fim, prazo_alteracao, status, organizador_id)
SELECT 'Churrasco da equipe',
       'Confraternização no escritório.',
       'CHURRASCO',
       TIMESTAMPTZ '2026-11-15 12:00:00-03',
       TIMESTAMPTZ '2026-11-15 17:00:00-03',
       TIMESTAMPTZ '2026-11-14 18:00:00-03',
       'ABERTO',
       id
  FROM usuario
 WHERE email = 'ana.souza@sulwork.demo';

INSERT INTO item (evento_id, categoria_id, nome, quantidade_necessaria, obrigatorio)
SELECT e.id, c.id, v.nome, v.qtd, v.obrigatorio
  FROM evento e
  JOIN (VALUES
        ('Bebidas', 'Refrigerante 2L', 10, TRUE),
        ('Carnes', 'Picanha', 3, TRUE),
        ('Descartáveis e utensílios', 'Guardanapo', 100, FALSE),
        ('Pães e frios', 'Pão francês', 50, FALSE)
       ) AS v(categoria, nome, qtd, obrigatorio)
    ON TRUE
  JOIN categoria c ON c.nome = v.categoria
 WHERE e.nome = 'Churrasco da equipe';

INSERT INTO item_alergeno (item_id, alergeno_id)
SELECT i.id, a.id
  FROM item i
  JOIN evento e ON e.id = i.evento_id
  JOIN alergeno a ON a.nome = 'Glúten'
 WHERE e.nome = 'Churrasco da equipe'
   AND i.nome = 'Pão francês';

INSERT INTO item_alergeno (item_id, alergeno_id)
SELECT i.id, a.id
  FROM item i
  JOIN evento e ON e.id = i.evento_id
  JOIN alergeno a ON a.nome = 'Carne'
 WHERE e.nome = 'Churrasco da equipe'
   AND i.nome = 'Picanha';

INSERT INTO meta_categoria (evento_id, categoria_id, quantidade_meta)
SELECT e.id, c.id, v.qtd
  FROM evento e
  JOIN (VALUES
        ('Bebidas', 12),
        ('Carnes', 3)
       ) AS v(categoria, qtd) ON TRUE
  JOIN categoria c ON c.nome = v.categoria
 WHERE e.nome = 'Churrasco da equipe';

INSERT INTO participacao (usuario_id, evento_id)
SELECT u.id, e.id
  FROM usuario u
  JOIN evento e ON e.nome = 'Churrasco da equipe'
 WHERE u.email IN ('ana.souza@sulwork.demo', 'bia.lima@sulwork.demo', 'carlos.dias@sulwork.demo');

INSERT INTO contribuicao (participacao_id, item_id, quantidade)
SELECT p.id, i.id, 5
  FROM participacao p
  JOIN usuario u ON u.id = p.usuario_id
  JOIN evento e ON e.id = p.evento_id
  JOIN item i ON i.evento_id = e.id AND i.nome = 'Refrigerante 2L'
 WHERE u.email = 'bia.lima@sulwork.demo'
   AND e.nome = 'Churrasco da equipe';

INSERT INTO contribuicao (participacao_id, item_id, quantidade)
SELECT p.id, i.id, 2
  FROM participacao p
  JOIN usuario u ON u.id = p.usuario_id
  JOIN evento e ON e.id = p.evento_id
  JOIN item i ON i.evento_id = e.id AND i.nome = 'Picanha'
 WHERE u.email = 'ana.souza@sulwork.demo'
   AND e.nome = 'Churrasco da equipe';

-- Almoço já encerrado do Carlos
INSERT INTO evento (nome, descricao, tipo, data_inicio, data_fim, prazo_alteracao, status, organizador_id)
SELECT 'Almoço de alinhamento',
       'Prazo encerrado; serve para ver o painel depois do prazo.',
       'ALMOCO',
       TIMESTAMPTZ '2026-10-10 12:00:00-03',
       TIMESTAMPTZ '2026-10-10 14:00:00-03',
       TIMESTAMPTZ '2026-10-09 18:00:00-03',
       'ENCERRADO',
       id
  FROM usuario
 WHERE email = 'carlos.dias@sulwork.demo';

INSERT INTO item (evento_id, categoria_id, nome, quantidade_necessaria, obrigatorio)
SELECT e.id, c.id, 'Suco de laranja', 8, TRUE
  FROM evento e
  JOIN categoria c ON c.nome = 'Bebidas'
 WHERE e.nome = 'Almoço de alinhamento';

INSERT INTO participacao (usuario_id, evento_id)
SELECT u.id, e.id
  FROM usuario u
  JOIN evento e ON e.nome = 'Almoço de alinhamento'
 WHERE u.email IN ('carlos.dias@sulwork.demo', 'ana.souza@sulwork.demo');

INSERT INTO contribuicao (participacao_id, item_id, quantidade)
SELECT p.id, i.id, 8
  FROM participacao p
  JOIN usuario u ON u.id = p.usuario_id
  JOIN evento e ON e.id = p.evento_id
  JOIN item i ON i.evento_id = e.id
 WHERE u.email = 'ana.souza@sulwork.demo'
   AND e.nome = 'Almoço de alinhamento';
