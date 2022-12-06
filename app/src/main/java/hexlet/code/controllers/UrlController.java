package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;
import hexlet.code.domain.query.QUrlCheck;
import io.ebean.PagedList;
import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class UrlController {
    public static Handler addUrl = ctx -> {
        // get input as url
        String formParam = ctx.formParam("url");
        URL formedUrl;

        try {
            formedUrl = new URL(formParam);
        } catch (Exception e) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect("/");
            return;
        }

        String normalizedUrl = formedUrl.getProtocol() + "://" + formedUrl.getAuthority();

        if (formedUrl == null) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect("/");
            return;
        }

        Url url = new QUrl()
                .name.equalTo(normalizedUrl)
                .findOne();
        if (url != null) {
            ctx.sessionAttribute("flash", "Страница уже существует");
            ctx.sessionAttribute("flash-type", "info");
            ctx.redirect("/urls");
            return;
        }

        Url newUrl = new Url(normalizedUrl);
        newUrl.save();
        ctx.sessionAttribute("flash", "Страница успешно добавлена");
        ctx.sessionAttribute("flash-type", "success");
        ctx.redirect("/urls");
        return;
    };

    public static Handler listUrls = ctx -> {
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1) - 1;
        int rowsPerPage = 10;
        QUrlCheck qUrlCheck = QUrlCheck.alias();
        PagedList<Url> pagedUrls = new QUrl()
                .orderBy().id.asc()
                .urlChecks.fetch(qUrlCheck.createdAt, qUrlCheck.statusCode)
                .orderBy().urlChecks.id.desc()
                .setFirstRow(page * rowsPerPage)
                .setMaxRows(rowsPerPage)
                .findPagedList();

        List<Url> urls = pagedUrls.getList();

        int lastPage = pagedUrls.getTotalPageCount() + 1;
        int currentPage = pagedUrls.getPageIndex() + 1;
        List<Integer> pages = IntStream
                .range(1, lastPage)
                .boxed()
                .collect(Collectors.toList());

        ctx.attribute("urls", urls);
        ctx.attribute("currentPage", currentPage);
        ctx.attribute("pages", pages);
        ctx.render("urls/index.html");
    };

    public static Handler showUrl = ctx -> {
        int id = ctx.pathParamAsClass("id", Integer.class).getOrDefault(null);
        Url url = new QUrl().urlChecks.fetch()
                .id.equalTo(id)
                .findOne();
        if (url == null) {
            throw new NotFoundResponse();
        }
        List<UrlCheck> checks = new QUrlCheck().url.equalTo(url)
                .orderBy().id.desc()
                .findList();
        ctx.attribute("url", url);
        ctx.attribute("checks", checks);
        ctx.render("urls/showUrl.html");
    };

    public static Handler addCheck = ctx -> {
        long id = ctx.pathParamAsClass("id", Integer.class).getOrDefault(null);
        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();
        String name = url.getName();
        try {
            HttpResponse<String> responseGet = Unirest.get(name).asString();

            int statusCode = responseGet.getStatus();

            Document document = Jsoup.parse(responseGet.getBody());

            String title = document.title();

            String description = null;

            if (document.selectFirst("meta[name=description]") != null) {
                description = document.selectFirst("meta[name=description]").attr("content");
            }

            String h1 = null;

            if (document.selectFirst("h1") != null) {
                h1 = document.selectFirst("h1").text();
            }

            UrlCheck urlCheck = new UrlCheck(statusCode, title, h1, description, url);
            urlCheck.save();

            ctx.sessionAttribute("flash", "Страница успешно проверена");
            ctx.sessionAttribute("flash-type", "success");
        } catch (UnirestException exception) {
            ctx.sessionAttribute("flash", "Не удалось проверить страницу");
            ctx.sessionAttribute("flash-type", "danger");
        }
        ctx.redirect("/urls/" + id);
    };


}
