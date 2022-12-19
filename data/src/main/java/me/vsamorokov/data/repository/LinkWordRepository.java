package me.vsamorokov.data.repository;

import me.vsamorokov.data.entity.LinkBetweenUrl;
import me.vsamorokov.data.entity.LinkWord;
import me.vsamorokov.data.entity.WordRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LinkWordRepository extends JpaRepository<LinkWord, Long> {
    LinkWord findByWordRecordAndLinkBetweenUrl(WordRecord wordRecord, LinkBetweenUrl linkBetweenUrl);
}
