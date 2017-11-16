package com.energycollector;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import org.immutables.value.Value;

@Value.Immutable
@ImmutableStyle
@JsonDeserialize(as = RegisterVillageCounterRequest.class)
public interface AbstractRegisterVillageCounterRequest {

    @Value.Parameter
    long getCounterId();

    @Value.Parameter
    double getInitialAmount();

    @Value.Parameter
    String getVillageName();
}
