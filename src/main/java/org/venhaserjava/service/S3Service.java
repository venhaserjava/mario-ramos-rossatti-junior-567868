package org.venhaserjava.service;

import io.smallrye.mutiny.Uni;

import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.nio.file.Path;
import java.time.Duration;
import java.util.UUID;

/**
 * Serviço de integração com Storage compatível com AWS S3 (MinIO).
 * Implementa requisitos de persistência de binários e geração de URLs temporárias.
 */
@ApplicationScoped
public class S3Service {

    @Inject
    S3AsyncClient s3;

    @Inject
    S3Presigner presigner;

    @ConfigProperty(name = "bucket.name")
    String bucketName;

    /**
     * Executa o upload de um arquivo para o bucket configurado e gera uma URL assinada.
     * * @param filePath Caminho local temporário do arquivo.
     * @param contentType MIME type do arquivo (image/jpeg, etc).
     * @return Uni contendo a URL pré-assinada válida por 30 minutos.
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
     * Gera uma URL de acesso temporário conforme exigido no edital.
     * [SÊNIOR] A assinatura garante segurança, permitindo que o bucket permaneça privado.
     * * @param fileName Nome da chave do objeto no S3.
     * @return URL string com token de acesso.
     */
    public String generatePresignedUrl(String fileName) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(30))
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
        return presignedRequest.url().toString();
    }

    /**
     * Valida a existência do bucket no startup e o cria caso necessário.
     */
    public Uni<Void> inicializarBucket() {
        return Uni.createFrom().completionStage(() -> s3.headBucket(HeadBucketRequest.builder().bucket(bucketName).build()))
                .replaceWithVoid() 
                .onFailure().recoverWithUni(t -> 
                    Uni.createFrom().completionStage(() -> 
                        s3.createBucket(CreateBucketRequest.builder().bucket(bucketName).build())
                    ).replaceWithVoid()
                );
    }
}
