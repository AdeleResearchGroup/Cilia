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

package fr.liglab.adele.cilia.runtime;

import fr.liglab.adele.cilia.exceptions.CiliaIllegalParameterException;

public interface WorkQueue {

	/**
	 * Change the piority for all threads
	 * 
	 * @param newPriority
	 * @throws CiliaIllegalParameterException 
	 */
	public void setPriority(int newPriority) throws CiliaIllegalParameterException;

	/**
	 * 
	 * @return threads priority
	 */
	public int getPriority();

	/**
	 * 
	 * @return current size of the pool of thread
	 */
	public int size();

	/**
	 * manage the size of the pool of thread ( increment , reduce )
	 * 
	 * @param newSize
	 * @return the new current size of the pool of thread
	 * @throws CiliaIllegalParameterException 
	 */
	public int size(int newSize) throws CiliaIllegalParameterException;

	/**
	 * 
	 * @return the number of job waiting to be executed
	 */
	public int sizeJobQueued();

	/**
	 * 
	 * @param job
	 *            asynchronous work executed inside the first thread ready
	 */
	public void execute(Runnable job);

	/**
	 * Return the number max of job queued
	 * 
	 * @return
	 */
	public int sizeMaxjobQueued();

	/**
	 * Reset the value max
	 */
	public void resetMaxJobQueued();

	public void start();

	public void stop();

}