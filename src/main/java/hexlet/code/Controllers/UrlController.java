package hexlet.code.Controllers;

import io.javalin.http.Handler;

public final class UrlController {
    public static Handler wtf = ctx -> {
        ctx.render("main.html");
    };
}
