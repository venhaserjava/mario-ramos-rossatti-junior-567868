-- Adiciona a coluna de referência para o artista na tabela já existente
ALTER TABLE albuns ADD COLUMN artista_id INTEGER;

-- Cria a restrição de chave estrangeira
ALTER TABLE albuns 
ADD CONSTRAINT fk_artista 
FOREIGN KEY (artista_id) REFERENCES artistas(id);

-- Opcional: Se quiser manter o relacionamento N:N criado na V1.0.0, 
-- você pode remover a tabela intermediária se ela não for mais necessária:
-- DROP TABLE artista_album;

-- Inserção de teste para o artista 1 (conforme seu plano anterior)
-- Nota: Na V1.0.0, o título do álbum era a coluna 'titulo', vamos manter o padrão da V1.0.0
INSERT INTO albuns (titulo, ano_lancamento, artista_id) VALUES ('Hybrid Theory', 2000, 1);
