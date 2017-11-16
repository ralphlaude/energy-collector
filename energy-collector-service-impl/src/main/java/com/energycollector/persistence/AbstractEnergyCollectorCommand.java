package com.energycollector.persistence;

import akka.Done;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.serialization.Jsonable;
import org.immutables.value.Value;

import java.time.Instant;

/**
 * Represents all commands that can be handled by an village energy collector.
 */
public interface AbstractEnergyCollectorCommand extends Jsonable {

    @Value.Immutable
    @ImmutableStyle
    @JsonDeserialize(as = CallbackRegister.class)
    interface AbstractCallbackRegister extends AbstractEnergyCollectorCommand, PersistentEntity.ReplyType<Done> {

        @Value.Parameter
        long getCounterId();

        @Value.Parameter
        String getVillageName();

        @Value.Parameter
        double getInitialAmount();
    }

    @Value.Immutable
    @ImmutableStyle
    @JsonDeserialize(as = ConsumptionUpdate.class)
    interface AbstractConsumptionUpdate extends AbstractEnergyCollectorCommand, PersistentEntity.ReplyType<Done> {

        @Value.Parameter
        long getCounterId();

        @Value.Parameter
        double getTotalAmount();

        @Value.Parameter
        Instant getConsumptionTime();
    }

    @Value.Immutable
    @ImmutableStyle
    @JsonDeserialize(as = GetCounterInfo.class)
    interface AbstractGetCounterInfo extends AbstractEnergyCollectorCommand, PersistentEntity.ReplyType<AbstractCounterInfo> {
    }
}
