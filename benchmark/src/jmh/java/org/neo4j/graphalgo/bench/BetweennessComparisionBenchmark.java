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

import org.neo4j.graphalgo.TestDatabaseCreator;
import org.neo4j.graphalgo.centrality.BetweennessCentralityProc;
import org.neo4j.graphalgo.core.utils.Pools;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Threads(1)
@Fork(1)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class BetweennessComparisionBenchmark extends BaseBenchmark {

    private static final RelationshipType RELATIONSHIP_TYPE = RelationshipType.withName("TYPE");

    private List<Node> lines = new ArrayList<>();

    @Setup
    public void setup() throws Exception {
        db = TestDatabaseCreator.createTestDatabase();
        registerProcedures(BetweennessCentralityProc.class);

        createNet(30);
    }

    @TearDown
    public void tearDown() {
        db.shutdown();
        Pools.DEFAULT.shutdownNow();
    }

    private void createNet(int size) {
        try (Transaction tx = db.beginTx()) {
            List<Node> temp = null;
            for (int i = 0; i < size; i++) {
                List<Node> line = createLine(size);
                if (null != temp) {
                    for (int j = 0; j < size; j++) {
                        for (int k = 0; k < size; k++) {
                            if (i == k) {
                                continue;
                            }
                            createRelation(temp.get(j), line.get(k));
                        }
                    }
                }
                temp = line;
            }
            tx.success();
        }
    }

    private List<Node> createLine(int length) {
        ArrayList<Node> nodes = new ArrayList<>();
        Node temp = db.createNode();
        nodes.add(temp);
        lines.add(temp);
        for (int i = 1; i < length; i++) {
            Node node = db.createNode();
            nodes.add(temp);
            createRelation(temp, node);
            temp = node;
        }
        return nodes;
    }

    private static Relationship createRelation(Node from, Node to) {
        return from.createRelationshipTo(to, RELATIONSHIP_TYPE);
    }

    @Benchmark
    public Object _01_benchmark_sequential() {
        return runQuery(
            "CALL algo.betweenness('','', {write:false}) YIELD computeMillis",
            r -> r.stream().count()
        );
    }

    @Benchmark
    public Object _04_benchmark_parallel8() {
        return runQuery(
            "CALL algo.betweenness('','', {write:false, concurrency:8}) YIELD computeMillis",
            r -> r.stream().count()
        );
    }

    @Benchmark
    public Object _05_benchmark_sucessorBrandes() {
        return runQuery(
            "CALL algo.betweenness.exp1('','', {write:false, concurrency:8}) YIELD computeMillis",
            r -> r.stream().count()
        ); }

}
