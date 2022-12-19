package me.vsamorokov.data.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "word_list", indexes = @Index(name = "word_unique_idx", columnList = "word", unique = true))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WordRecord extends AbstractPersistable<Long> {

    @Column(name = "word", length = 2048, nullable = false, unique = true)
    private String word;

    @Column(name = "filtered")
    private boolean filtered;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        WordRecord that = (WordRecord) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return getId() == null ? 0 : getId().hashCode();
    }
}
