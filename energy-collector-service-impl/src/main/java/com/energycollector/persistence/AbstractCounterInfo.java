package com.energycollector.persistence;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import com.lightbend.lagom.serialization.Jsonable;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@ImmutableStyle
@JsonDeserialize(as = CounterInfo.class)
public interface AbstractCounterInfo extends Jsonable {

    @Value.Parameter
    Optional<Long> getCounterId();

    @Value.Parameter
    Optional<String> getVillageName();

    default boolean isValid() {
        return getCounterId().isPresent();
    }
}
