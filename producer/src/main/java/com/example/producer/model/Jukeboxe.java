package com.example.producer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("jukeboxes")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Jukeboxe {

  @Id private Long id; // ID auto-incrémenté (clé primaire)

  @Column("serial_number")
  private String serialNumber;

  @Column("jukebox_id")
  private String jukeboxId;

  private String status = "ACTIVE";
  // Statut du jukebox (ACTIVE, INACTIVE, MAINTENANCE). Valeur par défaut = "ACTIVE"

  private String locationId;
  // Référence vers l’emplacement du jukebox (par son ID). Permet de savoir où se situe le jukebox physiquement.

  private Instant lastHeartbeat;
  // Date/heure du dernier signal reçu du jukebox. Sert à détecter les jukebox inactifs.

  private Instant createdAt = Instant.now();
  // Date de création de l’enregistrement. Initialisée automatiquement à l’instant de l’insertion.
}

