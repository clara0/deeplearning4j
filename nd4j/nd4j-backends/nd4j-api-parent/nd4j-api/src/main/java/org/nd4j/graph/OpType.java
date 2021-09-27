/*
 *  ******************************************************************************
 *  *
 *  *
 *  * This program and the accompanying materials are made available under the
 *  * terms of the Apache License, Version 2.0 which is available at
 *  * https://www.apache.org/licenses/LICENSE-2.0.
 *  *
 *  *  See the NOTICE file distributed with this work for additional
 *  *  information regarding copyright ownership.
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  * License for the specific language governing permissions and limitations
 *  * under the License.
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *  *****************************************************************************
 */

package org.nd4j.graph;

public final class OpType {
  private OpType() { }
  public static final byte TRANSFORM_FLOAT = 0;
  public static final byte TRANSFORM_SAME = 1;
  public static final byte TRANSFORM_BOOL = 2;
  public static final byte TRANSFORM_STRICT = 3;
  public static final byte TRANSFORM_ANY = 4;
  public static final byte REDUCE_FLOAT = 5;
  public static final byte REDUCE_SAME = 6;
  public static final byte REDUCE_LONG = 7;
  public static final byte REDUCE_BOOL = 8;
  public static final byte INDEX_REDUCE = 9;
  public static final byte SCALAR = 10;
  public static final byte SCALAR_BOOL = 11;
  public static final byte BROADCAST = 12;
  public static final byte BROADCAST_BOOL = 13;
  public static final byte PAIRWISE = 14;
  public static final byte PAIRWISE_BOOL = 15;
  public static final byte REDUCE_3 = 16;
  public static final byte SUMMARYSTATS = 17;
  public static final byte SHAPE = 18;
  public static final byte AGGREGATION = 19;
  public static final byte RANDOM = 20;
  public static final byte CUSTOM = 21;
  public static final byte GRAPH = 22;
  public static final byte VARIABLE = 40;
  public static final byte BOOLEAN = 60;
  public static final byte LOGIC = 119;

  public static final String[] names = { "TRANSFORM_FLOAT", "TRANSFORM_SAME", "TRANSFORM_BOOL", "TRANSFORM_STRICT", "TRANSFORM_ANY", "REDUCE_FLOAT", "REDUCE_SAME", "REDUCE_LONG", "REDUCE_BOOL", "INDEX_REDUCE", "SCALAR", "SCALAR_BOOL", "BROADCAST", "BROADCAST_BOOL", "PAIRWISE", "PAIRWISE_BOOL", "REDUCE_3", "SUMMARYSTATS", "SHAPE", "AGGREGATION", "RANDOM", "CUSTOM", "GRAPH", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "VARIABLE", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "BOOLEAN", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "LOGIC", };

  public static String name(int e) { return names[e]; }
}

