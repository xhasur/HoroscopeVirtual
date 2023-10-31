package horoscope.handler;

import horoscope.model.Signs;
import horoscope.persistence.HoroscopeRepository;
import io.vertx.core.json.Json;
import io.vertx.rxjava3.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

//other imports
@Slf4j
public class HoroscopeHandler {
  HoroscopeRepository horoscope;
  private HoroscopeHandler(HoroscopeRepository _posts) {
    this.horoscope = _posts;
  }
  public static HoroscopeHandler create(HoroscopeRepository posts) {
    return new HoroscopeHandler(posts);
  }
  public void all(RoutingContext routingContext) {
    this.horoscope.findAllSigns()
      .takeLast(10).toList()
      .subscribe(data -> routingContext.response().end(Json.encode(data)));
  }

  public void get(RoutingContext routingContext) {
    String idSign = routingContext.pathParam("id");
    this.horoscope
      .findById(Integer.parseInt(idSign))
      .subscribe(
        post -> routingContext.response().end(Json.encode(post)),
        err -> routingContext.fail(404, err)
      );
  }

  public void save(RoutingContext routingContext) {
    var body = routingContext.getBodyAsJson();
    var form  = body.mapTo(Signs.class);
    this.horoscope.save(form)
      .subscribe(savedId -> routingContext.response()
        .putHeader("Location", "/signs/" + savedId)
        .setStatusCode(201)
        .end()
      );
  }

  public void delete(RoutingContext routingContext) {
    String idSign = routingContext.pathParam("id");
    this.horoscope
      .delete(Integer.parseInt(idSign))
      .subscribe(
        post -> routingContext.response().setStatusCode(204).end(idSign),
        err -> routingContext.fail(404, err)
      );
  }

  public void defaultMethod(RoutingContext routingContext) {
    routingContext.response().end("Router not found");
  }


}
