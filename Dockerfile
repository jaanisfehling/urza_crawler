ARG BUILD_HOME=/urza_crawler

FROM gradle:8.0.2-jdk19-alpine AS build-image

ARG BUILD_HOME
ENV APP_HOME=$BUILD_HOME

WORKDIR $APP_HOME

COPY --chown=gradle:gradle app/build.gradle settings.gradle $APP_HOME/
COPY --chown=gradle:gradle app/src $APP_HOME/src

RUN gradle build --no-daemon


FROM amazoncorretto:19.0.2-alpine3.17

ARG BUILD_HOME
ENV APP_HOME=$BUILD_HOME

COPY --from=build-image $APP_HOME/build/libs/*.jar app/

CMD java -jar app/app.jar