package me.vsamorokov.data.repository;

import me.vsamorokov.data.entity.LinkBetweenUrl;
import me.vsamorokov.data.entity.UrlRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LinkBetweenUrlRepository extends JpaRepository<LinkBetweenUrl, Long> {
    LinkBetweenUrl findByFromUrlAndToUrl(UrlRecord from, UrlRecord to);
    List<LinkBetweenUrl> findAllByToUrlAndFromUrlNot(UrlRecord to, UrlRecord from);
    long countAllByFromUrl(UrlRecord from);
}
