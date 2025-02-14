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

package org.eclipse.deeplearning4j.nd4j.linalg.api.indexing;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.nd4j.common.base.Preconditions;
import org.nd4j.common.tests.tags.NativeTag;
import org.nd4j.common.tests.tags.TagNames;
import org.nd4j.common.util.ArrayUtil;
import org.nd4j.linalg.BaseNd4jTestWithBackends;
import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.api.iter.NdIndexIterator;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.factory.Nd4jBackend;
import org.nd4j.linalg.indexing.*;
import org.nd4j.linalg.ops.transforms.Transforms;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.nd4j.linalg.indexing.NDArrayIndex.*;

/**
 * @author Adam Gibson
 */
@Tag(TagNames.NDARRAY_INDEXING)
@NativeTag
public class IndexingTestsC extends BaseNd4jTestWithBackends {



    @ParameterizedTest
    @MethodSource("org.nd4j.linalg.BaseNd4jTestWithBackends#configs")
    public void testSpecifiedIndexPut(Nd4jBackend backend) {
        INDArray arr = Nd4j.arange(12.0).reshape(2,3,2);
        INDArray original = arr.dup();
        INDArrayIndex[] indices = {NDArrayIndex.all(),NDArrayIndex.indices(0,2),NDArrayIndex.point(1)};
        INDArray put  = arr.put(indices,Nd4j.onesLike(arr.muli(-1)));
        assertNotEquals(original,put);
    }


    @ParameterizedTest
    @MethodSource("org.nd4j.linalg.BaseNd4jTestWithBackends#configs")
    public void testNegativeBounds(Nd4jBackend backend) {
        INDArray arr = Nd4j.linspace(1,10,10, DataType.DOUBLE).reshape(2,5);
        INDArrayIndex interval = NDArrayIndex.interval(0,1,-2,arr.size(1));
        INDArray get = arr.get(NDArrayIndex.all(),interval);
        INDArray assertion = Nd4j.create(new double[][]{
                {1,2,3},
                {6,7,8}
        });
        assertEquals(assertion,get);
    }

    @ParameterizedTest
    @MethodSource("org.nd4j.linalg.BaseNd4jTestWithBackends#configs")
    public void testNewAxis(Nd4jBackend backend) {
        INDArray arr = Nd4j.linspace(1, 12, 12, DataType.DOUBLE).reshape(3, 2, 2);
        INDArray get = arr.get(NDArrayIndex.all(), NDArrayIndex.all(), newAxis(), newAxis(), all());
        long[] shapeAssertion = {3, 2, 1, 1, 2};
        assertArrayEquals(shapeAssertion, get.shape());
    }


    @ParameterizedTest
    @MethodSource("org.nd4j.linalg.BaseNd4jTestWithBackends#configs")
    public void broadcastBug(Nd4jBackend backend) {
        INDArray a = Nd4j.create(new double[] {1.0, 2.0, 3.0, 4.0}, new int[] {2, 2});
        final INDArray col = a.get(NDArrayIndex.all(), NDArrayIndex.point(0));

        final INDArray aBad = col.broadcast(2, 2);
        final INDArray aGood = col.dup().broadcast(2, 2);
        assertTrue(Transforms.abs(aGood.sub(aBad).div(aGood)).maxNumber().doubleValue() < 0.01);
    }


    @ParameterizedTest
    @MethodSource("org.nd4j.linalg.BaseNd4jTestWithBackends#configs")
    public void testIntervalsIn3D(Nd4jBackend backend) {
        INDArray arr = Nd4j.arange(8).reshape(2, 2, 2).castTo(DataType.DOUBLE);
        INDArray assertion = Nd4j.create(new double[][] {{4, 5}, {6, 7}}).reshape(1, 2, 2);
        INDArray rest = arr.get(interval(1, 2), interval(0, 2), interval(0, 2));
        assertEquals(assertion, rest);

    }

    @ParameterizedTest
    @MethodSource("org.nd4j.linalg.BaseNd4jTestWithBackends#configs")
    public void testSmallInterval(Nd4jBackend backend) {
        INDArray arr = Nd4j.arange(8).reshape(2, 2, 2).castTo(DataType.DOUBLE);
        INDArray assertion = Nd4j.create(new double[][] {{4, 5}, {6, 7}}).reshape(1, 2, 2);
        INDArray rest = arr.get(interval(1, 2), all(), all());
        assertEquals(assertion, rest);

    }

    @ParameterizedTest
    @MethodSource("org.nd4j.linalg.BaseNd4jTestWithBackends#configs")
    public void testAllWithNewAxisAndInterval(Nd4jBackend backend) {
        INDArray arr = Nd4j.linspace(1, 24, 24, DataType.DOUBLE).reshape(4, 2, 3);
        INDArray assertion2 = Nd4j.create(new double[][] {{7, 8, 9},}).reshape(1, 1, 3);

        INDArray get2 = arr.get(NDArrayIndex.point(1), newAxis(), NDArrayIndex.interval(0, 1));
        assertEquals(assertion2, get2);
    }

    @ParameterizedTest
    @MethodSource("org.nd4j.linalg.BaseNd4jTestWithBackends#configs")
    public void testAllWithNewAxisInMiddle(Nd4jBackend backend) {
        INDArray arr = Nd4j.linspace(1, 24, 24, DataType.DOUBLE).reshape(4, 2, 3);
        INDArray assertion2 = Nd4j.create(new double[][] {{7, 8, 9}, {10, 11, 12}}).reshape(1, 2, 3);

        INDArray get2 = arr.get(NDArrayIndex.point(1), newAxis(), all(), all());
        assertEquals(assertion2, get2);
    }

    @ParameterizedTest
    @MethodSource("org.nd4j.linalg.BaseNd4jTestWithBackends#configs")
    public void testAllWithNewAxis(Nd4jBackend backend) {
        INDArray arr = Nd4j.linspace(1, 24, 24, DataType.DOUBLE).reshape(4, 2, 3);
        INDArray get = arr.get(newAxis(), all(), point(1));
        INDArray assertion = Nd4j.create(new double[][] {{4, 5, 6}, {10, 11, 12}, {16, 17, 18}, {22, 23, 24}})
                .reshape(1, 4, 3);
        assertEquals(assertion, get);

    }

    @ParameterizedTest
    @MethodSource("org.nd4j.linalg.BaseNd4jTestWithBackends#configs")
    public void testIndexingWithMmul(Nd4jBackend backend) {
        INDArray a = Nd4j.linspace(1, 9, 9, DataType.DOUBLE).reshape(3, 3);
        INDArray b = Nd4j.linspace(1, 5, 5, DataType.DOUBLE).reshape(1, -1);
//        System.out.println(b);
        INDArray view = a.get(all(), NDArrayIndex.interval(0, 1));
        INDArray c = view.mmul(b);
        INDArray assertion = a.get(all(), NDArrayIndex.interval(0, 1)).dup().mmul(b);
        assertEquals(assertion, c);
    }

    @ParameterizedTest
    @MethodSource("org.nd4j.linalg.BaseNd4jTestWithBackends#configs")
    public void testPointPointInterval(Nd4jBackend backend) {
        INDArray wholeArr = Nd4j.linspace(1, 36, 36, DataType.DOUBLE).reshape(4, 3, 3);
        INDArray get = wholeArr.get(point(0), interval(1, 3), interval(1, 3));
        INDArray assertion = Nd4j.create(new double[][] {{5, 6}, {8, 9}});

        assertEquals(assertion, get);
    }

    @ParameterizedTest
    @MethodSource("org.nd4j.linalg.BaseNd4jTestWithBackends#configs")
    public void testIntervalLowerBound(Nd4jBackend backend) {
        INDArray wholeArr = Nd4j.linspace(1, 24, 24, DataType.DOUBLE).reshape(4, 2, 3);
        INDArray subarray = wholeArr.get(interval(1, 3), NDArrayIndex.point(0), NDArrayIndex.indices(0, 2));
        INDArray assertion = Nd4j.create(new double[][] {{7, 9}, {13, 15}});

        assertEquals(assertion, subarray);

    }


    @ParameterizedTest
    @MethodSource("org.nd4j.linalg.BaseNd4jTestWithBackends#configs")
    public void testGetPointRowVector(Nd4jBackend backend) {
        INDArray arr = Nd4j.linspace(1, 1000, 1000, DataType.DOUBLE).reshape(1, -1);

        INDArray arr2 = arr.get(point(0), interval(0, 100));

        assertEquals(100, arr2.length()); //Returning: length 0
        assertEquals(Nd4j.linspace(1, 100, 100, DataType.DOUBLE), arr2);
    }

    @ParameterizedTest
    @MethodSource("org.nd4j.linalg.BaseNd4jTestWithBackends#configs")
    public void testSpecifiedIndexVector(Nd4jBackend backend) {
        INDArray rootMatrix = Nd4j.linspace(1, 16, 16, DataType.DOUBLE).reshape(4, 4);
        INDArray threeD = Nd4j.linspace(1, 16, 16, DataType.DOUBLE).reshape(2, 2, 2, 2);
        INDArray get = rootMatrix.get(all(), new SpecifiedIndex(0, 2));
        INDArray assertion = Nd4j.create(new double[][] {{1, 3}, {5, 7}, {9, 11}, {13, 15}});

        assertEquals(assertion, get);

        INDArray assertion2 = Nd4j.create(new double[][] {{1, 3, 4}, {5, 7, 8}, {9, 11, 12}, {13, 15, 16}});
        INDArray get2 = rootMatrix.get(all(), new SpecifiedIndex(0, 2, 3));

        assertEquals(assertion2, get2);

    }


    @ParameterizedTest
    @MethodSource("org.nd4j.linalg.BaseNd4jTestWithBackends#configs")
    public void testPutRowIndexing(Nd4jBackend backend) {
        INDArray arr = Nd4j.ones(1, 10);
        INDArray row = Nd4j.create(1, 10);

        arr.putRow(0, row); //OK
        arr.put(new INDArrayIndex[] {point(0), all()}, row); //Exception
        assertEquals(arr, row);
    }

    @ParameterizedTest
    @MethodSource("org.nd4j.linalg.BaseNd4jTestWithBackends#configs")
    public void testVectorIndexing2(Nd4jBackend backend) {
        INDArray wholeVector = Nd4j.linspace(1, 5, 5, DataType.DOUBLE).get(interval(1, 2, 3, true));
        INDArray assertion = Nd4j.create(new double[] {2, 4});
        assertEquals(assertion, wholeVector);
        INDArray wholeVectorTwo = Nd4j.linspace(1, 5, 5, DataType.DOUBLE).get(interval(1, 2, 4, true));
        assertEquals(assertion, wholeVectorTwo);
        INDArray wholeVectorThree = Nd4j.linspace(1, 5, 5, DataType.DOUBLE).get(interval(1, 2, 4, false));
        assertEquals(assertion, wholeVectorThree);
        INDArray threeFiveAssertion = Nd4j.create(new double[] {3, 5});
        INDArray threeFive = Nd4j.linspace(1, 5, 5, DataType.DOUBLE).get(interval(2, 2, 4, true));
        assertEquals(threeFiveAssertion, threeFive);
    }


    @ParameterizedTest
    @MethodSource("org.nd4j.linalg.BaseNd4jTestWithBackends#configs")
    public void testOffsetsC(Nd4jBackend backend) {
        INDArray arr = Nd4j.linspace(1, 4, 4, DataType.DOUBLE).reshape(2, 2);
        assertEquals(3, NDArrayIndex.offset(arr, 1, 1));
        assertEquals(3, NDArrayIndex.offset(arr, point(1), point(1)));

        INDArray arr2 = Nd4j.linspace(1, 6, 6, DataType.DOUBLE).reshape(3, 2);
        assertEquals(3, NDArrayIndex.offset(arr2, 1, 1));
        assertEquals(3, NDArrayIndex.offset(arr2, point(1), point(1)));
        assertEquals(6, NDArrayIndex.offset(arr2, 2, 2));
        assertEquals(6, NDArrayIndex.offset(arr2, point(2), point(2)));



    }

    @ParameterizedTest
    @MethodSource("org.nd4j.linalg.BaseNd4jTestWithBackends#configs")
    public void testIndexFor(Nd4jBackend backend) {
        long[] shape = {1, 2};
        INDArrayIndex[] indexes = NDArrayIndex.indexesFor(shape);
        for (int i = 0; i < indexes.length; i++) {
            assertEquals(shape[i], indexes[i].offset());
        }
    }

    @ParameterizedTest
    @MethodSource("org.nd4j.linalg.BaseNd4jTestWithBackends#configs")
    public void testGetScalar(Nd4jBackend backend) {
        INDArray arr = Nd4j.linspace(1, 5, 5, DataType.DOUBLE);
        INDArray d = arr.get(point(1));
        assertTrue(d.isScalar());
        assertEquals(2.0, d.getDouble(0), 1e-1);

    }

    @ParameterizedTest
    @MethodSource("org.nd4j.linalg.BaseNd4jTestWithBackends#configs")
    public void testVectorIndexing(Nd4jBackend backend) {
        INDArray arr = Nd4j.linspace(1, 10, 10, DataType.DOUBLE).reshape(1, -1);
        INDArray assertion = Nd4j.create(new double[] {2, 3, 4, 5});
        INDArray viewTest = arr.get(point(0), interval(1, 5));
        assertEquals(assertion, viewTest);
    }

    @ParameterizedTest
    @MethodSource("org.nd4j.linalg.BaseNd4jTestWithBackends#configs")
    public void testNegativeIndices(Nd4jBackend backend) {
        INDArray test = Nd4j.create(10, 10, 10);
        test.putScalar(new int[] {0, 0, -1}, 1.0);
        assertEquals(1.0, test.getScalar(0, 0, -1).sumNumber());
    }


    @ParameterizedTest
    @MethodSource("org.nd4j.linalg.BaseNd4jTestWithBackends#configs")
    public void testGetRowSingle(Nd4jBackend backend) {
        INDArray ones = Nd4j.ones(1, 8);
        System.out.println(ones.shapeInfoToString());
        ones = ones.getRow(0);
        assertArrayEquals(new long[]{8},ones.shape());

        ones = Nd4j.ones(2, 8);
        System.out.println(ones.shapeInfoToString());
        ones = ones.getRow(0);
        assertArrayEquals(new long[]{8},ones.shape());
    }

    @ParameterizedTest
    @MethodSource("org.nd4j.linalg.BaseNd4jTestWithBackends#configs")
    public void testGetIndices2d(Nd4jBackend backend) {
        INDArray twoByTwo = Nd4j.linspace(1, 6, 6, DataType.DOUBLE).reshape(3, 2);
        INDArray firstRow = twoByTwo.getRow(0);
        INDArray secondRow = twoByTwo.getRow(1);
        INDArray firstAndSecondRow = twoByTwo.getRows(1, 2);
        INDArray firstRowViaIndexing = twoByTwo.get(interval(0, 1), NDArrayIndex.all());
        assertEquals(firstRow.reshape(1,2), firstRowViaIndexing);
        INDArray secondRowViaIndexing = twoByTwo.get(point(1), NDArrayIndex.all());
        assertEquals(secondRow, secondRowViaIndexing);

        INDArray firstAndSecondRowTest = twoByTwo.get(interval(1, 3), NDArrayIndex.all());
        assertEquals(firstAndSecondRow, firstAndSecondRowTest);

        INDArray individualElement = twoByTwo.get(interval(1, 2), interval(1, 2));
        assertEquals(Nd4j.create(new double[] {4}, new int[]{1,1}), individualElement);
    }

    @ParameterizedTest
    @MethodSource("org.nd4j.linalg.BaseNd4jTestWithBackends#configs")
    public void testGetRow(Nd4jBackend backend) {
        Nd4j.getRandom().setSeed(12345);
        INDArray in = Nd4j.linspace(0, 14, 15, DataType.DOUBLE).reshape(3, 5);
        int[] toGet = {0, 1};
        INDArray out = in.getRows(toGet);
        assertEquals(in.getRow(0), out.getRow(0));
        assertEquals(in.getRow(1), out.getRow(1));

        int[] toGet2 = {0, 1, 2, 0, 1, 2};
        INDArray out2 = in.getRows(toGet2);
        for (int i = 0; i < toGet2.length; i++) {
            assertEquals(in.getRow(toGet2[i]), out2.getRow(i));
        }
    }


    @ParameterizedTest
    @MethodSource("org.nd4j.linalg.BaseNd4jTestWithBackends#configs")
    public void testGetRowEdgeCase(Nd4jBackend backend) {
        INDArray rowVec = Nd4j.linspace(1, 5, 5, DataType.DOUBLE).reshape(1, -1);
        INDArray get = rowVec.getRow(0); //Returning shape [1,1]

        assertArrayEquals(new long[] {1, 5}, get.shape());
        assertEquals(rowVec, get);
    }

    @ParameterizedTest
    @MethodSource("org.nd4j.linalg.BaseNd4jTestWithBackends#configs")
    public void testGetColumnEdgeCase(Nd4jBackend backend) {
        INDArray colVec = Nd4j.linspace(1, 5, 5, DataType.DOUBLE).reshape(1, -1).transpose();
        INDArray get = colVec.getColumn(0); //Returning shape [1,1]

        assertArrayEquals(new long[] {5, 1}, get.shape());
        assertEquals(colVec, get);
    }

    @ParameterizedTest
    @MethodSource("org.nd4j.linalg.BaseNd4jTestWithBackends#configs")
    public void testConcatColumns(Nd4jBackend backend) {
        INDArray input1 = Nd4j.zeros(2, 1).castTo(DataType.DOUBLE);
        INDArray input2 = Nd4j.ones(2, 1).castTo(DataType.DOUBLE);
        INDArray concat = Nd4j.concat(1, input1, input2);
        INDArray assertion = Nd4j.create(new double[][] {{0, 1}, {0, 1}});
        assertEquals(assertion, concat);
    }

    @ParameterizedTest
    @MethodSource("org.nd4j.linalg.BaseNd4jTestWithBackends#configs")
    public void testGetIndicesVector(Nd4jBackend backend) {
        INDArray line = Nd4j.linspace(1, 4, 4, DataType.DOUBLE).reshape(1, -1);
        INDArray test = Nd4j.create(new double[] {2, 3});
        INDArray result = line.get(point(0), interval(1, 3));
        assertEquals(test, result);
    }

    @ParameterizedTest
    @MethodSource("org.nd4j.linalg.BaseNd4jTestWithBackends#configs")
    public void testArangeMul(Nd4jBackend backend) {
        INDArray arange = Nd4j.arange(1, 17).reshape(4, 4).castTo(DataType.DOUBLE);
        INDArrayIndex index = interval(0, 2);
        INDArray get = arange.get(index, index);
        INDArray ones = Nd4j.ones(DataType.DOUBLE, 2, 2).mul(0.25);
        INDArray mul = get.mul(ones);
        INDArray assertion = Nd4j.create(new double[][] {{0.25, 0.5}, {1.25, 1.5}});
        assertEquals(assertion, mul);
    }

    @ParameterizedTest
    @MethodSource("org.nd4j.linalg.BaseNd4jTestWithBackends#configs")
    public void testIndexingThorough() {
        long[] fullShape = {3,4,5,6,7};

        //Note: 888,880 total test cases here - randomly run a fraction of the tests to minimize runtime
        // whilst still maintaining good coverage
        Random r = new Random(12345);
        double fractionRun = 0.01;

        long totalTestCaseCount = 0;
        for( int rank=1; rank<=5; rank++ ){
            for(char order : new char[]{'c', 'f'}) {
                int[] n = new int[rank];
                long[] inShape = new long[rank];
                long prod = 1;
                for (int i = 0; i < rank; i++) {
                    n[i] = 10;
                    inShape[i] = fullShape[i];
                    prod *= fullShape[i];
                }

                for (int newAxisTestCase : new int[]{0, 1, 2, 3}) {    //0 = none, 1=at start, 2=at end, 3=between
                    int outRank = rank;
                    switch (newAxisTestCase){
                        case 1: //At start
                        case 2: //At end
                            outRank++;
                            break;
                        case 3: //Between
                            outRank += rank - 1;
                            break;
                    }

                    INDArrayIndex[] indexes = new INDArrayIndex[outRank];
                    NdIndexIterator iter = new NdIndexIterator(n);      //This is used as a simple counter
                    while (iter.hasNext()) {
                        long[] next = iter.next();

                        if(r.nextFloat() > fractionRun){
                            //Randomly skip fraction of tests to minimize runtime
                            continue;
                        }

                        int pos = 0;

                        if(newAxisTestCase == 1){
                            indexes[pos++] = NDArrayIndex.newAxis();
                        }

                        for (int i = 0; i < next.length; i++) {
                            switch ((int) next[i]) {
                                case 0:
                                    indexes[pos++] = NDArrayIndex.point(0);
                                    break;
                                case 1:
                                    indexes[pos++] = NDArrayIndex.point(fullShape[i] - 1);
                                    break;
                                case 2:
                                    indexes[pos++] = NDArrayIndex.point(fullShape[i] / 2);
                                    break;
                                case 3:
                                    indexes[pos++] = NDArrayIndex.interval(0, fullShape[i]);
                                    break;
                                case 4:
                                    indexes[pos++] = NDArrayIndex.interval(0, fullShape[i] - 1, true);
                                    break;
                                case 5:
                                    indexes[pos++] = NDArrayIndex.interval(1, 2, fullShape[i]);
                                    break;
                                case 6:
                                    indexes[pos++] = NDArrayIndex.interval(1, 2, fullShape[i] - 1, true);
                                    break;
                                case 7:
                                    indexes[pos++] = NDArrayIndex.all();
                                    break;
                                case 8:
                                    indexes[pos++] = NDArrayIndex.indices(0);
                                    break;
                                case 9:
                                    indexes[pos++] = NDArrayIndex.indices(2,1);
                                    break;
                                default:
                                    throw new RuntimeException();
                            }
                            if(newAxisTestCase == 3 && i < next.length - 1){  //Between
                                indexes[pos++] = NDArrayIndex.newAxis();
                            }
                        }

                        if(newAxisTestCase == 2){  //At end
                            indexes[pos++] = NDArrayIndex.newAxis();
                        }

                        INDArray arr = Nd4j.linspace(DataType.FLOAT, 1, prod, prod).reshape('c', inShape).dup(order);
                        INDArray sub = arr.get(indexes);

                        String msg = "Test case: rank = " + rank + ", order = " + order + ", inShape = " + Arrays.toString(inShape) +
                                ", indexes = " + Arrays.toString(indexes) + ", newAxisTest=" + newAxisTestCase;

                        long[] expShape = getShape(arr, indexes);
                        long[] subShape = sub.shape();
                        assertArrayEquals(expShape, subShape,msg);

                        msg = "Test case: rank = " + rank + ", order = " + order + ", inShape = " + Arrays.toString(inShape) +
                                ", outShape = " + Arrays.toString(expShape) +
                                ", indexes = " + Arrays.toString(indexes) + ", newAxisTest=" + newAxisTestCase;

                        NdIndexIterator posIter = new NdIndexIterator(expShape);
                        while (posIter.hasNext()) {
                            long[] outIdxs = posIter.next();
                            double act = sub.getDouble(outIdxs);
                            double exp = getDouble(indexes, arr, outIdxs);

                            assertEquals(exp, act, 1e-6,msg);
                        }
                        totalTestCaseCount++;
                    }
                }
            }
        }

        assertTrue( totalTestCaseCount > 5000,String.valueOf(totalTestCaseCount));
    }

    private static long[] getShape(INDArray in, INDArrayIndex[] idxs){
        int countPoint = 0;
        int countNewAxis = 0;
        for(INDArrayIndex i : idxs){
            if(i instanceof PointIndex)
                countPoint++;
            if(i instanceof NewAxis)
                countNewAxis++;
        }

        Preconditions.checkState(in.rank() == idxs.length - countNewAxis);

        long[] out = new long[in.rank() - countPoint + countNewAxis];
        int outAxisCount = 0;
        int inAxisCount = 0;
        for( int i=0; i<idxs.length; i++ ){
            if(idxs[i] instanceof PointIndex) {
                //Point index doesn't appear in output
                inAxisCount++;
                continue;
            }
            if(idxs[i] instanceof NDArrayIndexAll){
                out[outAxisCount++] = in.size(inAxisCount++);
            } else if(idxs[i] instanceof IntervalIndex){
                IntervalIndex ii = (IntervalIndex)idxs[i];
                long begin = ii.offset();   //Inclusive
                long end = ii.end();        //Inclusive
                if(!ii.isInclusive())
                    end--;
                long stride = ii.stride();
                out[outAxisCount++] = (end-begin)/stride + 1;
                inAxisCount++;
            } else if(idxs[i] instanceof NewAxis) {
                //Don't increment inAxisCount as newAxis doesn't correspend to input axis
                out[outAxisCount++] = 1;
            } else if(idxs[i] instanceof SpecifiedIndex){
                SpecifiedIndex si = (SpecifiedIndex)idxs[i];
                out[outAxisCount++] = si.getIndexes().length;
                inAxisCount++;
            } else {
                throw new RuntimeException();
            }
        }
        return out;
    }

    public static double getDouble(INDArrayIndex[] idxs, INDArray source, long[] viewIdx){
        long[] originalIdxs = new long[source.rank()];
        int origIdxC = 0;
        int viewC = 0;
        for( int i=0; i<idxs.length; i++ ){
            if(idxs[i] instanceof PointIndex){
                PointIndex p = (PointIndex)idxs[i];
                originalIdxs[origIdxC++] = p.offset();
                //View counter not increased: doesn't appear in output
            } else if(idxs[i] instanceof NDArrayIndexAll){
                originalIdxs[origIdxC++] = viewIdx[viewC++];
            } else if(idxs[i] instanceof IntervalIndex) {
                IntervalIndex ii = (IntervalIndex) idxs[i];
                originalIdxs[origIdxC++] = ii.offset() + viewIdx[viewC++] * ii.stride();
            } else if(idxs[i] instanceof NewAxis) {
                Preconditions.checkState(viewIdx[viewC++] == 0);
                continue;   //Skip new axis, size 1 dimension doesn't appear in source
            } else if(idxs[i] instanceof SpecifiedIndex){
                SpecifiedIndex si = (SpecifiedIndex)idxs[i];
                long[] s = si.getIndexes();
                originalIdxs[origIdxC++] = s[(int)viewIdx[viewC++]];
            } else {
                throw new RuntimeException();
            }
        }

        double d = source.getDouble(originalIdxs);
        return d;
    }



    @Override
    public char ordering() {
        return 'c';
    }
}
