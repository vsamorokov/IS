package me.vsamorokov.data.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;

@Entity
@Table(name = "link_word", indexes = @Index(name = "word_link_unique_idx", columnList = "word_id,link_id", unique = true))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LinkWord extends AbstractPersistable<Long> {

    @ManyToOne
    @JoinColumn(name = "word_id", nullable = false)
    private WordRecord wordRecord;

    @ManyToOne
    @JoinColumn(name = "link_id", nullable = false)
    private LinkBetweenUrl linkBetweenUrl;
}
