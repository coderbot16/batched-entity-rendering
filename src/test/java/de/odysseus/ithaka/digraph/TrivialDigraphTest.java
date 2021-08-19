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

import java.util.HashSet;
import java.util.OptionalInt;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class TrivialDigraphTest {

	@Test
	public void testAdd() {
		Assert.assertTrue(new TrivialDigraph<>().add("foo"));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testAdd2() {
		TrivialDigraph<Object> g = new TrivialDigraph<>();
		g.add("foo");
		g.add("bar");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAdd3() {
		new TrivialDigraph<>().add(null);
	}

	@Test
	public void testContainsObjectObject() {
		Assert.assertFalse(new TrivialDigraph<>().contains("foo", "bar"));
	}

	@Test
	public void testContainsObject() {
		TrivialDigraph<Object> g = new TrivialDigraph<>();
		Assert.assertFalse(g.contains("foo"));
		g.add("foo");
		Assert.assertTrue(g.contains("foo"));
	}

	@Test
	public void testGet() {
		Assert.assertEquals(OptionalInt.empty(), new TrivialDigraph<>().get("foo", "bar"));
	}

	@Test
	public void testGetInDegree() {
		Assert.assertEquals(0, new TrivialDigraph<>().getInDegree("foo"));
	}

	@Test
	public void testGetOutDegree() {
		Assert.assertEquals(0, new TrivialDigraph<>().getOutDegree("foo"));
	}

	@Test
	public void testGetEdgeCount() {
		Assert.assertEquals(0, new TrivialDigraph<>().getEdgeCount());
	}

	@Test
	public void testgetVertexCount() {
		TrivialDigraph<Object> g = new TrivialDigraph<>();
		Assert.assertEquals(0, g.getVertexCount());
		g.add("foo");
		Assert.assertEquals(1, g.getVertexCount());
	}

	@Test
	public void testNodes() {
		TrivialDigraph<Object> g = new TrivialDigraph<>();
		Assert.assertFalse(g.vertices().iterator().hasNext());
		g.add("foo");
		Assert.assertTrue(g.vertices().iterator().hasNext());
		Assert.assertEquals("foo", g.vertices().iterator().next());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testPut() {
		new TrivialDigraph<>().put("foo", "bar", 2);
	}

	@Test
	public void testRemoveVV() {
		Assert.assertEquals(OptionalInt.empty(), new TrivialDigraph<>().remove("foo", "bar"));
	}

	@Test
	public void testRemoveV() {
		TrivialDigraph<Object> g = new TrivialDigraph<>();
		Assert.assertFalse(g.remove("foo"));
		g.add("foo");
		Assert.assertTrue(g.remove("foo"));
		Assert.assertEquals(0, g.getVertexCount());
	}

	@Test
	public void testRemoveAll() {
		TrivialDigraph<Object> g = new TrivialDigraph<>();
		g.add("foo");
		HashSet<Object> set = new HashSet<>();
		set.add("bar");
		g.removeAll(set);
		Assert.assertTrue(g.contains("foo"));
		set.add("foo");
		g.removeAll(set);
		Assert.assertFalse(g.contains("foo"));
		Assert.assertEquals(0, g.getVertexCount());
	}

	@Test
	public void testReverse() {
		TrivialDigraph<Object> g = new TrivialDigraph<>();
		Assert.assertSame(g, g.reverse());
	}

	@Test
	public void testSubgraph() {
		TrivialDigraph<Object> g = new TrivialDigraph<>();
		Set<Object> set = new HashSet<>();
		set.add("foo");
		Assert.assertEquals(0, g.subgraph(set).getVertexCount());
		g.add("foo");
		Assert.assertEquals(1, g.subgraph(set).getVertexCount());
	}

	@Test
	public void testSources() {
		Assert.assertFalse(new TrivialDigraph<>().sources("foo").iterator().hasNext());
	}

	@Test
	public void testTargets() {
		Assert.assertFalse(new TrivialDigraph<>().targets("foo").iterator().hasNext());
	}

	@Test
	public void testIsAcyclic() {
		TrivialDigraph<Object> g = new TrivialDigraph<>();
		Assert.assertTrue(g.isAcyclic());
		g.add("foo");
		Assert.assertTrue(g.isAcyclic());
	}
}
