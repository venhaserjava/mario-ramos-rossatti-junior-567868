package org.venhaserjava.dto;

import java.util.List;
import java.util.stream.Collectors;
import org.venhaserjava.model.Album;

public class AlbumResponseDTO {
    public Long id;
    public String titulo;
    public Integer anoLancamento;
    public String capaUrl;
    public List<ArtistaMinDTO> artistas;

    public AlbumResponseDTO(Album album) {
        this.id = album.id;
        this.titulo = album.titulo;
        this.anoLancamento = album.anoLancamento;
        this.capaUrl = album.capaUrl;
        if (album.artistas != null) {
            this.artistas = album.artistas.stream()
                .map(ArtistaMinDTO::new)
                .collect(Collectors.toList());
        }
    }

    public static class ArtistaMinDTO {
        public Long id;
        public String nome;
        public String tipo;

        public ArtistaMinDTO(org.venhaserjava.model.Artista artista) {
            this.id = artista.id;
            this.nome = artista.nome;
            this.tipo = artista.tipo;
        }
    }
}