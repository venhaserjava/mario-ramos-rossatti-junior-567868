package org.venhaserjava.service;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger; // Adicionado para rastreabilidade
import org.venhaserjava.client.RegionalClient;
import org.venhaserjava.dto.RegionalDTO;
import org.venhaserjava.model.Regional;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class RegionalService {

    private static final Logger LOG = Logger.getLogger(RegionalService.class);

    @Inject
    @RestClient
    RegionalClient regionalClient;

    @WithTransaction
    public Uni<Void> sincronizar() {
        LOG.info("Iniciando sincronização de regionais...");

        return Uni.combine().all().unis(
                regionalClient.buscarRegionaisExternas(),
                Regional.<Regional>listAll()
        ).asTuple().chain(tuple -> {
            List<RegionalDTO> externas = tuple.getItem1();
            List<Regional> locais = tuple.getItem2();

            // 1. Identificar quem deve ser inativado (está no banco mas não na API externa)
            List<Uni<Void>> operacoes = locais.stream()
                .filter(l -> l.ativo)
                .filter(l -> externas.stream().noneMatch(e -> e.nome.equals(l.nome)))
                .map(l -> {
                    LOG.infof("Inativando regional: %s", l.nome);
                    l.ativo = false;
                    return l.persist().replaceWithVoid();
                }).collect(Collectors.toCollection(java.util.ArrayList::new));

            // 2. Identificar quem deve ser inserido (está na API externa mas não no banco)
            externas.stream()
                .filter(e -> locais.stream().noneMatch(l -> l.nome.equals(e.nome)))
                .forEach(e -> {
                    LOG.infof("Inserindo nova regional: %s", e.nome);
                    Regional nova = new Regional();
                    nova.nome = e.nome;
                    nova.ativo = true;
                    operacoes.add(nova.persist().replaceWithVoid());
                });

            // CORREÇÃO: Se não houver nada para processar, retornamos sucesso sem erro
            if (operacoes.isEmpty()) {
                LOG.info("Sincronização finalizada: Nenhuma alteração necessária.");
                return Uni.createFrom().voidItem();
            }

            LOG.infof("Processando %d operações de sincronização...", operacoes.size());
            return Uni.combine().all().unis(operacoes).discardItems();
        });
    }
}