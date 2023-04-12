FROM gradle:8.0.2-jdk19-alpine AS build-image

WORKDIR /gradle-build

COPY --chown=gradle:gradle app/build.gradle settings.gradle app/logging.properties /gradle-build/
COPY --chown=gradle:gradle app/src /gradle-build/src

RUN gradle build --no-daemon


FROM amazoncorretto:19.0.2-alpine3.17

COPY --from=build-image /gradle-build/build/distributions/urza_crawler.tar /app/

WORKDIR /app
RUN tar -xvf urza_crawler.tar

WORKDIR /app/urza_crawler
COPY --from=build-image /gradle-build/logging.properties /app/urza_crawler

CMD bin/urza_crawler