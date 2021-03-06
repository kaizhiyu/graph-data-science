/*
 * Copyright (c) 2017-2020 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.graphalgo.bench;

import org.neo4j.graphalgo.PropertyMapping;
import org.neo4j.graphalgo.StoreLoaderBuilder;
import org.neo4j.graphalgo.api.Graph;
import org.neo4j.graphalgo.core.loading.HugeGraphFactory;
import org.neo4j.graphalgo.pagerank.PageRank;
import org.neo4j.graphalgo.pagerank.PageRankAlgorithmType;
import org.neo4j.graphalgo.results.CentralityResult;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

@Threads(1)
@Fork(1)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class WeightedPageRankBenchmark extends  BaseBenchmark {

    @Param({"5", "20", "100"})
    int iterations;

    private GraphDatabaseAPI db;

    @Setup
    public void setup() {
        String createGraph = "CREATE (nA)\n" +
                "CREATE (nB)\n" +
                "CREATE (nC)\n" +
                "CREATE (nD)\n" +
                "CREATE (nE)\n" +
                "CREATE (nF)\n" +
                "CREATE (nG)\n" +
                "CREATE (nH)\n" +
                "CREATE (nI)\n" +
                "CREATE (nJ)\n" +
                "CREATE (nK)\n" +
                "CREATE\n" +
                "  (nB)-[:TYPE {weight: 1}]->(nC),\n" +
                "  (nC)-[:TYPE {weight: 3}]->(nB),\n" +
                "  (nD)-[:TYPE {weight: 2}]->(nA),\n" +
                "  (nD)-[:TYPE {weight: 5}]->(nB),\n" +
                "  (nE)-[:TYPE {weight: 7}]->(nB),\n" +
                "  (nE)-[:TYPE {weight: 8}]->(nD),\n" +
                "  (nE)-[:TYPE {weight: 3}]->(nF),\n" +
                "  (nF)-[:TYPE {weight: 12}]->(nB),\n" +
                "  (nF)-[:TYPE {weight: 11}]->(nE),\n" +
                "  (nG)-[:TYPE {weight: 10}]->(nB),\n" +
                "  (nG)-[:TYPE {weight: 2}]->(nE),\n" +
                "  (nH)-[:TYPE {weight: 5}]->(nB),\n" +
                "  (nH)-[:TYPE {weight: 8}]->(nE),\n" +
                "  (nI)-[:TYPE {weight: 7}]->(nB),\n" +
                "  (nI)-[:TYPE {weight: 2}]->(nE),\n" +
                "  (nJ)-[:TYPE {weight: 19}]->(nE),\n" +
                "  (nK)-[:TYPE {weight: 12}]->(nE);";
        db = (GraphDatabaseAPI)
                new TestGraphDatabaseFactory()
                        .newImpermanentDatabaseBuilder()
                        .newGraphDatabase();
        try (Transaction tx = db.beginTx()) {
            runQuery(createGraph);
            tx.success();
        }
    }

    @TearDown
    public void tearDown() {
        db.shutdown();
    }

    @Benchmark
    public CentralityResult run() {
        final Graph graph = new StoreLoaderBuilder()
            .api(db)
            .loadAnyLabel()
            .loadAnyRelationshipType()
            .addRelationshipProperty(PropertyMapping.of("weight", 0.0))
            .build()
            .graph(HugeGraphFactory.class);

        try {
            final PageRank.Config algoConfig = new PageRank.Config(iterations, 0.85, PageRank.DEFAULT_TOLERANCE);
            return PageRankAlgorithmType.WEIGHTED
                    .create(graph, algoConfig, LongStream.empty())
                    .compute()
                    .result();
        } finally {
            graph.release();
        }
    }
}
