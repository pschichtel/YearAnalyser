FROM mozilla/sbt:latest@sha256:df8eeb6a4c1752ae722aa638a5052627426bee5b7a321212b797d8c92dbd44e8 AS build

RUN mkdir /build

WORKDIR /build

COPY build.sbt /build/
COPY project /build/project/

RUN sbt -no-colors update

ADD . /build/

RUN sbt -no-colors dist \
 && unzip target/universal/yearanalyser-*.zip

FROM adoptopenjdk/openjdk12:alpine-slim@sha256:dfa9585ad7318215493b1c2c47b934c27ceafcf3288949a65a4cf059b1c00cb5

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
