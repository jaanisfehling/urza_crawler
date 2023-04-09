FROM openjdk:19-jdk-slim

WORKDIR /app

# Copy the Gradle files to the container
COPY app/build.gradle .
COPY settings.gradle .

# Copy the application source code to the container

# Install Gradle
RUN apt-get update && \
    apt-get install -y curl unzip && \
    curl -L https://services.gradle.org/distributions/gradle-8.0.2-bin.zip && \
    unzip -d /gradle gradle-8.0.2-bin.zip && \
    rm gradle-7.1-bin.zip

ENV GRADLE_HOME=app/gradle/gradle-8.0.2
ENV PATH=$PATH:$GRADLE_HOME/bin

COPY src/ ./scr/


# Build the application using Gradle
RUN gradle build
# Set the command to run the application
CMD ["java", "-jar", "./build/libs/urza_crawler.jar"]