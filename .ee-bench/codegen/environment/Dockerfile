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

WORKDIR /repo
RUN git clone "${REPO_URL}" . && git checkout "${BASE_COMMIT}"

# Pre-download all Gradle dependencies to speed up eval runs
RUN ./gradlew dependencies --no-daemon -q 2>/dev/null

LABEL ee-bench.type="codegen"
