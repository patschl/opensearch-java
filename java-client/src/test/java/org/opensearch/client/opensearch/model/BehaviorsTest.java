/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

/*
 * Licensed to Elasticsearch B.V. under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch B.V. licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*
 * Modifications Copyright OpenSearch Contributors. See
 * GitHub history for details.
 */

package org.opensearch.client.opensearch.model;

import org.junit.Test;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch._types.ErrorCause;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.GeoLocation;
import org.opensearch.client.opensearch._types.GeoShapeRelation;
import org.opensearch.client.opensearch._types.SortOptions;
import org.opensearch.client.opensearch._types.SortOptionsBuilders;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch._types.query_dsl.ShapeQuery;
import org.opensearch.client.opensearch._types.query_dsl.TermQuery;
import org.opensearch.client.opensearch._types.query_dsl.WrapperQuery;
import org.opensearch.client.util.MapBuilder;

public class BehaviorsTest extends ModelTestCase {

    @Test
    public void testSingleKeyDictionary() {
        TermQuery q = new TermQuery.Builder().queryName("query-name").field("field-name").value(FieldValue.of("some-value")).build();

        q = checkJsonRoundtrip(q, "{\"field-name\":{\"_name\":\"query-name\",\"value\":\"some-value\"}}");

        assertEquals("query-name", q.queryName());
        assertEquals("field-name", q.field());
        assertEquals("some-value", q.value().stringValue());
    }

    @Test
    public void testAdditionalPropertyOnClass() {
        ShapeQuery q = new ShapeQuery.Builder().queryName("query-name")
            .field("field-name")
            .shape(_0 -> _0.relation(GeoShapeRelation.Disjoint))
            .ignoreUnmapped(true)
            .build();

        q = checkJsonRoundtrip(q, "{\"field-name\":{\"relation\":\"disjoint\"},\"_name\":\"query-name\",\"ignore_unmapped\":true}");

        assertEquals("query-name", q.queryName());
        assertTrue(q.ignoreUnmapped());
        assertEquals(GeoShapeRelation.Disjoint, q.shape().relation());
        System.out.println(toJson(q));
    }

    @Test
    public void testWrapperQuery() {
        WrapperQuery q = new WrapperQuery.Builder().query("encoded_query").build();

        q = checkJsonRoundtrip(q, "{\"query\":\"encoded_query\"}");

        assertEquals("encoded_query", q.query());
    }

    @Test
    public void testAdditionalPropertyOnContainer() {
        // Regular variant
        {
            SortOptions so = SortOptions.of(_0 -> _0.doc(_1 -> _1.order(SortOrder.Asc)));

            so = checkJsonRoundtrip(so, "{\"_doc\":{\"order\":\"asc\"}}");
            assertEquals(SortOptions.Kind.Doc, so._kind());
            assertEquals(SortOrder.Asc, so.doc().order());
        }

        // Regular variant
        {
            SortOptions so = SortOptionsBuilders.geoDistance()
                .field("foo")
                .location(GeoLocation.of(_b -> _b.text("someWKT")))
                .build()
                ._toSortOptions();

            so = checkJsonRoundtrip(so, "{\"_geo_distance\":{\"foo\":[\"someWKT\"]}}");
            assertEquals(SortOptions.Kind.GeoDistance, so._kind());
            assertEquals("foo", so.geoDistance().field());
            assertEquals("someWKT", so.geoDistance().location().get(0).text());
        }

        {
            SortOptions so = SortOptions.of(_0 -> _0.score(_1 -> _1.order(SortOrder.Asc)));

            so = checkJsonRoundtrip(so, "{\"_score\":{\"order\":\"asc\"}}");
            assertEquals(SortOptions.Kind.Score, so._kind());
            assertEquals(SortOrder.Asc, so.score().order());
        }

        {
            SortOptions so = SortOptions.of(_0 -> _0.script(_1 -> _1.script(_2 -> _2.inline(_3 -> _3.source("blah")))));
            so = checkJsonRoundtrip(so, "{\"_script\":{\"script\":{\"source\":\"blah\"}}}");
            assertEquals("blah", so.script().script().inline().source());

        }

        // Additional property variant
        {
            SortOptions so = SortOptions.of(_0 -> _0.field(_1 -> _1.field("foo").order(SortOrder.Desc)));

            so = checkJsonRoundtrip(so, "{\"foo\":{\"order\":\"desc\"}}");
            assertEquals(SortOptions.Kind.Field, so._kind());
            assertEquals("foo", so.field().field());
            assertEquals(SortOrder.Desc, so.field().order());
        }
    }

    @Test
    public void testAdditionalProperties() {
        // Check that additional property map is initialized even if not set explicitly
        ErrorCause err = new ErrorCause.Builder().reason("Foo").type("Bar").build();
        assertEquals(0, err.metadata().size());

        err = new ErrorCause.Builder().reason("Some failure")
            .type("Some type")
            .metadata(MapBuilder.of("index", JsonData.of("test"), "retries", JsonData.of(1)))
            .build();

        err = checkJsonRoundtrip(err, "{\"index\":\"test\",\"retries\":1,\"type\":\"Some type\",\"reason\":\"Some failure\"}");

        assertEquals("Some failure", err.reason());
        assertEquals(1, err.metadata().get("retries").to(int.class).intValue());
        assertEquals("test", err.metadata().get("index").to(String.class));
    }

    @Test
    public void testShortcutProperty() {

        // All-in-one: a variant, wrapping a single-key dictionary with a shortcut property
        String json = "{\"term\":{\"some-field\":\"some-value\"}}";
        Query q = fromJson(json, Query.class);

        assertEquals("some-field", q.term().field());
        assertEquals("some-value", q.term().value().stringValue());
    }
}
