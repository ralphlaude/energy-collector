package com.energycollector;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@ImmutableStyle
@JsonDeserialize(as = CounterInfoResponse.class)
public interface AbstractCounterInfoResponse {

    @Value.Parameter
    Optional<Long> getId();

    @Value.Parameter
    Optional<String> getVillageName();

    Optional<String> getApiStatus();
}
