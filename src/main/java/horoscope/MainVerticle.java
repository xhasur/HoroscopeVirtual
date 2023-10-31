package horoscope;


import horoscope.handler.HoroscopeHandler;
import horoscope.persistence.HoroscopeRepository;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.core.http.HttpMethod;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.rxjava3.core.AbstractVerticle;
import io.vertx.rxjava3.core.http.HttpServer;
import io.vertx.rxjava3.ext.web.Route;
import io.vertx.rxjava3.ext.web.Router;

import io.vertx.rxjava3.ext.web.handler.BodyHandler;
import io.vertx.rxjava3.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class MainVerticle extends AbstractVerticle {

  @Override
  public Completable rxStart() {
    log.info("Starting HTTP server...");
    HttpServer server = vertx.createHttpServer();
    InternalLoggerFactory.setDefaultFactory(Slf4JLoggerFactory.INSTANCE);

    var pgPool = pgPool();
    var hotelRepository = HoroscopeRepository.create(pgPool);
    var hotelHandler = HoroscopeHandler.create(hotelRepository);
    // Initializing the sample data
    //var initializer = DataInitializer.create(pgPool);
    //initializer.run();

    var router = routes(hotelHandler);
    return
      server.requestHandler(router)
      .rxListen(8888)
      .ignoreElement();
  }

  private PgPool pgPool() {
    PgConnectOptions connectOptions = new PgConnectOptions()
      .setPort(5432)
      .setHost("localhost")
      .setDatabase("zodiacaldb")
      .setUser("postgres")
      .setPassword("postgres");

    PoolOptions poolOptions = new PoolOptions().setMaxSize(5);
      return PgPool.pool(vertx, connectOptions, poolOptions);
  }

  private Router routes(HoroscopeHandler handlers) {
    Router router = Router.router(vertx);

    Route userGetRoute = router.get("/signs/");
    userGetRoute.produces("application/json");
    userGetRoute.handler(handlers::all);

    Route userSaveRoute = router.route("/signs/");
    userSaveRoute.method(HttpMethod.POST);
    userSaveRoute.handler(BodyHandler.create());
    userSaveRoute.handler(handlers::save);

    Route userDeleteRoute = router.route("/signs/:id");
    userDeleteRoute.method(HttpMethod.DELETE);
    userDeleteRoute.handler(handlers::delete);


    Route userGetIdRoute = router.get("/signs/:id");
    userGetIdRoute.produces("application/json");
    userGetIdRoute .handler(handlers::get).failureHandler(frc -> frc.response().setStatusCode(404).end());

    Route routeByDefault = router.routeWithRegex(".*");
    routeByDefault.last();
    routeByDefault.handler(handlers::defaultMethod);

    return router;
  }


}

