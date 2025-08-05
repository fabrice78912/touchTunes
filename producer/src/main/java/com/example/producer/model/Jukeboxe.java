package com.example.producer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

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
}
