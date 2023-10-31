package horoscope.persistence;

import horoscope.model.Signs;
import io.reactivex.rxjava3.core.Flowable;


import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.pgclient.PgPool;
import io.vertx.rxjava3.sqlclient.Row;
import io.vertx.rxjava3.sqlclient.RowSet;
import io.vertx.rxjava3.sqlclient.SqlResult;
import io.vertx.rxjava3.sqlclient.Tuple;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Flow;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


@Slf4j
public class HoroscopeRepository {
  private final PgPool client;

  private HoroscopeRepository(PgPool client) {
    this.client = client;
  }

  //factory method
  public static HoroscopeRepository create(PgPool client) {
    return new HoroscopeRepository(client);
  }

  private static Function<Row, Signs> MAPPER = (row) ->
     Signs.of(
      row.getInteger("id"),
      row.getString("name"),
      row.getString("since"),
      row.getString("until"),
      row.getString("characteristics"));

  public Flowable<Signs> findAllSigns() {
    return this.client
      .query("SELECT * FROM sign ")
      .rxExecute()
      .flattenAsFlowable(rows -> StreamSupport.stream(rows.spliterator(), false).map(MAPPER).collect(Collectors.toList()));
  }
  public Single<Signs> findById(Integer idSign) {
    return client
      .preparedQuery("SELECT * FROM sign where id=$1")
      .rxExecute(Tuple.of(idSign))
      .map(RowSet::iterator)
      .flatMap(iterator -> Single.just(MAPPER.apply(iterator.next())));
  }

  public Single<Integer> save(Signs sign) {
    return client.preparedQuery("INSERT INTO sign(name, since , until , characteristics) VALUES ($1, $2,$3,$4) RETURNING (id)")
      .rxExecute(Tuple.of(sign.getName(), sign.getSince() , sign.getUntil() , sign.getCharacteristics()))
      .map(rs -> rs.iterator().next().getInteger("id"));
  }

  public Single<Integer> delete(int idSign) {
    return client
      .preparedQuery("DELETE FROM sign where id=$1")
      .rxExecute(Tuple.of(idSign))
      .map(SqlResult::rowCount);
  }
}
