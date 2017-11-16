package com.energycollector.service;

import akka.NotUsed;
import akka.japi.Pair;
import com.energycollector.AbstractConsumptionResponse;
import com.energycollector.AbstractCounterInfoResponse;
import com.energycollector.AbstractRegisterVillageCounterRequest;
import com.energycollector.AbstractUpdateConsumptionRequest;
import com.energycollector.ConsumptionResponse;
import com.energycollector.CounterInfoResponse;
import com.energycollector.EnergyCollectorService;
import com.energycollector.persistence.AbstractEnergyCollectorCommand;
import com.energycollector.persistence.CallbackRegister;
import com.energycollector.persistence.ConsumptionUpdate;
import com.energycollector.persistence.CounterInfo;
import com.energycollector.persistence.CounterRepository;
import com.energycollector.persistence.EnergyCollectorEntity;
import com.energycollector.persistence.GetCounterInfo;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.ResponseHeader;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import com.lightbend.lagom.javadsl.server.HeaderServiceCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.Instant;

/**
 * Represents the implementation of the service.
 *
 * @see EnergyCollectorService
 */
public final class EnergyCollectorServiceImpl implements EnergyCollectorService {

    private final Logger log = LoggerFactory.getLogger(EnergyCollectorServiceImpl.class);

    private final PersistentEntityRegistry persistentEntities;
    private final CounterRepository counterRepository;

    @Inject
    EnergyCollectorServiceImpl(PersistentEntityRegistry persistentEntities, CounterRepository counterRepository) {
        this.persistentEntities = persistentEntities;
        this.counterRepository = counterRepository;

        this.persistentEntities.register(EnergyCollectorEntity.class);
    }

    @Override
    public ServiceCall<AbstractRegisterVillageCounterRequest, NotUsed> callbackCounter() {
        return HeaderServiceCall.of((requestHeader, request) -> {
            CallbackRegister registerCounter = CallbackRegister.builder()
                    .counterId(request.getCounterId())
                    .villageName(request.getVillageName())
                    .initialAmount(request.getInitialAmount())
                    .build();
            return persistentEntities.refFor(EnergyCollectorEntity.class, String.valueOf(request.getCounterId())).ask(registerCounter).thenApply(reply -> {
                log.info("counter successfully created");
                ResponseHeader responseHeader = ResponseHeader.OK.withStatus(201)
                        .withHeader("Location", "http://localhost:9000/energycollector/counter?id=" + request.getCounterId());
                Pair<ResponseHeader, NotUsed> response = Pair.create(responseHeader, NotUsed.getInstance());
                return response;
            });
        });
    }

    @Override
    public ServiceCall<AbstractUpdateConsumptionRequest, NotUsed> updateCounterConsumption(long counterId) {
       return HeaderServiceCall.of((requestHeader, request) -> {
            PersistentEntityRef<AbstractEnergyCollectorCommand> entityRef = persistentEntities.refFor(EnergyCollectorEntity.class, String.valueOf(counterId));
           ConsumptionUpdate counterConsumptionUpdate = ConsumptionUpdate.builder()
                    .counterId(counterId)
                    .totalAmount(request.getTotalAmount())
                    .consumptionTime(Instant.now())
                    .build();
            return entityRef.ask(counterConsumptionUpdate).thenApply(reply -> {
                log.info("Counter consumption successfully updated");
                ResponseHeader responseHeader = ResponseHeader.OK.withStatus(201)
                        .withHeader("Location", "http://localhost:9000/energycollector/counter?id=" + counterId);
                Pair<ResponseHeader, NotUsed> response = Pair.create(responseHeader, NotUsed.getInstance());
                return response;
            });
        });
    }

    @Override
    public ServiceCall<NotUsed, AbstractCounterInfoResponse> counterInfo(long counterId) {
        return notUsed -> {
            PersistentEntityRef<AbstractEnergyCollectorCommand> entityRef = persistentEntities.refFor(EnergyCollectorEntity.class, String.valueOf(counterId));
            GetCounterInfo getCounterInfo = GetCounterInfo.builder().build();
            return entityRef.ask(getCounterInfo).thenApply(reply -> {
                log.info("Counter info");
                CounterInfo counterInfo = (CounterInfo) reply;
                if(!counterInfo.isValid()) {
                    return CounterInfoResponse.builder().apiStatus("counter info is not accessible").build();
                } else {
                    CounterInfoResponse counterInfoResponse = CounterInfoResponse.builder().id(counterInfo.getCounterId())
                            .villageName(counterInfo.getVillageName()).build();
                    return counterInfoResponse;
                }
            });
        };
    }

    @Override
    public ServiceCall<NotUsed, AbstractConsumptionResponse> allVillageConsumption(int duration) {
        return notUsed ->
            counterRepository.consumptionByPeriod(duration)
                    .thenApply(villageConsumptions -> ConsumptionResponse.builder().villageConsumptions(villageConsumptions).build());
    }

}
