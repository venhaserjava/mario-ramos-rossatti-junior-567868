-- Tabela de Artistas (SERIAL cuidará do ID e da Sequence automaticamente)
CREATE TABLE artistas (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    tipo VARCHAR(50) 
);

-- Tabela de Álbuns
CREATE TABLE albuns (
    id SERIAL PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    ano_lancamento INTEGER,
    capa_url VARCHAR(500)
);

-- Tabela Intermediária (Relacionamento N:N)
CREATE TABLE artista_album (
    artista_id INTEGER REFERENCES artistas(id) ON DELETE CASCADE,
    album_id INTEGER REFERENCES albuns(id) ON DELETE CASCADE,
    PRIMARY KEY (artista_id, album_id)
);

-- Tabela de Regionais
CREATE TABLE regionais (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(200) NOT NULL,
    ativo BOOLEAN DEFAULT TRUE
);

-- CARGA INICIAL (Sem IDs manuais para não quebrar a Sequence do IDENTITY)
INSERT INTO artistas (nome, tipo) VALUES 
('Serj Tankian', 'Cantor'),
('Mike Shinoda', 'Cantor'),
('Michel Teló', 'Cantor'),
('Guns N’ Roses', 'Banda');

INSERT INTO albuns (titulo) VALUES 
('Harakiri'), ('Black Blooms'), ('The Rough Dog'), -- IDs gerados: 1, 2, 3
('The Rising Tied'), ('Post Traumatic'), ('Post Traumatic EP'), ('Where’d You Go'), -- 4, 5, 6, 7
('Bem Sertanejo'), ('Bem Sertanejo - O Show (Ao Vivo)'), ('Bem Sertanejo - (1ª Temporada) - EP'), -- 8, 9, 10
('Use Your Illusion I'), ('Use Your Illusion II'), ('Greatest Hits'); -- 11, 12, 13

-- Associação baseada na ordem de inserção (considerando IDs gerados automaticamente)
INSERT INTO artista_album (artista_id, album_id) VALUES 
(1,1), (1,2), (1,3),
(2,4), (2,5), (2,6), (2,7),
(3,8), (3,9), (3,10),
(4,11), (4,12), (4,13);