FROM mozilla/sbt:latest AS build

RUN mkdir /build

WORKDIR /build

COPY build.sbt /build/
COPY project /build/project/

RUN sbt -no-colors update

ADD . /build/

RUN sbt -no-colors dist \
 && unzip target/universal/yearanalyser-*.zip

FROM adoptopenjdk/openjdk12:alpine-slim

RUN apk update && apk add --no-cache curl

RUN adduser -S play \
 && mkdir /app \
 && chown play /app

USER play

WORKDIR /app

COPY --from=build "/build/yearanalyser-*/bin" /app/bin/
COPY --from=build "/build/yearanalyser-*/lib" /app/lib/
COPY --from=build "/build/yearanalyser-*/conf" /app/conf/

EXPOSE 9000/tcp

HEALTHCHECK --interval=20s --timeout=3s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:9000/ || exit 1

ENTRYPOINT ["./bin/yearanalyser"]
