package com.example.consumer.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "jukebox")
@Data
public class JukeboxConfig {

    private String serialNumber = "ABC123470"; // valeur par défaut
    private String jukeboxId;

    private static final String DEFAULT_CONFIG_FILE = "classpath:jukebox.yml";

    public void save() {
        save(DEFAULT_CONFIG_FILE);
    }

    public void save(String filePath) {
        try {
            DumperOptions options = new DumperOptions();
            options.setPrettyFlow(true);
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            Yaml yaml = new Yaml(options);

            Map<String, Object> data = new HashMap<>();
            data.put("serialNumber", serialNumber);
            data.put("jukeboxId", jukeboxId);

            try (Writer writer = new FileWriter(filePath)) {
                yaml.dump(data, writer);
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de l'enregistrement du fichier " + filePath);
            e.printStackTrace();
        }
    }

    @PostConstruct
    public void load() {
        load(DEFAULT_CONFIG_FILE);
    }

    public void load(String filePath) {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            System.out.println("Fichier de configuration " + filePath + " non trouvé. Utilisation des valeurs par défaut.");
            return;
        }

        try (InputStream in = Files.newInputStream(path)) {
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(in);
            if (data != null) {
                this.serialNumber = (String) data.getOrDefault("serialNumber", serialNumber);
                this.jukeboxId = (String) data.get("jukeboxId");
                System.out.println("Configuration chargée depuis " + filePath);
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la lecture du fichier " + filePath);
            e.printStackTrace();
        }
    }
}
