# Deployable GitLab CI Wallboard

This project uses Quarkus for the backend, and Angular and d3 for the frontend.

## Running in Docker

You can run the wallboard using the following:

```shell
docker run \
   -p8080:8080 \
   -escm.service.gitlab.host=gitlab.com \
   -escm.service.gitlab.token=my-token \
   -ebuild.service.gitlab-ci.host=gitlab.com \
   -ebuild.service.gitlab-ci.token=my-token \
   -esystem.dashboard.name="My Awesome Dashboard" \
   davedupplaw/gitlab-wallboard-quarkus
```

The following settings affect the running of the service

| Setting               | Options   | Default      | Description                                                                                               |
|-----------------------|-----------|--------------|-----------------------------------------------------------------------------------------------------------|
| system.dashboard.name | string    | Build Status | The name of the dashboard that will show at the top of the UI                                             |
| scm.services          | gitlab    | gitlab       | Determines which SCM services are used to gather projects (only supports gitlab at the moment)            |
| build.services        | gitlab-ci | gitlab-ci    | Determines which build services are used to gather project status (only supports gitlab-ci at the moment) |

### GitLab Settings

These are the GitLab settings for getting project information:

| Setting                               | Options                     | Description                                                                |
|---------------------------------------|-----------------------------|----------------------------------------------------------------------------|
| scm.service.gitlab.host               | hostname                    | (required) The hostname used to connect to the GitLab API                  |
| scm.service.gitlab.token              | string                      | (required) The private token used to connect to the GitLab API (read only) |
 | scm.service.gitlab.whitelists.groups  | comma-separated group ids   | A comma-separated list of group ids to retrieve data from                  |
 | scm.service.gitlab.blacklist.projects | comma-separated project ids | A comma-separated list of projects ids to ignore                           |

These are the GitLab-CI settings for getting build information:

| Setting                                  | Options      | Default | Description                                                                      |
|------------------------------------------|--------------|---------|----------------------------------------------------------------------------------|
| build.service.gitlab-ci.host             | hostname     |         | (required) The hostname used to connect to the GitLab API                        |
| build.service.gitlab-ci.token            | string       |         | (required) The private token used to connect to the GitLab API (read only)       |  
| build.service.gitlab-ci.ref              | ref name     | master  | The branch name to retrieve builds from                                          |
| build.service.gitlab-ci.min-refresh-time | milliseconds | 20000   | The lower-bound number of milliseconds to wait until retrieving new build status |
| build.service.gitlab-ci.max-refresh-time | milliseconds | 30000   | The upper-bound number of milliseconds to wait until retrieving new build status | 

## Development

### Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw compile quarkus:dev -Dui.dev
```

This runs the backend on port 8080 with live-reloading and ensures the UI is also built.

If you are developing the UI too, you need to run the following in a separate shell:

```shell
yarn proxy
```

This will run a proxied live-reloading frontend on port 4200. The proxy will proxy backend
requests (`/api`) to port 8080 where the backend runs.

### Packaging and running the application

The application can be packaged using:

```shell script
mvn package quarkus:dev -Dui.deps -Dui.dev
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.type=uber-jar -Dui.deps -Dui.dev
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

### Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Pnative -Dui.deps -Dui.dev
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Pnative -Dquarkus.native.container-build=true -Dui.deps -Dui.dev
```

You can then execute your native executable with: `./target/uk.dupplaw.gitlab.wallboard-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.

### Creating a native Docker image

You can create a native docker image (running on GraalVM) using:

```shell
./mvnw clean package -Pnative -Dquarkus.native.container-build=true -Dquarkus.container-image.build=true -Dui -Dui.deps
```

### Related Guides

- RESTEasy Classic JSON-B ([guide](https://quarkus.io/guides/rest-json)): JSON-B serialization support for RESTEasy
  Classic
- Kotlin ([guide](https://quarkus.io/guides/kotlin)): Write your services in Kotlin
- WebSockets ([guide](https://quarkus.io/guides/websockets)): WebSocket communication channel support

# License

Released under MIT license.
