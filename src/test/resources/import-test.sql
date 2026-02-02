-- 1. Limpeza (Ordem reversa para não violar FK)
DELETE FROM artista_album;
DELETE FROM albuns;
DELETE FROM artistas;

-- 2. Inserção de dados base
-- Inserimos o artista
INSERT INTO artistas (id, nome, tipo) VALUES (1, 'Linkin Park', 'Banda');

-- Inserimos o álbum (Meteora)
INSERT INTO albuns (id, titulo, ano_lancamento) VALUES (1, 'Meteora', 2003);

-- 3. Criamos o vínculo na tabela de ligação
INSERT INTO artista_album (artista_id, album_id) VALUES (1, 1);

-- 4. SINCRONIZAÇÃO DE SEQUENCES (O SEGREDO DO SÊNIOR)
-- Resetamos as sequences para que o próximo INSERT pelo Java comece do ID 2
-- Isso evita erro de "Unique Constraint Violation" nos outros testes de criação
SELECT setval('artistas_id_seq', (SELECT MAX(id) FROM artistas));
SELECT setval('albuns_id_seq', (SELECT MAX(id) FROM albuns));


