package hexlet.code.Controllers;

import io.javalin.http.Handler;

public final class RootController {
    public static Handler welcome = ctx -> {
        ctx.render("main.html");
    };

}
