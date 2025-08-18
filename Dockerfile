# Dockerfile  ── Java 11 + Spring Boot JAR 런타임
FROM openjdk:11-jdk-slim

#작업 디렉토리 설정
WORKDIR /app

# Maven 빌드 결과물 복사 (파일명 정확히!)
COPY target/myShop-0.0.1-SNAPSHOT.jar app.jar


ENTRYPOINT ["java", "-jar", "app.jar"]
