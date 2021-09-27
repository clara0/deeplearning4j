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

public final class UIEventType {
  private UIEventType() { }
  public static final byte ADD_NAME = 0;
  public static final byte SCALAR = 1;
  public static final byte ARRAY = 2;
  public static final byte ARRAY_LIST = 3;
  public static final byte HISTOGRAM = 4;
  public static final byte IMAGE = 5;
  public static final byte SUMMARY_STATISTICS = 6;
  public static final byte OP_TIMING = 7;
  public static final byte HARDWARE_STATE = 8;

  public static final String[] names = { "ADD_NAME", "SCALAR", "ARRAY", "ARRAY_LIST", "HISTOGRAM", "IMAGE", "SUMMARY_STATISTICS", "OP_TIMING", "HARDWARE_STATE", };

  public static String name(int e) { return names[e]; }
}

