package com.energycollector.persistence;

import akka.Done;
import akka.japi.Pair;
import akka.stream.Materializer;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.lightbend.lagom.javadsl.persistence.*;
import com.lightbend.lagom.javadsl.persistence.ReadSideProcessor.ReadSideHandler;
import org.pcollections.PSequence;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.SECONDS;

@Singleton
final class ReadSideTestDriver implements ReadSide {

    private final Injector injector;
    private final Materializer materializer;
    private final ConcurrentMap<Class<?>, List<Pair<ReadSideHandler<?>, Offset>>> processors;

    @Inject
    ReadSideTestDriver(Injector injector, Materializer materializer) {
        this.injector = injector;
        this.materializer = materializer;
        this.processors = new ConcurrentHashMap<>();
    }


    @Override
    public <Event extends AggregateEvent<Event>> void register(Class<? extends ReadSideProcessor<Event>> processorClass) {
        ReadSideProcessor<Event> processor = injector.getInstance(processorClass);
        PSequence<AggregateEventTag<Event>> eventTags = processor.aggregateTags();

        CompletionStage<Done> processorInit = processor.buildHandler().globalPrepare().thenCompose(x -> {
            AggregateEventTag<Event> tag = eventTags.get(0);
            ReadSideHandler<Event> handler = processor.buildHandler();
            return handler.prepare(tag).thenApply(offset -> {
                List<Pair<ReadSideHandler<?>, Offset>> currentHandlers =
                        processors.computeIfAbsent(tag.eventType(), (z) -> new ArrayList<>());
                currentHandlers.add(Pair.create(handler, offset));
                return Done.getInstance();
            });
        });

        try {
            processorInit.toCompletableFuture().get(30, SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Couldn't register the processor on the testkit.", e);
        }

    }

    <Event extends AggregateEvent<Event>> CompletionStage<Done> feed(Event e, Offset offset) {
        AggregateEventTagger<Event> tag = e.aggregateTag();
        List<Pair<ReadSideHandler<?>, Offset>> list = processors.get(tag.eventType());
        if (list == null) {
            throw new RuntimeException("No processor registered for Event " + tag.eventType().getCanonicalName());
        }

        List<CompletionStage<?>> stages = list.stream().map(pHandlerOffset -> {
            @SuppressWarnings("unchecked")
            ReadSideHandler<Event> handler = (ReadSideHandler<Event>) pHandlerOffset.first();
            Flow<Pair<Event, Offset>, Done, ?> flow = handler.handle();
                return Source.single(Pair.create(e, offset)).via(flow).runWith(Sink.ignore(), materializer);
            }
        ).collect(Collectors.toList());
        return doAll(stages);
    }

    private static CompletionStage<Done> doAll(List<CompletionStage<?>> stages) {
        CompletionStage<Done> result = CompletableFuture.completedFuture(Done.getInstance());
        for (CompletionStage<?> stage : stages) {
            result = result.thenCombine(stage, (d1, d2) -> Done.getInstance());
        }
        return result;
    }

}
