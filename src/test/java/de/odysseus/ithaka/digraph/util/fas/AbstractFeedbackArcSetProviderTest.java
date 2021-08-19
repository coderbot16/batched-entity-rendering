/*
 * Copyright 2012 Odysseus Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.odysseus.ithaka.digraph.util.fas;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

import de.odysseus.ithaka.digraph.MapDigraph;

import org.junit.Assert;
import org.junit.Test;

import de.odysseus.ithaka.digraph.Digraph;
import de.odysseus.ithaka.digraph.Digraphs;
import de.odysseus.ithaka.digraph.EdgeWeights;

public class AbstractFeedbackArcSetProviderTest {
	@Test
	public void testMinWeightPolicy() {
		final Digraph<Integer> graph = new MapDigraph<>();
		graph.put(1, 2, 3);
		graph.put(2, 1, 1);

		AbstractFeedbackArcSetProvider provider = new AbstractFeedbackArcSetProvider() {
			@Override
			protected <V> Digraph<V> lfas(Digraph<V> digraph, EdgeWeights<? super V> weights) {
				Assert.assertSame(digraph, graph);
				Assert.assertSame(weights, graph);
				return digraph;
			}
		};

		FeedbackArcSet<Integer> feedback = provider.getFeedbackArcSet(graph, graph, FeedbackArcSetPolicy.MIN_WEIGHT);
		Assert.assertFalse(feedback.isExact());
		Assert.assertEquals(4, feedback.getWeight());
		Assert.assertTrue(Digraphs.isEquivalent(feedback, graph, true));
	}

	@Test
	public void testMinSizePolicy() {
		final Digraph<Integer> graph = new MapDigraph<>();
		graph.put(1, 2, 3);
		graph.put(2, 1, 1);

		AbstractFeedbackArcSetProvider provider = new AbstractFeedbackArcSetProvider() {
			@Override
			protected <V> Digraph<V> lfas(Digraph<V> digraph, EdgeWeights<? super V> weights) {
				Assert.assertSame(digraph, graph);
				for (V source : digraph.vertices()) {
					for (V target : digraph.targets(source)) {
						if (Integer.valueOf(1).equals(source)) {
							Assert.assertEquals(OptionalInt.of(7), weights.get(source, target));
						} else {
							Assert.assertEquals(OptionalInt.of(5), weights.get(source, target));
						}
					}
				}
				return digraph;
			}
		};

		FeedbackArcSet<Integer> feedback = provider.getFeedbackArcSet(graph, graph, FeedbackArcSetPolicy.MIN_SIZE);
		Assert.assertFalse(feedback.isExact());
		Assert.assertEquals(4, feedback.getWeight());
		Assert.assertTrue(Digraphs.isEquivalent(feedback, graph, true));
	}

	@Test
	public void testDecompose() {
		final Digraph<Integer> graph = new MapDigraph<>();
		graph.put(1, 2, 3);
		graph.put(2, 1, 1);

		AbstractFeedbackArcSetProvider provider = new AbstractFeedbackArcSetProvider(0) { // current thread
			@Override
			protected <V> Digraph<V> lfas(Digraph<V> digraph, EdgeWeights<? super V> weights) {
				Assert.assertSame(digraph, graph);
				Assert.assertSame(weights, graph);
				return digraph;
			}
		};

		FeedbackArcSet<Integer> feedback = provider.getFeedbackArcSet(graph, graph, FeedbackArcSetPolicy.MIN_WEIGHT);
		Assert.assertFalse(feedback.isExact());
		Assert.assertEquals(4, feedback.getWeight());
		Assert.assertTrue(Digraphs.isEquivalent(feedback, graph, true));
	}

	@Test
	public void testDecompose2() {
		final Digraph<Integer> graph = new MapDigraph<>();
		graph.put(1, 2, 3);
		graph.put(2, 1, 1);
		graph.put(2, 3, 1);
		graph.put(3, 4, 1);
		graph.put(4, 3, 2);
		graph.put(4, 5, 1);

		final List<Thread> threads = new ArrayList<Thread>();
		AbstractFeedbackArcSetProvider provider = new AbstractFeedbackArcSetProvider(2) { // two threads
			@Override
			protected <V> Digraph<V> lfas(Digraph<V> digraph, EdgeWeights<? super V> weights) {
				threads.add(Thread.currentThread());
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					Assert.fail();
				}
				Assert.assertFalse(digraph == graph);
				Assert.assertTrue(weights == graph);
				return digraph;
			}
		};

		FeedbackArcSet<Integer> feedback = provider.getFeedbackArcSet(graph, graph, FeedbackArcSetPolicy.MIN_WEIGHT);
		Assert.assertEquals(2, threads.size());
		Assert.assertNotSame(threads.get(0), threads.get(1));
		Assert.assertFalse(feedback.isExact());
		Assert.assertEquals(7, feedback.getWeight());
	}

	@Test
	public void testAcyclic() {
		final Digraph<Integer> graph = new MapDigraph<>();
		graph.put(1, 2, 3);
		graph.put(2, 3, 1);

		AbstractFeedbackArcSetProvider provider = new AbstractFeedbackArcSetProvider(0) {
			@Override
			public <V> FeedbackArcSet<V> mfas(Digraph<V> digraph, EdgeWeights<? super V> weights) {
				Assert.fail();
				return null;
			}

			@Override
			protected <V> Digraph<V> lfas(Digraph<V> digraph, EdgeWeights<? super V> weights) {
				Assert.fail();
				return null;
			}
		};

		FeedbackArcSet<Integer> feedback = provider.getFeedbackArcSet(graph, graph, FeedbackArcSetPolicy.MIN_WEIGHT);
		Assert.assertTrue(feedback.isExact());
		Assert.assertEquals(0, feedback.getWeight());
		Assert.assertEquals(0, feedback.getVertexCount());
	}
}
