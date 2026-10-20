-- Catálogos fixos. Os ids são gerados pelo banco; os vínculos usam o nome.

INSERT INTO categoria (nome) VALUES
    ('Bebidas'),
    ('Salgados'),
    ('Doces e sobremesas'),
    ('Pratos principais'),
    ('Acompanhamentos'),
    ('Carnes'),
    ('Frutas'),
    ('Pães e frios'),
    ('Descartáveis e utensílios'),
    ('Outros');

-- Alergênicos de rotulagem obrigatória (RDC 26/2015 da Anvisa, com gergelim)
-- mais Carne e Mel, usados nas restrições de origem (vegetariano e vegano).
INSERT INTO alergeno (nome) VALUES
    ('Glúten'),
    ('Leite e derivados'),
    ('Ovo'),
    ('Amendoim'),
    ('Castanhas e nozes'),
    ('Soja'),
    ('Peixe'),
    ('Crustáceos'),
    ('Gergelim'),
    ('Carne'),
    ('Mel');

INSERT INTO restricao_alimentar (nome) VALUES
    ('Doença celíaca / sem glúten'),
    ('Intolerância à lactose'),
    ('Alergia a leite'),
    ('Alergia a ovo'),
    ('Alergia a amendoim'),
    ('Alergia a castanhas'),
    ('Alergia a soja'),
    ('Alergia a peixe'),
    ('Alergia a crustáceos'),
    ('Alergia a gergelim'),
    ('Vegetariano'),
    ('Vegano');

INSERT INTO restricao_alergeno (restricao_id, alergeno_id)
SELECT r.id, a.id
FROM (VALUES
    ('Doença celíaca / sem glúten', 'Glúten'),
    ('Intolerância à lactose', 'Leite e derivados'),
    ('Alergia a leite', 'Leite e derivados'),
    ('Alergia a ovo', 'Ovo'),
    ('Alergia a amendoim', 'Amendoim'),
    ('Alergia a castanhas', 'Castanhas e nozes'),
    ('Alergia a soja', 'Soja'),
    ('Alergia a peixe', 'Peixe'),
    ('Alergia a crustáceos', 'Crustáceos'),
    ('Alergia a gergelim', 'Gergelim'),
    ('Vegetariano', 'Carne'),
    ('Vegetariano', 'Peixe'),
    ('Vegetariano', 'Crustáceos'),
    ('Vegano', 'Carne'),
    ('Vegano', 'Peixe'),
    ('Vegano', 'Crustáceos'),
    ('Vegano', 'Leite e derivados'),
    ('Vegano', 'Ovo'),
    ('Vegano', 'Mel')
) AS mapeamento (restricao, alergeno)
JOIN restricao_alimentar r ON r.nome = mapeamento.restricao
JOIN alergeno a ON a.nome = mapeamento.alergeno;
