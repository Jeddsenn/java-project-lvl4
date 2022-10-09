package hexlet.code;

import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import io.ebean.DB;
import io.ebean.Database;
import io.javalin.Javalin;
import kong.unirest.Unirest;
import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.assertThat;
import kong.unirest.HttpResponse;

class AppTest {

    private static Javalin app;
    private static String baseUrl;
    private static Url existingUrl;
    private static Database database;


    @BeforeAll
    public static void beforeAll() {
        app = App.getApp();
        app.start(0);
        int port = app.port();
        baseUrl = "http://localhost:" + port;
        database = DB.getDefault();
    }

    @AfterAll
    public static void afterAll() {
        app.stop();
    }

    @BeforeEach
    void beforeEach() {
        database.script().run("/truncate.sql");
        database.script().run("/seed-test-db.sql");
    }

    @Test
    void testInit() {
        assertThat(true).isEqualTo(true);
    }

    @Nested
    class RootTest {
        @Test
        void testMain() {
            HttpResponse<String> response = Unirest.get(baseUrl).asString();
            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.getBody()).contains("Анализатор страниц");
        }
    }

    @Nested
    class UrlTest {

        @Test
        void testAddUrl() {
            String inputUrl = "https://www.cia.edu";
            HttpResponse<String> responsePost = Unirest
                    .post(baseUrl + "/urls")
                    .field("url", inputUrl)
                    .asEmpty();

            assertThat(responsePost.getStatus()).isEqualTo(302);

            Url dbUrl = new QUrl()
                    .name.equalTo(inputUrl)
                    .findOne();

            assertThat(dbUrl).isNotNull();
            assertThat(dbUrl.getName()).isEqualTo(inputUrl);
        }

        @Test
        void testListUrls() {
            HttpResponse<String> response = Unirest
                    .get(baseUrl + "/urls")
                    .asString();
            String body = response.getBody();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(body).contains("https://www.example.com");
            assertThat(body).contains("https://www.hexlet.io");
            assertThat(body).contains("https://www.google.ru");
        }

        @Test
        void testShowUrl() {
            HttpResponse<String> response = Unirest
                    .get(baseUrl + "/urls/1")
                    .asString();
            String body = response.getBody();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(body).contains("https://www.example.com");
        }



    }
}