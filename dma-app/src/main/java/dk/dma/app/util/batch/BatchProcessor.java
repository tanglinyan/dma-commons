package dk.dma.app.util.batch;

public interface BatchProcessor<T> extends Processor<T> {

    void finished(Throwable exceptionalFinished);
}
