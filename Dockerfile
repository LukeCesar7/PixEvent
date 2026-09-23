# ── Estágio 1: build ────────────────────────────────────────────────
# Compila o projeto com Maven + JDK 17. Essa imagem intermediária não vai
# pro artefato final, então não importa o tamanho dela.
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copia só o pom.xml primeiro e baixa as dependências — assim, se você só
# mudar código Java depois, o Docker reaproveita essa camada em cache e
# não baixa tudo de novo a cada build.
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# ── Estágio 2: runtime ──────────────────────────────────────────────
# Imagem final: só o JRE (não o JDK completo) + o .jar já compilado.
# Bem mais leve que carregar o Maven e o JDK inteiro pra produção.
FROM eclipse-temurin:17-jre-alpine

# Roda como usuário não-root — boa prática de segurança em produção.
RUN addgroup -S pixevent && adduser -S pixevent -G pixevent
USER pixevent

WORKDIR /app
COPY --from=build /app/target/pixevent-backend.jar app.jar

EXPOSE 3001

# Verifica periodicamente se a aplicação está respondendo, pra orquestradores
# (Docker Swarm, ECS, Kubernetes) saberem se precisam reiniciar o container.
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
    CMD wget -q --spider http://localhost:3001/api/mesas || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
