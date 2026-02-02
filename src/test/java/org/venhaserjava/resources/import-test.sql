-- Limpa as tabelas para garantir um estado inicial conhecido
DELETE FROM artista_album;
DELETE FROM albuns;
DELETE FROM artistas;

-- Insere um artista base para os testes de álbuns
INSERT INTO artistas (id, nome, tipo) VALUES (1, 'Artista de Teste', 'Banda');
-- Se o seu banco usar SERIAL, este comando garante que o próximo ID será 2
SELECT setval(pg_get_serial_sequence('artistas', 'id'), 1);