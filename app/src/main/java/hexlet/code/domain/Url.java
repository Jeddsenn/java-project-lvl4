package hexlet.code.domain;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Entity
public class Url extends Model {

    @Id
    @GeneratedValue
    private long id;


    private String name;

    @WhenCreated
    private Instant createdAt;

    @OneToMany(cascade = CascadeType.ALL)
    private List<UrlCheck> urlChecks;


    public Url(String name) {
        this.name = name;
        urlChecks = new ArrayList<>();
    }

    public final long getId() {
        return id;
    }

    public final void setId(long id) {
        this.id = id;
    }

    public final String getName() {
        return name;
    }

    public final void setName(String name) {
        this.name = name;
    }

    public final Instant getCreatedAt() {
        return createdAt;
    }

    public final List<UrlCheck> getUrlChecks() {
        return urlChecks;
    }

    public final UrlCheck getLastUrlCheck() {
        return urlChecks.stream()
                .max(Comparator.comparing(x -> x.getCreatedAt()))
                .orElse(null);
    }

}
