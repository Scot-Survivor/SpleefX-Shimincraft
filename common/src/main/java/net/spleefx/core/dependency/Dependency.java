package net.spleefx.core.dependency;

import com.google.common.collect.ImmutableList;
import net.spleefx.core.dependency.relocation.Relocation;
import net.spleefx.core.dependency.relocation.RelocationHelper;

import java.nio.file.Path;
import java.util.List;

/**
 * The dependencies used by LuckPerms.
 */
public enum Dependency {

    /* Important dependencies */
    ASM(
            "org.ow2.asm",
            "asm",
            "7.1"
    ),
    ASM_COMMONS(
            "org.ow2.asm",
            "asm-commons",
            "7.1"
    ),
    JAR_RELOCATOR(
            "me.lucko",
            "jar-relocator",
            "1.4"
    ),

    CAFFEINE(
            "com.github.ben-manes.caffeine",
            "caffeine",
            "2.8.5",
            Relocation.of("caffeine", "com{}github{}benmanes{}caffeine")
    ),

    /* our dependencies */
    OKIO(
            "com{}squareup{}" + RelocationHelper.OKIO_STRING,
            RelocationHelper.OKIO_STRING,
            "1.17.5",
            Relocation.of(RelocationHelper.OKIO_STRING, RelocationHelper.OKIO_STRING)
    ),

    OKHTTP(
            "com{}squareup{}" + RelocationHelper.OKHTTP3_STRING,
            "okhttp",
            "3.14.7",
            Relocation.of(RelocationHelper.OKHTTP3_STRING, RelocationHelper.OKHTTP3_STRING),
            Relocation.of(RelocationHelper.OKIO_STRING, RelocationHelper.OKIO_STRING)
    ),

    COMMODORE(
            "me{}lucko",
            "commodore",
            "1.7",
            Relocation.of("commodore", "me{}lucko{}commodore")
    ),

    GSON(
            "com.google.code.gson",
            "gson",
            "2.8.9",
            Relocation.of("gson", "com{}google{}gson")
    ),

    SNAKEYAML(
            "org.yaml",
            "snakeyaml",
            "1.30",
            Relocation.of("yaml", "org{}yaml{}snakeyaml")
    ),

    XSERIES("com.github.cryptomorin",
            "XSeries",
            "8.7.1",
            Relocation.of("xseries", "com{}cryptomorin{}xseries")
    ),

    /* Storage dependencies */
    MYSQL_DRIVER(
            "mysql",
            "mysql-connector-java",
            "8.0.21",
            Relocation.of("mysql", "com{}mysql")
    ),

    POSTGRESQL_DRIVER(
            "org{}postgresql",
            "postgresql",
            "9.4.1212",
            Relocation.of("postgresql", "org{}postgresql")
    ),

    MARIADB_DRIVER(
            "org{}mariadb{}jdbc",
            "mariadb-java-client",
            "2.7.0",
            Relocation.of("mariadb", "org{}mariadb")
    ),

    H2_DRIVER(
            "com.h2database",
            "h2",
            "1.4.199"
    ),

    SQLITE_DRIVER(
            "org.xerial",
            "sqlite-jdbc",
            "3.28.0"
    ),

    HIKARI(
            "com{}zaxxer",
            "HikariCP",
            "3.4.5",
            Relocation.of("hikari", "com{}zaxxer{}hikari")
    ),

    MONGODB_DRIVER(
            "org.mongodb",
            "mongo-java-driver",
            "3.12.2",
            Relocation.of("mongodb", "com{}mongodb"),
            Relocation.of("bson", "org{}bson")
    ),

    TOML4J(
            "com{}moandjiezana{}toml",
            "toml4j",
            "0.7.2",
            Relocation.of("toml4j", "com{}moandjiezana{}toml")
    ),

    PAPERLIB(
            "io{}papermc",
            "paperlib",
            "1.0.6",
            DependencyRepository.PAPER,
            Relocation.of("paperlib", "io{}papermc{}lib")
    ),

    /* Logging frameworks */

    SLF4J_API(
            "org.slf4j",
            "slf4j-api",
            "1.7.30"
    ),

    SLF4J_SIMPLE(
            "org.slf4j",
            "slf4j-simple",
            "1.7.30"
    );

    private final String mavenRepoPath;
    private final String version;
    private final List<Relocation> relocations;
    private final DependencyRepository repository;

    private static final String MAVEN_FORMAT = "%s/%s/%s/%s-%s.jar";

    Dependency(String groupId, String artifactId, String version) {
        this(groupId, artifactId, version, DependencyRepository.MAVEN_CENTRAL, new Relocation[0]);
    }

    Dependency(String groupId, String artifactId, String version, DependencyRepository repository) {
        this(groupId, artifactId, version, repository, new Relocation[0]);
    }

    Dependency(String groupId, String artifactId, String version, Relocation... relocations) {
        this(groupId, artifactId, version, DependencyRepository.MAVEN_CENTRAL, relocations);
    }

    Dependency(String groupId, String artifactId, String version, DependencyRepository repository, Relocation... relocations) {
        this.mavenRepoPath = String.format(MAVEN_FORMAT,
                rewriteEscaping(groupId).replace(".", "/"),
                rewriteEscaping(artifactId),
                version,
                rewriteEscaping(artifactId),
                version
        );
        this.version = version;
        this.relocations = ImmutableList.copyOf(relocations);
        this.repository = repository;
    }

    private static String rewriteEscaping(String s) {
        return s.replace("{}", ".");
    }

    public String getFileName() {
        return name().toLowerCase().replace('_', '-') + "-" + this.version;
    }

    String getMavenRepoPath() {
        return this.mavenRepoPath;
    }

    public List<Relocation> getRelocations() {
        return this.relocations;
    }

    public void download(Path path) throws DependencyDownloadException {
        repository.download(this, path);
    }

}
