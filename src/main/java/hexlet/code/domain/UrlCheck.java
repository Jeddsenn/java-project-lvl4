package hexlet.code.domain;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;

import javax.persistence.*;
import java.time.Instant;

@Entity
public final class UrlCheck extends Model {

    @Id
    private long id;

    private String title;

    private String h1;

    @Lob
    private String description;

    @WhenCreated
    private Instant createdAt;

    private Long statusCode;

    @JoinColumn(name = "url_id")
    @ManyToOne
    private Url url;

    public UrlCheck(long statusCode, String title, String h1, String description, Url url) {
        this.statusCode = statusCode;
        this.title = title;
        this.h1 = h1;
        this.description = description;
        this.url = url;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getH1() {
        return h1;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Long getStatusCode() {
        return statusCode;
    }

    public Url getUrl() {
        return url;
    }
}
