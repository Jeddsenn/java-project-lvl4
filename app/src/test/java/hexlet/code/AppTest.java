package hexlet.code;

import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static java.nio.file.Files.readString;
import static org.assertj.core.api.Assertions.assertThat;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import io.javalin.Javalin;
import io.ebean.DB;
import io.ebean.Database;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

class AppTest {

    private static Javalin app;
    private static String baseUrl;
    private static Database database;
    private static MockWebServer mockWebServer;


    @BeforeAll
    public static void beforeAll() throws IOException {
        app = App.getApp();
        app.start(0);
        int port = app.port();
        baseUrl = "http://localhost:" + port;
        database = DB.getDefault();

        mockWebServer = new MockWebServer();
        var resp = readString(Paths.get("src", "test", "resources", "mock.html"));
        mockWebServer.enqueue(new MockResponse().setBody(resp));
        mockWebServer.start();
    }

    @AfterAll
    public static void afterAll() throws IOException {
        app.stop();
        mockWebServer.shutdown();
    }

    @BeforeEach
    void beforeEach() {
        database.script().run("/truncate.sql");
        database.script().run("/seed-test-db.sql");
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
            String inputName = "asdfasdfasdfasdf";
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

        @Test
        public void testCheckUrl() throws Exception {
            String mockHTML = readString(Paths.get("src/test/resources/mock.html"), StandardCharsets.US_ASCII);
            MockWebServer server = new MockWebServer();
            server.enqueue(new MockResponse().setBody(mockHTML));
            server.start();

            String mockUrl = server.url("/").toString();
            HttpResponse<String> response = Unirest.post(baseUrl + "/urls")
                    .field("url", mockUrl)
                    .asEmpty();

            Url url = new QUrl().name.equalTo(mockUrl.substring(0, mockUrl.length() - 1))
                    .findOne();

            HttpResponse<String> responseCheck = Unirest.post(baseUrl + "/urls/" + url.getId() + "/checks")
                    .asEmpty();
            HttpResponse<String> responseShow = Unirest.get(baseUrl + "/urls/" + url.getId()).asString();
            server.shutdown();

            assertThat(responseShow.getStatus()).isEqualTo(200);
            assertThat(responseShow.getBody()).contains("Title");
            assertThat(responseShow.getBody()).contains("Lorem Ipsum");
            assertThat(responseShow.getBody()).contains("CONTENT");

        }


    }
}
