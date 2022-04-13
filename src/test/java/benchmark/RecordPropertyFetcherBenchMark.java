package benchmark;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingEnvironmentImpl;
import graphql.schema.PropertyDataFetcher;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@Warmup(iterations = 2, time = 5, batchSize = 3)
@Measurement(iterations = 3, time = 10, batchSize = 4)
public class RecordPropertyFetcherBenchMark {

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void benchMarkRecordViaFieldAccess(Blackhole blackhole) {
        blackhole.consume(nameFetcher.get(dfeField));
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void benchMarkRecordViaMethodAccess(Blackhole blackhole) {
        blackhole.consume(nameFetcher.get(dfeMethod));
    }

    static PropertyDataFetcher<Object> nameFetcher = PropertyDataFetcher.fetching("a");

    static DataFetchingEnvironment dfeMethod = DataFetchingEnvironmentImpl.newDataFetchingEnvironment().source(new MethodRecord("world")).build();
    static DataFetchingEnvironment dfeField = DataFetchingEnvironmentImpl.newDataFetchingEnvironment().source(new FieldRecord("world")).build();
}
