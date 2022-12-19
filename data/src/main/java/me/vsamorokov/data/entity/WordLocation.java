package me.vsamorokov.data.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;

@Entity
@Table(name = "word_location", indexes = @Index(name = "word_url_location_unique_idx", columnList = "word_id,url_id,location", unique = true))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WordLocation extends AbstractPersistable<Long> {

    @ManyToOne
    @JoinColumn(name = "word_id", nullable = false)
    private WordRecord wordRecord;

    @ManyToOne
    @JoinColumn(name = "url_id", nullable = false)
    private UrlRecord urlRecord;

    @Column(name = "location", nullable = false)
    private int location;
}
