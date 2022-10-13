package hexlet.code.Controllers;

import hexlet.code.UrlParser;
import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;
import hexlet.code.domain.query.QUrlCheck;
import io.ebean.PagedList;
import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;

import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class UrlController {
    public static Handler addUrl = ctx -> {
        // get input as url
        String formParam = ctx.formParam("url");
        URL formedUrl = new URL(formParam);
        String validUrl = formedUrl.getProtocol() + "://" + formedUrl.getAuthority();

        if (formedUrl == null) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect("/");
            return;
        }

        Url url = new QUrl()
                .name.equalTo(validUrl)
                .findOne();
        if (url != null) {
            ctx.sessionAttribute("flash", "Страница уже существует");
            ctx.sessionAttribute("flash-type", "info");
            ctx.redirect("/urls");
            return;
        }

        Url newUrl = new Url(validUrl);
        newUrl.save();
        ctx.sessionAttribute("flash", "Страница успешно добавлена");
        ctx.sessionAttribute("flash-type", "success");
        ctx.redirect("/urls");
        return;
    };

    public static Handler listUrls = ctx -> {
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1) - 1;
        int rowsPerPage = 10;
        PagedList<Url> pagedUrls = new QUrl()
                .setFirstRow(page * rowsPerPage)
                .setMaxRows(rowsPerPage)
                .orderBy()
                .id.asc()
                .findPagedList();
        List<Url> urls = pagedUrls.getList();
        int lastPage = pagedUrls.getTotalPageCount() + 1;
        int currentPage = pagedUrls.getPageIndex() + 1;
        List<Integer> pages = IntStream
                .range(1, lastPage)
                .boxed()
                .collect(Collectors.toList());
        ctx.attribute("urls", urls);
        ctx.attribute("pages", pages);
        ctx.attribute("currentPage", currentPage);
        ctx.render("listUrls.html");
    };

    public static Handler showUrl = ctx -> {
        int id = ctx.pathParamAsClass("id", Integer.class).getOrDefault(null);
        Url url = new QUrl()
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
        ctx.render("showUrl.html");
    };

    public static Handler addCheck = ctx -> {
        int id = ctx.pathParamAsClass("id", Integer.class).getOrDefault(null);
        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();
        if (url == null) {
            throw new NotFoundResponse();
        }
        UrlCheck check = UrlParser.checkUrl(url);

        if (check != null) {
            check.save();
            ctx.sessionAttribute("flash", "Страница успешно проверена");
            ctx.sessionAttribute("flash-type", "success");
        } else {
            ctx.sessionAttribute("flash", "Проверка не удалась");
            ctx.sessionAttribute("flash-type", "danger");
        }
        ctx.redirect("/urls/" + url.getId());
    };

}
