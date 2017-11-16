package com.energycollector.persistence;

import akka.NotUsed;
import akka.stream.Materializer;
import akka.stream.javadsl.Source;
import com.energycollector.Consumption;
import com.lightbend.lagom.javadsl.persistence.ReadSide;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;
import org.pcollections.PVector;
import org.pcollections.TreePVector;

import javax.inject.Inject;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.concurrent.CompletionStage;

/**
 * Provides access to the readside.
 */
public final class CounterRepository {

    private final CassandraSession session;
    private final Materializer materializer;

    @Inject
    public CounterRepository(CassandraSession session, Materializer materializer, ReadSide readSide) {
        this.session = session;
        this.materializer = materializer;
        readSide.register(EnergyCollectorEventProcessor.class);
    }

    public CompletionStage<PVector<Consumption>> consumptionByPeriod(int duration) {
        Source<Consumption, NotUsed> consumptionSource = session.select("SELECT * FROM VILLAGE_COUNTERS")
                .mapAsync(2, row -> {
                    Date dateDuration = Date.from(Instant.now().minus(duration, ChronoUnit.HOURS));
                    String sumConsumptionQuery = "SELECT SUM(consumptionAmount) FROM village_consumptions WHERE counterId = ? AND updatetime > ?";
                    return session.selectAll(sumConsumptionQuery, row.getLong("counterId"), dateDuration)
                            .thenApply(rows ->
                                    Consumption.builder().villageName(row.getString("villageName"))
                                            .consumption(rows.get(0).getDouble(0)).build()
                            );

                });

        PVector<Consumption> zero = TreePVector.empty();
        return consumptionSource.runFold(zero, (acc, elem) -> acc.plus(elem), materializer);
    }
}
