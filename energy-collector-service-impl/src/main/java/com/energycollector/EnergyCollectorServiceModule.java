package com.energycollector;

import com.energycollector.persistence.CounterRepository;
import com.energycollector.service.EnergyCollectorServiceImpl;
import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;

public class EnergyCollectorServiceModule extends AbstractModule implements ServiceGuiceSupport {

    @Override
    protected void configure() {
        bindService(EnergyCollectorService.class, EnergyCollectorServiceImpl.class);
        bind(CounterRepository.class);
    }
}
