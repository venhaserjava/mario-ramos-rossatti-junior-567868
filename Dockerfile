# ESTÁGIO ÚNICO: Runtime (Aproveitando o seu build local de sucesso)
FROM eclipse-temurin:21-jre-jammy

ENV LANGUAGE='en_US:en'
WORKDIR /deployments

# Configuração de permissões e usuário
RUN chown 1001 /deployments \
    && chmod "g+rwX" /deployments \
    && chown 1001:root /deployments

# Copia os arquivos DIRETAMENTE da sua pasta target local para o container
COPY --chown=1001 target/quarkus-app/lib/ /deployments/lib/
COPY --chown=1001 target/quarkus-app/*.jar /deployments/
COPY --chown=1001 target/quarkus-app/app/ /deployments/app/
COPY --chown=1001 target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8081
USER 1001

ENV JAVA_OPTIONS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"

ENTRYPOINT [ "java", "-jar", "/deployments/quarkus-run.jar" ]