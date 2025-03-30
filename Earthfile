VERSION 0.8

build:
    FROM eclipse-temurin:17-jdk
    WORKDIR /project
    COPY .git .git
    COPY gradle gradle
    COPY metadata metadata
    COPY app app
    COPY build.gradle ./
    COPY gradle.properties ./
    COPY gradlew ./
    COPY settings.gradle ./
    RUN TZ=Europe/Berlin ./gradlew clean test
    RUN TZ=Europe/Berlin ./gradlew assemble
    SAVE ARTIFACT app/build AS LOCAL build

build-and-release-on-github:
    ARG --required GITHUB_TOKEN
    BUILD +build
    FROM ubuntu:noble
    WORKDIR /project
    RUN apt-get update >/dev/null 2>&1 && apt-get -y install curl gpg >/dev/null 2>&1
    RUN curl -fsSL https://cli.github.com/packages/githubcli-archive-keyring.gpg | gpg --dearmor -o /usr/share/keyrings/githubcli-archive-keyring.gpg
    RUN echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/githubcli-archive-keyring.gpg] https://cli.github.com/packages stable main" | tee /etc/apt/sources.list.d/github-cli.list > /dev/null
    RUN apt-get update >/dev/null 2>&1 && apt-get -y install gh >/dev/null 2>&1
    COPY .git .git
    COPY +build/build build
    RUN --push export TAG=$(git tag --points-at HEAD); \
               echo TAG: $TAG; \
               export MATCH=$(echo "$TAG" | grep -e "^v"); \
               if [ -n "$MATCH" ]; then \
                 export RELEASE_HASH=$(git rev-parse HEAD); \
                 echo RELEASE_HASH: $RELEASE_HASH; \
                 export FILES=$(ls build/outputs/apk/release/*.apk); \
                 echo FILES: $FILES; \
                 gh release create $TAG --target $RELEASE_HASH --title $TAG --notes "see changelog: https://zephyrsoft.org/sdbviewer/history" $FILES; \
               else \
                 echo "not releasing, no eligible tag found"; \
               fi
