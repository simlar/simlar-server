FROM debian:bookworm

ENV LANG "C.UTF-8"
ENV DEBIAN_FRONTEND "noninteractive"

RUN apt-get update && \
    apt-get dist-upgrade --assume-yes && \
    apt-get install --assume-yes ca-certificates-java && \
    apt-get install --assume-yes --no-install-recommends openjdk-17-jdk-headless git && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

RUN useradd --create-home builder
USER builder
