package com.example.producer.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrackRequestDto {
  private String title;
  private String artist;
  private int durationSeconds;
  private String genre;
}
