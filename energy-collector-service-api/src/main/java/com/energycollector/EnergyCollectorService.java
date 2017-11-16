package com.energycollector;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.Method;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.restCall;

/**
 * This class define the API for the energy collector service.
 */
public interface EnergyCollectorService extends Service {

    ServiceCall<AbstractRegisterVillageCounterRequest, NotUsed> callbackCounter();
    ServiceCall<AbstractUpdateConsumptionRequest, NotUsed> updateCounterConsumption(long counterId);
    ServiceCall<NotUsed, AbstractCounterInfoResponse> counterInfo(long counterId);
    ServiceCall<NotUsed, AbstractConsumptionResponse> allVillageConsumption(int duration);

    default Descriptor descriptor() {
        return named("energycollector").withCalls(
                restCall(Method.POST,   "/energycollector/counter_callback", this::callbackCounter),
                restCall(Method.POST,   "/energycollector/counter_update?id", this::updateCounterConsumption),
                restCall(Method.GET,   "/energycollector/counter?id", this::counterInfo),
                restCall(Method.GET,   "/energycollector/consumption_report?duration", this::allVillageConsumption)
        ).withAutoAcl(true);
    }
}
