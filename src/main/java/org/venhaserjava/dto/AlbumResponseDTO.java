package org.venhaserjava.dto;

import java.util.List;
import java.util.stream.Collectors;
import org.venhaserjava.model.Album;

/**
 * DTO de resposta para a entidade Álbum.
 * <p>
 * Utilizado para desacoplar a representação externa (API) da estrutura 
 * interna de persistência, garantindo que apenas dados necessários sejam 
 * trafegados, seguindo os padrões de Clean Code e segurança do edital.
 * </p>
 * * @author Mario Ramos Rossatti Junior
 */
public class AlbumResponseDTO {

    /**
     * Identificador único do álbum.
     */
    public Long id;

    /**
     * Título da obra musical.
     */
    public String titulo;

    /**
     * Ano em que o álbum foi lançado oficialmente.
     */
    public Integer anoLancamento;

    /**
     * URL de acesso à imagem da capa (pode ser uma Presigned URL do S3).
     */
    public String capaUrl;

    /**
     * Lista simplificada de artistas que participam deste álbum.
     */
    public List<ArtistaMinDTO> artistas;

    /**
     * Construtor que realiza o mapeamento (De-To) da entidade para o DTO.
     * * @param album Entidade de origem vinda do banco de dados.
     */
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

    /**
     * DTO interno simplificado para representação de Artistas dentro de um Álbum.
     * Evita recursão infinita e reduz o overhead de dados na resposta.
     */
    public static class ArtistaMinDTO {
        /**
         * ID do artista.
         */
        public Long id;

        /**
         * Nome ou pseudônimo artístico.
         */
        public String nome;

        /**
         * Categoria do artista (Ex: Solo, Banda).
         */
        public String tipo;

        /**
         * Construtor de mapeamento simplificado.
         * * @param artista Entidade Artista original.
         */
        public ArtistaMinDTO(org.venhaserjava.model.Artista artista) {
            this.id = artista.id;
            this.nome = artista.nome;
            this.tipo = artista.tipo;
        }
    }
}
