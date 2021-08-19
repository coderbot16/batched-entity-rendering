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
package de.odysseus.ithaka.digraph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class DigraphsTest {

	@Test
	public void testEmptyDigraph() {
		Digraph<?> empty = Digraphs.emptyDigraph();
		Assert.assertEquals(0, empty.getEdgeCount());
		Assert.assertEquals(0, empty.getVertexCount());
		Assert.assertTrue(empty.isAcyclic());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testUnmodifiableDigraph() {
		Digraph<Integer> digraph = Digraphs.unmodifiableDigraph(new MapDigraph<>());
		digraph.put(1, 2, 3);
	}

	@Test
	public void testToposort() {
		Digraph<Integer> g = new MapDigraph<>();
		g.put(1, 2, 1);
		g.put(2, 3, 1);
		g.put(3, 4, 1);
		g.put(1, 3, 1);
		g.put(2, 4, 1);
		assert g.isAcyclic();

		int n = 0;
		int v = 0;
		for (int w : Digraphs.toposort(g, false)) {
			Assert.assertTrue(v < w);
			v = w;
			n++;
		}
		Assert.assertEquals(g.getVertexCount(), n);
	}

	@Test
	public void testClosure() {
		Digraph<Integer> g = new MapDigraph<>();
		g.put(1, 2, 1);
		g.put(2, 3, 1);
		g.put(3, 2, 1);
		g.put(3, 4, 1);

		Set<Integer> c;

		c = Digraphs.closure(g, 1);
		Assert.assertEquals(4, c.size());

		c = Digraphs.closure(g, 2);
		Assert.assertEquals(3, c.size());
		Assert.assertFalse(c.contains(1));

		c = Digraphs.closure(g, 3);
		Assert.assertEquals(3, c.size());
		Assert.assertFalse(c.contains(1));

		c = Digraphs.closure(g, 4);
		Assert.assertEquals(1, c.size());
		Assert.assertTrue(c.contains(4));
	}

	@Test
	public void testIsAcyclic() {
		Digraph<Integer> g;

		g = new MapDigraph<>();
		g.put(1, 2, 1);
		g.put(2, 3, 1);
		g.put(3, 4, 1);
		g.put(1, 3, 1);
		g.put(2, 4, 1);
		Assert.assertTrue(Digraphs.isAcyclic(g));

		g = new MapDigraph<>();
		g.put(1, 2, 1);
		g.put(2, 3, 1);
		g.put(3, 2, 1);
		g.put(3, 4, 1);
		Assert.assertFalse(Digraphs.isAcyclic(g));
	}

	@Test
	public void testIsStronglyConnected() {
		Digraph<Integer> g;

		g = new MapDigraph<>();
		g.put(1, 2, 1);
		g.put(2, 3, 1);
		g.put(3, 4, 1);
		g.put(1, 3, 1);
		g.put(2, 4, 1);
		Assert.assertFalse(Digraphs.isStronglyConnected(g));

		g = new MapDigraph<>();
		g.put(1, 2, 1);
		g.put(2, 3, 1);
		g.put(3, 4, 1);
		g.put(4, 2, 1);
		g.put(3, 1, 1);
		Assert.assertTrue(Digraphs.isStronglyConnected(g));
	}

	@Test
	public void testIsEquivalent() {
		Digraph<Integer> g1;
		Digraph<Integer> g2;

		g1 = new MapDigraph<>();
		g1.add(0);
		g1.put(1, 2, 1);
		g1.put(2, 3, 2);
		g1.put(3, 4, 3);
		g1.put(1, 3, 4);
		g1.put(2, 4, 5);

		g2 = g1;

		Assert.assertTrue(Digraphs.isEquivalent(g1, g2, false));
		Assert.assertTrue(Digraphs.isEquivalent(g1, g2, true));

		g2 = new MapDigraph<>();
		g2.add(0);
		g2.put(1, 2, 1);
		g2.put(2, 3, 2);
		g2.put(3, 4, 3);
		g2.put(1, 3, 4);
		g2.put(2, 4, 5);

		Assert.assertTrue(Digraphs.isEquivalent(g1, g2, false));
		Assert.assertTrue(Digraphs.isEquivalent(g2, g1, false));
		Assert.assertTrue(Digraphs.isEquivalent(g1, g2, true));
		Assert.assertTrue(Digraphs.isEquivalent(g2, g1, true));

		g2.remove(0);

		Assert.assertFalse(Digraphs.isEquivalent(g1, g2, false));
		Assert.assertFalse(Digraphs.isEquivalent(g2, g1, false));
		Assert.assertFalse(Digraphs.isEquivalent(g1, g2, true));
		Assert.assertFalse(Digraphs.isEquivalent(g2, g1, true));

		g2.add(0);

		Assert.assertTrue(Digraphs.isEquivalent(g1, g2, false));
		Assert.assertTrue(Digraphs.isEquivalent(g2, g1, false));
		Assert.assertTrue(Digraphs.isEquivalent(g1, g2, true));
		Assert.assertTrue(Digraphs.isEquivalent(g2, g1, true));

		g2.remove(2, 3);

		Assert.assertFalse(Digraphs.isEquivalent(g1, g2, false));
		Assert.assertFalse(Digraphs.isEquivalent(g2, g1, false));
		Assert.assertFalse(Digraphs.isEquivalent(g1, g2, true));
		Assert.assertFalse(Digraphs.isEquivalent(g2, g1, true));

		g2.put(2, 3, 2);

		Assert.assertTrue(Digraphs.isEquivalent(g1, g2, false));
		Assert.assertTrue(Digraphs.isEquivalent(g2, g1, false));
		Assert.assertTrue(Digraphs.isEquivalent(g1, g2, true));
		Assert.assertTrue(Digraphs.isEquivalent(g2, g1, true));

		g2.put(2, 3, 0);

		Assert.assertTrue(Digraphs.isEquivalent(g1, g2, false));
		Assert.assertTrue(Digraphs.isEquivalent(g2, g1, false));
		Assert.assertFalse(Digraphs.isEquivalent(g1, g2, true));
		Assert.assertFalse(Digraphs.isEquivalent(g2, g1, true));
	}

	@Test
	public void testIsReachable() {
		Digraph<Integer> g = new MapDigraph<>();
		g.put(1, 2, 1);
		g.put(2, 3, 1);
		g.put(3, 2, 1);
		g.put(3, 4, 1);

		Assert.assertTrue(Digraphs.isReachable(g, 1, 1));
		Assert.assertTrue(Digraphs.isReachable(g, 1, 2));
		Assert.assertTrue(Digraphs.isReachable(g, 1, 3));
		Assert.assertTrue(Digraphs.isReachable(g, 1, 4));
		Assert.assertTrue(Digraphs.isReachable(g, 2, 2));
		Assert.assertTrue(Digraphs.isReachable(g, 2, 3));
		Assert.assertTrue(Digraphs.isReachable(g, 2, 4));
		Assert.assertTrue(Digraphs.isReachable(g, 3, 2));
		Assert.assertTrue(Digraphs.isReachable(g, 3, 3));
		Assert.assertTrue(Digraphs.isReachable(g, 3, 4));
		Assert.assertTrue(Digraphs.isReachable(g, 4, 4));

		Assert.assertFalse(Digraphs.isReachable(g, 2, 1));
		Assert.assertFalse(Digraphs.isReachable(g, 3, 1));
		Assert.assertFalse(Digraphs.isReachable(g, 4, 1));
		Assert.assertFalse(Digraphs.isReachable(g, 4, 2));
		Assert.assertFalse(Digraphs.isReachable(g, 4, 3));
	}

	@Test
	public void testDfs() {
		Digraph<Integer> g = new MapDigraph<>();
		g.put(1, 2, 1);
		g.put(2, 3, 1);
		g.put(3, 2, 1);
		g.put(3, 4, 1);

		Set<Integer> discovered = new HashSet<>();
		List<Integer> finished = new ArrayList<>();
		Digraphs.dfs(g, 1, discovered, finished);

		Assert.assertEquals(4, discovered.size());
		Assert.assertEquals(4, finished.size());
		Assert.assertEquals(4, finished.get(0).intValue());
		Assert.assertEquals(3, finished.get(1).intValue());
		Assert.assertEquals(2, finished.get(2).intValue());
		Assert.assertEquals(1, finished.get(3).intValue());
	}

	@Test
	public void testDfs2() {
		Digraph<Integer> g = new MapDigraph<>();
		g.put(2, 1, 1);
		g.put(2, 3, 1);
		g.put(3, 2, 1);
		g.put(3, 4, 1);

		Set<Integer> discovered = new HashSet<>();
		List<Integer> finished = new ArrayList<>();
		Digraphs.dfs2(g, 1, discovered, finished);

		Assert.assertEquals(4, discovered.size());
		Assert.assertEquals(4, finished.size());
		Assert.assertEquals(4, finished.get(0).intValue());
		Assert.assertEquals(3, finished.get(1).intValue());
		Assert.assertEquals(2, finished.get(2).intValue());
		Assert.assertEquals(1, finished.get(3).intValue());
	}

	@Test
	public void testScc() {
		Digraph<Integer> g = new MapDigraph<>();
		g.put(1, 2, 1);
		g.put(2, 1, 1);
		g.put(1, 3, 1);
		g.put(3, 4, 1);
		g.put(4, 2, 1);
		g.put(3, 5, 1);

		List<Set<Integer>> components = Digraphs.scc(g);
		Assert.assertEquals(2, components.size());
		if (components.get(0).size() == 1) {
			Set<Integer> tmp = components.get(0);
			components.set(0, components.get(1));
			components.set(1, tmp);
		}
		Assert.assertEquals(4, components.get(0).size());
		Assert.assertEquals(1, components.get(1).size());
		Assert.assertTrue(components.get(1).contains(5));
	}

	@Test
	public void testWcc() {
		Digraph<Integer> g = new MapDigraph<>();
		g.put(1, 2, 1);
		g.put(1, 3, 1);
		g.put(4, 2, 1);
		g.put(5, 6, 1);

		List<Set<Integer>> components = Digraphs.wcc(g);
		Assert.assertEquals(2, components.size());
		if (components.get(0).size() == 1) {
			Set<Integer> tmp = components.get(0);
			components.set(0, components.get(1));
			components.set(1, tmp);
		}
		Assert.assertEquals(4, components.get(0).size());
		Assert.assertEquals(2, components.get(1).size());
		Assert.assertTrue(components.get(1).contains(5));
		Assert.assertTrue(components.get(1).contains(6));
	}

	@Test
	public void testReverse() {
		Digraph<Integer> g = new MapDigraph<>();
		g.put(1, 2, 1);
		g.put(1, 3, 1);
		g.put(4, 2, 1);
		g.put(5, 6, 1);
		g.add(7);

		Digraph<Integer> r = g.reverse();
		Assert.assertEquals(7, r.getVertexCount());
		Assert.assertEquals(4, r.getEdgeCount());
		Assert.assertTrue(r.contains(2, 1));
		Assert.assertTrue(r.contains(3, 1));
		Assert.assertTrue(r.contains(2, 4));
		Assert.assertTrue(r.contains(6, 5));
	}

	@Test
	public void testSubgraph() {
		Digraph<Integer> g = new MapDigraph<>();
		g.put(1, 2, 1);
		g.put(1, 3, 1);
		g.put(4, 2, 1);
		g.put(5, 6, 1);
		g.add(7);

		Set<Integer> nodes = new HashSet<>();
		nodes.add(1);
		nodes.add(2);
		nodes.add(3);
		nodes.add(7);
		Digraph<Integer> s = g.subgraph(nodes);

		Assert.assertEquals(4, s.getVertexCount());
		Assert.assertEquals(2, s.getEdgeCount());
		Assert.assertTrue(s.contains(1, 2));
		Assert.assertTrue(s.contains(1, 3));
	}

	@Test
	public void testPartition() {
		Digraph<Integer> g = new MapDigraph<>();
		g.put(1, 2, 1);
		g.put(2, 1, 1);
		g.put(1, 3, 1);
		g.put(1, 4, 1);
		g.put(3, 4, 1);
		g.put(4, 2, 1);
		g.put(3, 5, 1);

		List<Set<Integer>> sets = new ArrayList<>();
		Set<Integer> set = new HashSet<>();
		set.add(1);
		set.add(2);
		sets.add(set);
		set = new HashSet<>();
		set.add(3);
		set.add(4);
		sets.add(set);
		set = new HashSet<>();
		set.add(5);
		sets.add(set);

		DigraphFactory<MapDigraph<Digraph<Integer>>> f1 = MapDigraph.getDefaultDigraphFactory();
		DigraphFactory<MapDigraph<Integer>> f2 = MapDigraph.getDefaultDigraphFactory();

		Digraph<Digraph<Integer>> p = Digraphs.partition(g, sets, f1, f2);

		Assert.assertEquals(3, p.getVertexCount());
		Assert.assertEquals(3, p.getEdgeCount());
		Digraph<Integer> s1 = null, s2 = null, s3 = null;
		for (Digraph<Integer> s : p.vertices()) {
			if (s.contains(1) && s.contains(2)) {
				Assert.assertEquals(2, s.getVertexCount());
				Assert.assertEquals(2, s.getEdgeCount());
				Assert.assertTrue(s.contains(1, 2));
				Assert.assertTrue(s.contains(2, 1));
				s1 = s;
			}
			if (s.contains(3) && s.contains(4)) {
				Assert.assertEquals(2, s.getVertexCount());
				Assert.assertEquals(1, s.getEdgeCount());
				Assert.assertTrue(s.contains(3, 4));
				s2 = s;
			}
			if (s.contains(5)) {
				Assert.assertEquals(1, s.getVertexCount());
				Assert.assertEquals(0, s.getEdgeCount());
				s3 = s;
			}
		}
		Assert.assertTrue(s1 != s2 && s1 != s3 && s2 != s3 && s1 != null && s2 != null && s3 != null);
		Assert.assertEquals(OptionalInt.of(2), p.get(s1, s2));
		Assert.assertEquals(OptionalInt.of(1), p.get(s2, s1));
		Assert.assertEquals(OptionalInt.of(1), p.get(s2, s3));
	}
}
