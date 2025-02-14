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

package org.nd4j.nativeblas;

import java.util.Properties;
import java.io.File;
import lombok.Getter;
import lombok.Setter;
import org.bytedeco.javacpp.Loader;
import org.nd4j.common.config.ND4JClassLoading;
import org.nd4j.common.config.ND4JEnvironmentVars;
import org.nd4j.common.config.ND4JSystemProperties;
import org.nd4j.common.io.ReflectionUtils;
import org.nd4j.context.Nd4jContext;
import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NativeOpsHolder {
    private static Logger log = LoggerFactory.getLogger(NativeOpsHolder.class);
    private static final NativeOpsHolder INSTANCE = new NativeOpsHolder();

    @Getter
    @Setter
    private  NativeOps deviceNativeOps;

    public static int getCores(int totals) {
        // that's special case for Xeon Phi
        if (totals >= 256)
            return 64;

        int ht_off = totals / 2; // we count off HyperThreading without any excuses
        if (ht_off <= 4)
            return 4; // special case for Intel i5. and nobody likes i3 anyway

        if (ht_off > 24) {
            int rounds = 0;
            while (ht_off > 24) { // we loop until final value gets below 24 cores, since that's reasonable
                                  // threshold as of 2016
                if (ht_off > 24) {
                    ht_off /= 2; // we dont' have any cpus that has higher number then 24 physical cores
                    rounds++;
                }
            }
            // 20 threads is special case in this branch
            if (ht_off == 20 && rounds < 2)
                ht_off /= 2;
        } else { // low-core models are known, but there's a gap, between consumer cpus and xeons
            if (ht_off <= 6) {
                // that's more likely consumer-grade cpu, so leave this value alone
                return ht_off;
            } else {
                if (isOdd(ht_off)) // if that's odd number, it's final result
                    return ht_off;

                // 20 threads & 16 threads are special case in this branch, where we go min
                // value
                if (ht_off == 20 || ht_off == 16)
                    ht_off /= 2;
            }
        }
        return ht_off;
    }

    private static boolean isOdd(int value) {
        return (value % 2 != 0);
    }

    private NativeOpsHolder() {
        try {

           if(getDeviceNativeOps() == null && Boolean.parseBoolean(System.getProperty(ND4JSystemProperties.INIT_NATIVEOPS_HOLDER,"true")) ){
               Properties props = Nd4jContext.getInstance().getConf();

               String name = System.getProperty(Nd4j.NATIVE_OPS, props.get(Nd4j.NATIVE_OPS).toString());
               Class<? extends NativeOps> nativeOpsClass = ND4JClassLoading
                       .loadClassByName(name)
                       .asSubclass(NativeOps.class);
               deviceNativeOps = ReflectionUtils.newInstance(nativeOpsClass);
               initOps();


           }


        } catch (Exception | Error e) {
            throw new RuntimeException(
                    "ND4J is probably missing dependencies. For more information, please refer to: https://deeplearning4j.konduit.ai/nd4j/backend",
                    e);
        }


    }

    public void initOps() {
        deviceNativeOps.initializeDevicesAndFunctions();
        int numThreads;
        String numThreadsString = System.getenv(ND4JEnvironmentVars.OMP_NUM_THREADS);
        if (numThreadsString != null && !numThreadsString.isEmpty()) {
            numThreads = Integer.parseInt(numThreadsString);
            deviceNativeOps.setOmpNumThreads(numThreads);
        } else {
            int cores = Loader.totalCores();
            int chips = Loader.totalChips();
            if (chips > 0 && cores > 0) {
                deviceNativeOps.setOmpNumThreads(Math.max(1, cores / chips));
            } else
                deviceNativeOps.setOmpNumThreads(
                        getCores(Runtime.getRuntime().availableProcessors()));
        }

        String logInitProperty = System.getProperty(ND4JSystemProperties.LOG_INITIALIZATION, "true");
        boolean logInit = Boolean.parseBoolean(logInitProperty);

        try {

            if (Loader.getPlatform().contains("vednn")) {
                String pPath = Loader.load(deviceNativeOps.getClass());
                pPath = pPath.replace(File.separatorChar, '/');
                String matchString = "org/nd4j/linalg/cpu/nativecpu/bindings/" + Loader.getPlatform();
                int start = pPath.indexOf(matchString);
               if(start < 0) {
                   int end = pPath.lastIndexOf("/");
                   String bindingsFolder = pPath.substring(start, end);
                   File file = Loader.cacheResource(bindingsFolder + "/libnd4jcpu_device.vso");
                   if (file != null) {
                       String path = file.getAbsoluteFile().getParent();
                       if (logInit) {
                           log.info("Veda device library cache path: {}", path);
                       }

                       deviceNativeOps.setVedaDeviceLibFolder(path);
                   }
               } else { //graalvm
                   matchString = "linux-x86_64";
                   pPath = "linux-x86_64";
                   start = pPath.indexOf(matchString);
                   int end = pPath.lastIndexOf("/");
                   String bindingsFolder = pPath.substring(start, end);
                   File file = Loader.cacheResource(bindingsFolder + "/libnd4jcpu_device.vso");
                   if (file != null) {
                       String path = file.getAbsoluteFile().getParent();
                       if (logInit) {
                           log.info("Veda device library cache path: {}", path);
                       }

                       deviceNativeOps.setVedaDeviceLibFolder(path);
                   }
               }

            }
        } catch (java.io.IOException exception) {

        }

        if (logInit) {
            log.info("Number of threads used for linear algebra: {}", deviceNativeOps.ompGetMaxThreads());
        }
    }

    public static NativeOpsHolder getInstance() {
        return INSTANCE;
    }
}
