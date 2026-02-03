package org.venhaserjava.service;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
    // No topo do RegionalService.java
import io.quarkus.scheduler.Scheduled;
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

    // Adicione o Validator no topo da classe
    @Inject
    jakarta.validation.Validator validator;

    @WithTransaction
    public Uni<Void> sincronizar() {
        LOG.info("Iniciando sincronização de regionais...");

        return Uni.combine().all().unis(
                regionalClient.buscarRegionaisExternas(),
                Regional.<Regional>listAll()
        ).asTuple().chain(tuple -> {
            List<RegionalDTO> externas = tuple.getItem1();
            List<Regional> locais = tuple.getItem2();

            List<Uni<Void>> operacoes = new java.util.ArrayList<>();

            // 1. Inativação (Lógica atual está segura)
            locais.stream()
                .filter(l -> l.ativo)
                .filter(l -> externas.stream().noneMatch(e -> e.nome.equals(l.nome)))
                .forEach(l -> {
                    LOG.infof("Inativando regional: %s", l.nome);
                    l.ativo = false;
                    operacoes.add(l.persist().replaceWithVoid());
                });

            // 2. Inserção com Validação Manual (O CAPRICHO SÊNIOR)
            externas.stream()
                .filter(e -> locais.stream().noneMatch(l -> l.nome.equals(e.nome)))
                .forEach(e -> {
                    Regional nova = new Regional();
                    nova.nome = e.nome;
                    nova.ativo = true;

                    // Validamos antes de tentar persistir
                    var violations = validator.validate(nova);
                    if (violations.isEmpty()) {
                        LOG.infof("Inserindo nova regional: %s", e.nome);
                        operacoes.add(nova.persist().replaceWithVoid());
                    } else {
                        String erros = violations.stream()
                            .map(v -> v.getMessage())
                            .collect(Collectors.joining(", "));
                        LOG.errorf("Regional externa ignorada por dados inválidos (%s): %s", e.nome, erros);
                    }
                });

            if (operacoes.isEmpty()) {
                return Uni.createFrom().voidItem();
            }

            return Uni.combine().all().unis(operacoes).discardItems();
        });
    }


/*    
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
*/
    @Scheduled(every = "1h", identity = "sincronizacao-regionais")
    @io.smallrye.common.annotation.NonBlocking // <--- Adicione isso
    void scheduledSync() {
        LOG.info("Executando sincronização agendada...");
        this.sincronizar().subscribe().with(
            success -> LOG.info("Sincronização agendada concluída com sucesso."),
            failure -> LOG.error("Falha na sincronização agendada: " + failure.getMessage())
        );
    }

    // ... dentro da classe
    // @Scheduled(every = "1h", identity = "sincronizacao-regionais")
    // void scheduledSync() {
    //     LOG.info("Executando sincronização agendada...");
    //     this.sincronizar().subscribe().with(
    //         success -> LOG.info("Sincronização agendada concluída com sucesso."),
    //         failure -> LOG.error("Falha na sincronização agendada: " + failure.getMessage())
    //     );
    // }
}