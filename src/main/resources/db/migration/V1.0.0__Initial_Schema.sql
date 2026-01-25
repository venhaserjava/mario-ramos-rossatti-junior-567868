-- Tabela de Artistas
CREATE TABLE artistas (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    tipo VARCHAR(50) -- 'Cantor' ou 'Banda' para atender requisito (e)
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

-- Tabela de Regionais (Requisito Senior)
CREATE TABLE regionais (
    id INTEGER PRIMARY KEY,
    nome VARCHAR(200) NOT NULL,
    ativo BOOLEAN DEFAULT TRUE
);

-- CARGA INICIAL (Dados do Edital)
INSERT INTO artistas (id, nome, tipo) VALUES 
(1, 'Serj Tankian', 'Cantor'),
(2, 'Mike Shinoda', 'Cantor'),
(3, 'Michel Teló', 'Cantor'),
(4, 'Guns N’ Roses', 'Banda');

INSERT INTO albuns (id, titulo) VALUES 
(1, 'Harakiri'), (2, 'Black Blooms'), (3, 'The Rough Dog'), -- Serj
(4, 'The Rising Tied'), (5, 'Post Traumatic'), (6, 'Post Traumatic EP'), (7, 'Where’d You Go'), -- Mike
(8, 'Bem Sertanejo'), (9, 'Bem Sertanejo - O Show (Ao Vivo)'), (10, 'Bem Sertanejo - (1ª Temporada) - EP'), -- Michel
(11, 'Use Your Illusion I'), (12, 'Use Your Illusion II'), (13, 'Greatest Hits'); -- Guns

INSERT INTO artista_album (artista_id, album_id) VALUES 
(1,1), (1,2), (1,3),
(2,4), (2,5), (2,6), (2,7),
(3,8), (3,9), (3,10),
(4,11), (4,12), (4,13);