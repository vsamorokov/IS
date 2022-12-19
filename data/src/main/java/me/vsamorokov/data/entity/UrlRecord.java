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
@Table(name = "url_list", indexes = @Index(name = "url_unique_idx", columnList = "url", unique = true))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UrlRecord extends AbstractPersistable<Long> {

    @Column(name = "url", length = 2048, nullable = false)
    private String url;

    @Column(name = "crawled")
    private boolean crawled;

    @Column(name = "page_rank")
    private double pageRank;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        UrlRecord urlRecord = (UrlRecord) o;
        return getId() != null && Objects.equals(getId(), urlRecord.getId());
    }

    @Override
    public int hashCode() {
        return getId() == null ? 0 : getId().hashCode();
    }

    @Override
    public String toString() {
        return Objects.requireNonNull(getId()).toString();
    }
}
