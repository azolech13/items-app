package pl.com.betacom.itemsapp

import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.util.ArrayList
import java.util.List
import org.slf4j.LoggerFactory
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.images.builder.ImageFromDockerfile

class ItemsAppEnvironment {

    private final Network network
    private final MySQLContainer mysql
    private final ImageFromDockerfile image
    private GenericContainer app
    private final List<GenericContainer> others = new ArrayList<>()

    private ItemsAppEnvironment(Network network, MySQLContainer mysql, ImageFromDockerfile image) {
        this.network = network
        this.mysql = mysql
        this.image = image
    }

    static ItemsAppEnvironment start() {
        Path jar = resolveJar()
        ImageFromDockerfile image = new ImageFromDockerfile()
                .withDockerfileFromBuilder(builder -> builder
                        .from("eclipse-temurin:21-jre")
                        .workDir("/app")
                        .copy("app.jar", "app.jar")
                        .entryPoint("java", "-jar", "/app/app.jar")
                        .build())
                .withFileFromPath("app.jar", jar)
        Network network = Network.newNetwork()
        MySQLContainer mysql = new MySQLContainer("mysql:8.4")
                .withNetwork(network)
                .withNetworkAliases("mysql")
                .withDatabaseName("items")
                .withUsername("items")
                .withPassword("items")
        try {
            mysql.start()
            return new ItemsAppEnvironment(network, mysql, image)
        } catch (RuntimeException exception) {
            closeQuietly { mysql.stop() }
            closeQuietly { network.close() }
            throw exception
        }
    }

    ItemsAppClient startApp(String name, Map<String, String> extraEnv = [:]) {
        stopApp()
        this.app = launch(name, extraEnv)
        return clientFor(this.app)
    }

    ItemsAppClient startAlongside(String name) {
        GenericContainer container = launch(name, [:])
        others.add(container)
        return clientFor(container)
    }

    void stopApp() {
        GenericContainer running = this.app
        this.app = null
        if (running != null) {
            closeQuietly { running.stop() }
        }
    }

    void close() {
        others.each { container -> closeQuietly { container.stop() } }
        others.clear()
        stopApp()
        closeQuietly { mysql.stop() }
        closeQuietly { network.close() }
    }

    private GenericContainer launch(String name, Map<String, String> extraEnv) {
        GenericContainer container = new GenericContainer(image)
                .withNetwork(network)
                .withEnv("SPRING_PROFILES_ACTIVE", "docker")
                .withExposedPorts(8080)
                .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("items-app-" + name)).withPrefix(name))
                .waitingFor(Wait.forHttp("/items").forStatusCode(401))
                .withStartupTimeout(Duration.ofMinutes(3))
        extraEnv.each { key, value -> container.withEnv(key, value) }
        try {
            container.start()
            return container
        } catch (RuntimeException exception) {
            closeQuietly { container.stop() }
            throw exception
        }
    }

    private static ItemsAppClient clientFor(GenericContainer container) {
        new ItemsAppClient("http://" + container.host + ":" + container.getMappedPort(8080))
    }

    private static Path resolveJar() {
        String jarPath = System.getProperty("items.app.jar")
        if (jarPath == null || jarPath.isBlank()) {
            throw new IllegalStateException("System property 'items.app.jar' is not set. Run via './gradlew integrationTest'.")
        }
        Path jar = Path.of(jarPath)
        if (!Files.isRegularFile(jar)) {
            throw new IllegalStateException("Application jar not found: " + jar)
        }
        return jar
    }

    private static void closeQuietly(Runnable action) {
        try {
            action.run()
        } catch (Exception ignored) {
        }
    }
}
