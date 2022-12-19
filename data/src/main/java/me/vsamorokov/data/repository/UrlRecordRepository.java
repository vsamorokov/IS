package me.vsamorokov.data.repository;

import me.vsamorokov.data.entity.UrlRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UrlRecordRepository extends JpaRepository<UrlRecord, Long> {
    UrlRecord findByUrl(String url);
    UrlRecord findByUrlAndCrawledIsFalse(String url);

    List<UrlRecord> findAllByCrawledIsTrue();

    @Query("select max(pageRank) from UrlRecord")
    Double findMaxPageRank();
}
