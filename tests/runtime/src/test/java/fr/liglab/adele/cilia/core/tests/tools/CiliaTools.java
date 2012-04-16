/*
 * Copyright Adele Team LIG
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.liglab.adele.cilia.core.tests.tools;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 *
 */
public final class CiliaTools {
	
	private static final double KILOBYTE = 1024D ;
	
	public static long getMemory(){
		long memory;
		Runtime runtime = Runtime.getRuntime();
		// Run the garbage collector
		runtime.gc();
		runtime.gc();
		runtime.gc();
		runtime.gc();
		// Calculate the used memory
		memory = runtime.totalMemory() - runtime.freeMemory();
		runtime.gc();
		runtime.gc();
		runtime.gc();
		runtime.gc();
		//System.out.println("Used memory is bytes: " + memory);
		//System.out.println("Used memory is KiloBytes: "+ bytesToKilobytes(memory));
		return memory;
	}
	
	public static double bytesToKilobytes(long bytes) {
		return bytes / KILOBYTE;
	}

}
