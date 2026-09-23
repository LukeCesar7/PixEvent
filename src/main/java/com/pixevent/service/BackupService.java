package com.pixevent.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Adaptação de src/utils/backup.js. O original copiava o arquivo .db do SQLite;
 * como o banco agora é PostgreSQL, o backup passa a ser feito via `pg_dump`
 * (precisa estar instalado no servidor/imagem). Se preferir, desative com
 * BACKUP_ON_START=false e use o backup gerenciado do seu provedor de Postgres
 * (RDS snapshot, Neon/Supabase backup automático etc.) em vez deste utilitário.
 */
@Service
public class BackupService {

    private static final Logger log = LoggerFactory.getLogger(BackupService.class);
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss");

    @Value("${app.backup.on-start:${BACKUP_ON_START:true}}")
    private boolean backupOnStart;

    @Value("${app.backup.dir:${BACKUP_DIR:./backups}}")
    private String backupDir;

    @Value("${app.backup.keep:${BACKUP_KEEP:30}}")
    private int backupKeep;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @PostConstruct
    public void onStart() {
        if (!backupOnStart) return;
        backup("start");
    }

    public String backup(String reason) {
        try {
            Path dir = Path.of(backupDir);
            Files.createDirectories(dir);

            String stamp = OffsetDateTime.now().format(STAMP);
            String safeReason = (reason == null ? "manual" : reason).replaceAll("[^a-zA-Z0-9_-]", "-");
            Path dest = dir.resolve("pixevent-" + stamp + "-" + safeReason + ".sql");

            // jdbc:postgresql://host:port/db -> extrai host, port e db para o pg_dump
            String semProtocolo = dbUrl.replace("jdbc:postgresql://", "");
            String hostPort = semProtocolo.substring(0, semProtocolo.indexOf('/'));
            String database = semProtocolo.substring(semProtocolo.indexOf('/') + 1);
            String host = hostPort.contains(":") ? hostPort.split(":")[0] : hostPort;
            String port = hostPort.contains(":") ? hostPort.split(":")[1] : "5432";

            ProcessBuilder pb = new ProcessBuilder(
                    "pg_dump", "-h", host, "-p", port, "-U", dbUser, "-F", "c", "-f", dest.toString(), database
            );
            pb.environment().put("PGPASSWORD", dbPassword == null ? "" : dbPassword);
            pb.redirectErrorStream(true);

            Process process = pb.start();
            boolean finished = process.waitFor(2, java.util.concurrent.TimeUnit.MINUTES);
            if (!finished || process.exitValue() != 0) {
                log.warn("pg_dump não concluiu com sucesso (verifique se está instalado e no PATH).");
                return null;
            }

            cleanupOldBackups(dir);
            log.info("Backup do banco criado: {}", dest);
            return dest.toString();
        } catch (Exception e) {
            log.warn("Backup ignorado: {}", e.getMessage());
            return null;
        }
    }

    private void cleanupOldBackups(Path dir) throws Exception {
        if (backupKeep <= 0) return;
        try (Stream<Path> files = Files.list(dir)) {
            List<Path> ordenados = files
                    .filter(p -> p.getFileName().toString().startsWith("pixevent-"))
                    .sorted(Comparator.comparingLong(p -> -p.toFile().lastModified()))
                    .toList();
            for (int i = backupKeep; i < ordenados.size(); i++) {
                Files.deleteIfExists(ordenados.get(i));
            }
        }
    }
}
