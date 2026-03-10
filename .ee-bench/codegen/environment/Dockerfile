FROM eclipse-temurin:24-jdk

# Install git and Docker CLI (required for Testcontainers)
RUN apt-get update && apt-get install -y git python3 ca-certificates curl gnupg && \
    install -m 0755 -d /etc/apt/keyrings && \
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg \
      | gpg --dearmor -o /etc/apt/keyrings/docker.gpg && \
    chmod a+r /etc/apt/keyrings/docker.gpg && \
    echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
      https://download.docker.com/linux/ubuntu jammy stable" \
      > /etc/apt/sources.list.d/docker.list && \
    apt-get update && apt-get install -y docker-ce-cli && \
    rm -rf /var/lib/apt/lists/*

ARG REPO_URL={{ instance.repo_url }}
ARG BASE_COMMIT={{ instance.base_commit }}
ARG BASE_BRANCH={{ instance.base_ref }}

WORKDIR /repo
RUN git clone "${REPO_URL}" . && git checkout "${BASE_COMMIT}"

# Fetch gradle-wrapper.jar from the base branch (not committed at merge base).
RUN git fetch origin "${BASE_BRANCH}" && \
    git checkout "origin/${BASE_BRANCH}" -- gradle/wrapper/gradle-wrapper.jar 2>/dev/null || true

# Pre-download all Gradle dependencies to speed up eval runs
RUN ./gradlew dependencies --no-daemon -q 2>/dev/null || true

LABEL ee-bench.type="codegen"
