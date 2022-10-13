package hexlet.code;

import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import io.javalin.Javalin;
import io.ebean.DB;
import io.ebean.Database;

class AppTest {

    private static Javalin app;
    private static String baseUrl;
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

        @Test
        void testAddUrl() {
            String input = "https://www.rambler.ru";
            HttpResponse<String> responsePost = Unirest
                    .post(baseUrl + "/urls")
                    .field("url", input)
                    .asEmpty();

            assertThat(responsePost.getStatus()).isEqualTo(302);
            assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/urls");

            HttpResponse<String> response = Unirest
                    .get(baseUrl + "/urls")
                    .asString();
            String body = response.getBody();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(body).contains(input);
            assertThat(body).contains("Страница успешно добавлена");

            Url actualUrl = new QUrl()
                    .name.equalTo(input)
                    .findOne();

            assertThat(actualUrl).isNotNull();
            assertThat(actualUrl.getName()).isEqualTo(input);
        }

        @Test
        void testCreateExistingUrl() {
            String inputName = "https://www.example.com";
            HttpResponse<String> responsePost1 = Unirest
                    .post(baseUrl + "/urls")
                    .field("url", inputName)
                    .asEmpty();

            assertThat(responsePost1.getHeaders().getFirst("Location")).isEqualTo("/urls");

            HttpResponse<String> response = Unirest
                    .get(baseUrl + "/urls")
                    .asString();
            String body = response.getBody();

            assertThat(body).contains(inputName);
            assertThat(body).contains("Страница уже существует");
        }

        @Test
        void testCreateBadUrl() {
            String inputName = "1231241624656tusdgsdfn5a35";
            HttpResponse<String> responsePost1 = Unirest
                    .post(baseUrl + "/urls")
                    .field("url", inputName)
                    .asEmpty();

            assertThat(responsePost1.getHeaders().getFirst("Location")).isEqualTo("/");

            HttpResponse<String> response = Unirest
                    .get(baseUrl + "/")
                    .asString();
            String body = response.getBody();

            assertThat(body).contains("Некорректный URL");
        }
    }
}
