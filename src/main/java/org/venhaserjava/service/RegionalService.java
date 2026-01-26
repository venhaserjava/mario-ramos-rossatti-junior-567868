package org.venhaserjava.service;


import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.venhaserjava.client.RegionalClient;
import org.venhaserjava.dto.RegionalDTO;
import org.venhaserjava.model.Regional;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class RegionalService {

    @Inject
    @RestClient
    RegionalClient regionalClient;

    @WithTransaction
    public Uni<Void> sincronizar() {
        return Uni.combine().all().unis(
                regionalClient.buscarRegionaisExternas(),
                Regional.<Regional>listAll()
        ).asTuple().chain(tuple -> {
            List<RegionalDTO> externas = tuple.getItem1();
            List<Regional> locais = tuple.getItem2();

            // 1. Inativar quem não está na lista externa ou mudou (Regra do Edital)
            List<Uni<Void>> operacoes = locais.stream()
                .filter(l -> l.ativo)
                .filter(l -> externas.stream().noneMatch(e -> e.nome.equals(l.nome)))
                .map(l -> {
                    l.ativo = false;
                    return l.persist().replaceWithVoid();
                }).collect(Collectors.toList());

            // 2. Inserir quem é novo
            externas.stream()
                .filter(e -> locais.stream().noneMatch(l -> l.nome.equals(e.nome)))
                .forEach(e -> {
                    Regional nova = new Regional();
                    nova.nome = e.nome;
                    nova.ativo = true;
                    operacoes.add(nova.persist().replaceWithVoid());
                });

            return Uni.combine().all().unis(operacoes).discardItems();
        });
    }
}