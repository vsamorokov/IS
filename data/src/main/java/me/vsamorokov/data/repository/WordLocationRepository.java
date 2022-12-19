package me.vsamorokov.data.repository;

import me.vsamorokov.data.entity.UrlRecord;
import me.vsamorokov.data.entity.WordLocation;
import me.vsamorokov.data.entity.WordRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WordLocationRepository extends JpaRepository<WordLocation, Long> {
    List<WordLocation> findByWordRecord(WordRecord wordRecord);
    WordLocation findByWordRecordAndUrlRecordAndLocation(WordRecord wordRecord, UrlRecord urlRecord, int location);

    List<WordLocation> findAllByUrlRecordOrderByLocationAsc(UrlRecord urlRecord);
}
