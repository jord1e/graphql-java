package benchmark;

import graphql.ExecutionInput;
import graphql.Scalars;
import graphql.execution.instrumentation.ChainedInstrumentation;
import graphql.execution.instrumentation.Instrumentation;
import graphql.execution.instrumentation.InstrumentationState;
import graphql.execution.instrumentation.SimpleInstrumentation;
import graphql.execution.instrumentation.parameters.InstrumentationCreateStateParameters;
import graphql.execution.instrumentation.parameters.InstrumentationExecutionParameters;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@Fork(value = 2, jvmArgs = {"-Xms3G", "-Xmx3G"})
@Threads(2)
@Warmup(iterations = 5, time = 15)
@Measurement(iterations = 2, timeUnit = TimeUnit.NANOSECONDS, time = 15)
public class ChainedInstrumentationBenchmark {

    @Param({"1", "2", "3", "4", "5", "8", "10", "12", "15", "30"})
    private int INSTRUMENTATION_LIST_SIZES;

    private ChainedInstrumentation testInstrumentation;
    private InstrumentationExecutionParameters testInstrumentationExecutionParameters;

    private static final ExecutionInput executionInput = ExecutionInput.newExecutionInput("{}").build();
    private static final GraphQLObjectType queryType = GraphQLObjectType.newObject().name("Query")
            .field(GraphQLFieldDefinition.newFieldDefinition().name("a")
                    .type(Scalars.GraphQLString).build()).build();
    private static final GraphQLSchema emptySchema = GraphQLSchema.newSchema().query(queryType).build();

    static class NamedInstrumentationState implements InstrumentationState {
        private final String name;

        public NamedInstrumentationState(String name) {
            this.name = name;
        }
    }

    static class NamedInstrumentation extends SimpleInstrumentation {
        private final String name;

        public NamedInstrumentation(String name) {
            this.name = name;
        }

        @Override
        public InstrumentationState createState() {
            return new NamedInstrumentationState(this.name);
        }
    }

    @Setup
    public void setup() {
        final List<Instrumentation> instrumentationList = new ArrayList<>();
        for (int i = 0; i < INSTRUMENTATION_LIST_SIZES; i++) {
            instrumentationList.add(new NamedInstrumentation(String.valueOf(i)));
        }
        this.testInstrumentation = new ChainedInstrumentation(instrumentationList);

        this.testInstrumentationExecutionParameters = new InstrumentationExecutionParameters(executionInput, emptySchema,
                testInstrumentation.createState(new InstrumentationCreateStateParameters(emptySchema, executionInput)));
    }

    @Benchmark
    public void benchmark(Blackhole bh) {
        bh.consume(testInstrumentation.beginParse(testInstrumentationExecutionParameters));
    }
}
