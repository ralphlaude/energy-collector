package com.energycollector.persistence;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.serialization.Jsonable;
import org.immutables.value.Value;

import java.time.Instant;

/**
 * Represents all events that can be issued by and village energy collector on state change.
 */
public interface AbstractEnergyCollectorEvent extends Jsonable, AggregateEvent<AbstractEnergyCollectorEvent> {

    default AggregateEventTag<AbstractEnergyCollectorEvent> aggregateTag() {
        return EnergyCollectorEventTag.EVENT_AGGREGATE_EVENT_TAG;
    }

    @Value.Immutable
    @ImmutableStyle
    @JsonDeserialize(as = CallbackRegistered.class)
    interface AbstractCallbackRegistered extends AbstractEnergyCollectorEvent {

        @Value.Parameter
        long getCounterId();

        @Value.Parameter
        String getVillageName();

        @Value.Parameter
        double getInitialAmount();
    }

    @Value.Immutable
    @ImmutableStyle
    @JsonDeserialize(as = ConsumptionUpdated.class)
    interface AbstractConsumptionUpdated extends AbstractEnergyCollectorEvent {

        @Value.Parameter
        long getCounterId();

        @Value.Parameter
        String getVillageName();

        @Value.Parameter
        double getConsumptionAmount();

        @Value.Parameter
        double getTotalAmount();

        @Value.Parameter
        Instant getConsumptionTime();
    }
}
