package com.example.producer.service.nosql;

import com.example.common_lib.model.exception.NotFoundException;
import com.example.producer.model.Jukebox;
import com.example.producer.model.PlayRequest;
import com.example.producer.model.Track;
import com.example.producer.repo.JukeboxMongoRepo;
import com.example.producer.repo.PlayRequestRepository;
import com.example.producer.repo.TrackRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JukeboxMongoService {

  private final JukeboxMongoRepo jukeboxRepository;
  private final TrackRepository trackRepository;
  private final PlayRequestRepository playRequestRepository;
  private final MongoTemplate mongoTemplate;
  private final ObjectMapper objectMapper;

  public Jukebox createJukebox(Jukebox jukebox) {
    jukebox.setCreatedAt(Instant.now());
    return jukeboxRepository.save(jukebox);
  }

  public List<Jukebox> getJukeboxesPlayingTrack(String trackTitle) {
    Optional<Track> trackOpt = trackRepository.findByTitleIgnoreCase(trackTitle);
    if (trackOpt.isEmpty()) {
      throw new NotFoundException( trackTitle + " Non trouve ", "404", "");
    }

    Track track = trackOpt.get();
    List<String> statuses = List.of("PENDING", "QUEUED", "PLAYING");
    List<PlayRequest> playRequests =
        playRequestRepository.findByTrackIdAndStatusIn(track.getId(), statuses);

    List<String> jukeboxIds =
        playRequests.stream().map(PlayRequest::getJukeboxId).distinct().toList();

    return jukeboxRepository.findByIdIn(jukeboxIds);
  }

  /*public List<Jukebox> getJukeboxesPlayingTrack(String trackTitle) {
          // Étape 1 : retrouver la piste par son titre
          Optional<Track> trackOpt = trackRepository.findByTitle(trackTitle);
          if (trackOpt.isEmpty()) {
              return List.of(); // aucune piste trouvée
          }

          String trackId = trackOpt.get().getId();

          // Étape 2 : construire l'agrégation MongoDB
          MatchOperation matchTrackIdAndStatus = Aggregation.match(Criteria.where("trackId").is(trackId)
                  .and("status").in(List.of("PENDING", "QUEUED", "PLAYING")));

          GroupOperation groupByJukebox = Aggregation.group("jukeboxId");

          LookupOperation lookupJukebox = Aggregation.lookup("jukeboxes", "_id", "jukeboxId", "jukebox");

          Aggregation aggregation = Aggregation.newAggregation(
                  matchTrackIdAndStatus,
                  groupByJukebox
          );

          AggregationResults<Document> results = mongoTemplate.aggregate(
                  aggregation,
                  "play_requests",
                  Document.class
          );

          List<String> jukeboxIds = results.getMappedResults().stream()
                  .map(doc -> doc.getString("_id"))
                  .toList();

          if (jukeboxIds.isEmpty()) {
              return List.of();
          }

          // Étape 3 : retourner les jukeboxes correspondants
          return jukeboxRepository.findByIdIn(jukeboxIds);
      }
  */

  /* public List<Jukebox> getJukeboxesPlayingTrack(String trackTitle) {
          // Étape 1 : retrouver la piste par son titre
          Optional<Track> trackOpt = trackRepository.findByTitle(trackTitle);
          if (trackOpt.isEmpty()) {
              return List.of(); // aucune piste trouvée
          }

          String trackId = trackOpt.get().getId();

          // Étape 2 : pipeline d'agrégation avec lookup pour obtenir directement les jukebox
          MatchOperation matchTrack = Aggregation.match(
                  Criteria.where("trackId").is(trackId)
                          .and("status").in(List.of("PENDING", "QUEUED", "PLAYING"))
          );

          LookupOperation lookupJukebox = Aggregation.lookup(
                  "jukeboxes",      // collection cible
                  "jukeboxId",      // champ dans play_requests
                  "jukeboxId",      // champ dans jukeboxes
                  "jukeboxData"     // alias du résultat
          );

          UnwindOperation unwind = Aggregation.unwind("jukeboxData");

          // Optionnel : filtrer pour ne garder que les jukebox actifs
          MatchOperation matchActiveJukebox = Aggregation.match(
                  Criteria.where("jukeboxData.status").is("ACTIVE")
          );

          // Étape 3 : projection du résultat final
          ProjectionOperation projectJukebox = Aggregation.project("jukeboxData");

          Aggregation aggregation = Aggregation.newAggregation(
                  matchTrack,
                  lookupJukebox,
                  unwind,
                  matchActiveJukebox,
                  projectJukebox
          );

          AggregationResults<Document> results = mongoTemplate.aggregate(
                  aggregation,
                  "play_requests",
                  Document.class
          );

          // Transformer les résultats en Jukebox
          List<Jukebox> jukeboxes = results.getMappedResults().stream()
                  .map(doc -> {
                      Document jb = (Document) doc.get("jukeboxData");
                      Jukebox jukebox = new Jukebox();
                      jukebox.setId(jb.getString("_id"));
                      jukebox.setSerialNumber(jb.getString("serialNumber"));
                      jukebox.setJukeboxId(jb.getString("jukeboxId"));
                      jukebox.setStatus(jb.getString("status"));
                      jukebox.setLocationId(jb.getString("locationId"));
                      if (jb.get("lastHeartbeat") != null) {
                          jukebox.setLastHeartbeat(jb.getDate("lastHeartbeat").toInstant());
                      }
                      if (jb.get("createdAt") != null) {
                          jukebox.setCreatedAt(jb.getDate("createdAt").toInstant());
                      }
                      return jukebox;
                  })
                  .toList();

          return jukeboxes;
      }
  */
}
