package com.energycollector.persistence;

import akka.Done;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.ReadSideProcessor;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.sql.Date;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide.completedStatement;

/**
 * This class is responsible for storing events for the readside in differents tables.
 */
final class EnergyCollectorEventProcessor extends ReadSideProcessor<AbstractEnergyCollectorEvent> {

    private final Logger log = LoggerFactory.getLogger(EnergyCollectorEventProcessor.class);

    private final CassandraSession session;
    private final CassandraReadSide readSide;

    private PreparedStatement writeConsumption = null;
    private PreparedStatement writeCounter = null;

    @Inject
    EnergyCollectorEventProcessor(CassandraSession session, CassandraReadSide readSide) {
        this.session = session;
        this.readSide = readSide;
    }

    @Override
    public String readSideName() {
        return "EnergyCollectorEventProcessor";
    }

    @Override
    public ReadSideHandler<AbstractEnergyCollectorEvent> buildHandler() {
        return readSide.<AbstractEnergyCollectorEvent>builder("EnergyCollectorEventHandler")
                .setGlobalPrepare(this::prepareCreateTables)
                .setPrepare((ignored) -> prepareStatements())
                .setEventHandler(ConsumptionUpdated.class, this::processConsumptionUpdated)
                .setEventHandler(CallbackRegistered.class, this::processCounterRegistered)
                .build();
    }

    @Override
    public PSequence<AggregateEventTag<AbstractEnergyCollectorEvent>> aggregateTags() {
        return TreePVector.singleton(EnergyCollectorEventTag.EVENT_AGGREGATE_EVENT_TAG);
    }

    private CompletionStage<Done> prepareCreateTables() {
        log.info("init all tables");
        return session.executeCreateTable(
                "CREATE TABLE IF NOT EXISTS VILLAGE_CONSUMPTIONS ("
                        + "counterId bigint, villageName text, consumptionAmount double, updateTime timestamp, "
                        + "PRIMARY KEY (counterId, updateTime)) WITH CLUSTERING ORDER BY (updateTime DESC)")
                .thenCompose(ps ->
                session.executeCreateTable(
                        "CREATE TABLE IF NOT EXISTS VILLAGE_COUNTERS ("
                                + "counterId bigint, villageName text, "
                                + "PRIMARY KEY (counterId))"
                ));
    }

    private CompletionStage<Done> prepareStatements() {
        CompletionStage<Done> insertVillageConsumptionCompletionStage = session.prepare("INSERT INTO VILLAGE_CONSUMPTIONS (counterId, villageName, consumptionAmount, updateTime) VALUES (?, ?, ?, ?)").thenApply(ps -> {
            this.writeConsumption = ps;
            return Done.getInstance();
        });
        CompletionStage<Done> insertVillageCounterCompletionStage = session.prepare("INSERT INTO VILLAGE_COUNTERS (counterId, villageName) VALUES (?, ?)").thenApply(ps -> {
            this.writeCounter = ps;
            return Done.getInstance();
        });

        return insertVillageConsumptionCompletionStage.thenCombine(insertVillageCounterCompletionStage, (t, y) -> Done.getInstance());
    }

    private CompletionStage<List<BoundStatement>> processConsumptionUpdated(ConsumptionUpdated event) {
        BoundStatement bindWriteUser = writeConsumption.bind();
        bindWriteUser.setLong("counterId", event.getCounterId());
        bindWriteUser.setString("villageName", event.getVillageName());
        bindWriteUser.setDouble("consumptionAmount", event.getConsumptionAmount());
        bindWriteUser.setTimestamp("updateTime", Date.from(event.getConsumptionTime()));
        log.info("put ConsumptionUpdated event in the read side " + event.toString());
        return completedStatement(bindWriteUser);
    }

    private CompletionStage<List<BoundStatement>> processCounterRegistered(CallbackRegistered event) {
        BoundStatement bindWriteUser = writeCounter.bind();
        bindWriteUser.setLong("counterId", event.getCounterId());
        bindWriteUser.setString("villageName", event.getVillageName());
        log.info("put CallbackRegistered event in the read side " + event.toString());
        return completedStatement(bindWriteUser);
    }

}