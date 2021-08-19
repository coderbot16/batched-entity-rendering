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
package de.odysseus.ithaka.digraph.io.tgf;

import java.io.IOException;
import java.io.StringWriter;

import de.odysseus.ithaka.digraph.Digraph;
import de.odysseus.ithaka.digraph.MapDigraph;
import org.junit.Assert;
import org.junit.Test;

public class TgfExporterTest {
	@Test
	public void testUnweighted() throws IOException {
		Digraph<Integer> digraph = new MapDigraph<>();
		digraph.put(1, 2, 1);
		digraph.put(1, 3, 1);
		digraph.put(4, 2, 1);
		digraph.put(5, 6, 1);
		digraph.add(7);

		TgfLabelProvider<Integer> provider = new TgfLabelProvider<Integer>() {
			@Override
			public String getVertexLabel(Integer vertex) {
				return String.valueOf(vertex);
			}

			@Override
			public String getEdgeLabel(int edgeWeight) {
				return null;
			}
		};

		StringWriter writer = new StringWriter();
		new TgfExporter("  ").export(provider, digraph, writer);
		Assert.assertEquals("1 1  2 2  3 3  4 4  5 5  6 6  7 7  #  1 2  1 3  4 2  5 6  ", writer.toString());
	}

	@Test
	public void testWeighted() throws IOException {
		Digraph<Integer> digraph = new MapDigraph<>();
		digraph.put(1, 2, 1);
		digraph.put(1, 3, 2);
		digraph.put(4, 2, 3);
		digraph.put(5, 6, 4);
		digraph.add(7);

		TgfLabelProvider<Integer> provider = new TgfLabelProvider<Integer>() {
			@Override
			public String getVertexLabel(Integer vertex) {
				return String.valueOf(vertex);
			}

			@Override
			public String getEdgeLabel(int edgeWeight) {
				return Integer.toString(edgeWeight);
			}
		};

		StringWriter writer = new StringWriter();
		new TgfExporter("  ").export(provider, digraph, writer);
		Assert.assertEquals("1 1  2 2  3 3  4 4  5 5  6 6  7 7  #  1 2 1  1 3 2  4 2 3  5 6 4  ", writer.toString());
	}
}
