package me.vsamorokov.data.repository;

import me.vsamorokov.data.entity.WordRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WordRecordRepository extends JpaRepository<WordRecord, Long> {
    WordRecord findByWord(String word);
}
