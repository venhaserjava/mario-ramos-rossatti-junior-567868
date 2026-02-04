//package org.venhaserjava.service;


package org.venhaserjava.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.nio.file.Path;
import java.time.Duration;
import java.util.UUID;

@ApplicationScoped
public class S3Service {

    @Inject
    S3AsyncClient s3;

    @Inject
    S3Presigner presigner; // Necessário para gerar a assinatura da URL

    @ConfigProperty(name = "bucket.name")
    String bucketName;

    /**
     * Requisito: Faz o upload e retorna a URL pré-assinada de 30 minutos.
     */
    public Uni<String> uploadCapa(Path filePath, String contentType) {
        String fileName = UUID.randomUUID() + "-" + filePath.getFileName().toString();

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(contentType)
                .build();

        return Uni.createFrom().completionStage(() -> s3.putObject(putRequest, AsyncRequestBody.fromFile(filePath)))
                .onItem().transform(response -> generatePresignedUrl(fileName));
    }

    /**
     * [SÊNIOR] Gera a URL temporária. 
     * Cumpre o requisito de expiração de 30 minutos do edital.
     */
    public String generatePresignedUrl(String fileName) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(30)) // Expiração de 30 min
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
        
        return presignedRequest.url().toString();
    }

    public Uni<Void> inicializarBucket() {
        return Uni.createFrom().completionStage(() -> s3.headBucket(HeadBucketRequest.builder().bucket(bucketName).build()))
                .replaceWithVoid() 
                .onFailure().recoverWithUni(t -> {
                    System.out.println("Bucket '" + bucketName + "' não encontrado. Criando...");
                    return Uni.createFrom().completionStage(() -> 
                        s3.createBucket(CreateBucketRequest.builder().bucket(bucketName).build())
                    ).replaceWithVoid();
                });
    }
    
    void onStart(@jakarta.enterprise.event.Observes io.quarkus.runtime.StartupEvent ev) {
        this.inicializarBucket().subscribe().with(
            success -> System.out.println("Infra S3 (MinIO) verificada."),
            failure -> System.err.println("Erro S3: " + failure.getMessage())
        );
    }
}
/*
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.file.Path;
import java.util.UUID;

@ApplicationScoped
public class S3Service {

    @Inject
    S3AsyncClient s3; // Cliente injetado e configurado pelo Quarkus

    @ConfigProperty(name = "bucket.name")
    String bucketName;

    @ConfigProperty(name = "quarkus.s3.endpoint-override")
    String endpoint;

    //
    // Requisito 28: Faz o upload da imagem e retorna a URL pública
    //
    public Uni<String> uploadCapa(Path filePath, String contentType) {
        // Geramos um nome único para o arquivo para evitar sobrescrita
        String fileName = UUID.randomUUID() + "-" + filePath.getFileName().toString();

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(contentType)
                .build();

        return Uni.createFrom().completionStage(() -> s3.putObject(putRequest, AsyncRequestBody.fromFile(filePath)))
                .onItem().transform(response -> String.format("%s/%s/%s", endpoint, bucketName, fileName));
    }

//
    // Garante que o bucket existe ao iniciar a aplicação.
    // Refatorado para garantir compatibilidade de tipos no recoverWithUni.
     //
    public Uni<Void> inicializarBucket() {
        return Uni.createFrom().completionStage(() -> s3.headBucket(HeadBucketRequest.builder().bucket(bucketName).build()))
                // Transformamos o sucesso em Void imediatamente para uniformizar o fluxo
                .replaceWithVoid() 
                .onFailure().recoverWithUni(t -> {
                    System.out.println("Bucket '" + bucketName + "' não encontrado ou sem acesso. Tentando criar...");
                    
                    return Uni.createFrom().completionStage(() -> 
                        s3.createBucket(CreateBucketRequest.builder().bucket(bucketName).build())
                    ).replaceWithVoid(); // Agora ambos os caminhos retornam Uni<Void>
                });
    }
    
    //
    // Listener que dispara a criação do bucket no startup
    //
    void onStart(@jakarta.enterprise.event.Observes io.quarkus.runtime.StartupEvent ev) {
        this.inicializarBucket().subscribe().with(
            success -> System.out.println("Infra S3 (MinIO) verificada com sucesso."),
            failure -> System.err.println("Erro ao inicializar infra S3: " + failure.getMessage())
        );
    }
}
*/