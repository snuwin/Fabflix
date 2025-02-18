# Imagine we start with an empty container (machine) here, each command below makes some changes to the machine

# download all the necessary software to run maven (search this maven base image in DockerHub to see what is included)
FROM maven:3.8.5-openjdk-11-slim AS builder

# create and `cd` into a folder called "app" inside the virtual machine
WORKDIR /app

# copy everything in the current folder into the "app" folder. (src/ WebContent/ etc)
COPY . .

# compile the application inside the "app" folder to generate the war file
RUN mvn clean package

# download all the necessary software to run tomcat (this is another base image)
FROM tomcat:10-jdk11

# `cd` into the "app" folder inside the machine
WORKDIR /app

# copy the war file what we have generated earlier into the tomcat webapps folder inside the container
COPY --from=builder /app/target/project1.war /usr/local/tomcat/webapps/project1.war

# open the 8080 port of the container, so that outside requests can reach the tomcat server
EXPOSE 8080

# start tomcat server in the foreground
CMD ["catalina.sh", "run"]

# Side note: The final image would only contain the `tomcat` base image but not the `maven` base image.
# Learn more about Docker multi-stage build at (https://docs.docker.com/build/building/multi-stage/).