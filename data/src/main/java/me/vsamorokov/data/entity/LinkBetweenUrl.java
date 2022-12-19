package me.vsamorokov.data.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "link_between_url", indexes = @Index(name = "from_to_unique_idx", columnList = "from_url_id,to_url_id", unique = true))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LinkBetweenUrl extends AbstractPersistable<Long> {

    @ManyToOne
    @JoinColumn(name = "from_url_id", nullable = false)
    private UrlRecord fromUrl;

    @ManyToOne
    @JoinColumn(name = "to_url_id", nullable = false)
    private UrlRecord toUrl;
}
