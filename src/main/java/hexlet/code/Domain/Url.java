package hexlet.code.Domain;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Url extends Model {

    @Id
    private long id;

    private String name;

    @WhenCreated
    private String createdAt;

}
