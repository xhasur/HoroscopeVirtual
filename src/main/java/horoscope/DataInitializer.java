package horoscope;

import io.vertx.rxjava3.pgclient.PgPool;
import io.vertx.rxjava3.sqlclient.Row;
import io.vertx.rxjava3.sqlclient.RowSet;
import io.vertx.rxjava3.sqlclient.SqlConnection;
import io.vertx.rxjava3.sqlclient.Tuple;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DataInitializer {
  private PgPool client;

  public DataInitializer(PgPool client) {
    this.client = client;
  }

  public static DataInitializer create(PgPool client) {
    return new DataInitializer(client);
  }

  public void run() {
    System.out.println("Data initialization is starting...");
    List<Tuple> batch = new ArrayList<>();
    batch.add(Tuple.of("Aries", "03/21", "04/19", "Competitive"));
    batch.add(Tuple.of("Taurus", "04/20", "05/20", "Loyal"));
    client
      .rxWithTransaction((SqlConnection tx) -> tx.query("DELETE FROM sign").rxExecute()
        .flatMap(resul -> tx.preparedQuery("INSERT INTO sign (name, since,until,characteristics) VALUES ($1, $2,$3,$4)").rxExecuteBatch(batch))
        .toMaybe()
      ).flatMapSingle(data -> client.query("SELECT * FROM sign").rxExecute())
      .subscribe((RowSet<Row> data) ->
          System.out.println("saved row: {} ," + data.toString()),
        err -> {
          System.out.println("failed to initializing: ," + err.getMessage());
        });
  }
}

