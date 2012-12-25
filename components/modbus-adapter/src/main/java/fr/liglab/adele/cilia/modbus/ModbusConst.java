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

package fr.liglab.adele.cilia.modbus;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 * 
 */
public interface ModbusConst {

	public static final String READ_HOLDING_REGISTERS = "read.holding.registers";
	public static final String READ_DISCRETE_INPUTS = "read.discrete.inputs";
	public static final String READ_INPUT_REGISTERS = "read.input.registers";
	public static final String READ_COILS = "read.coils";

	public static final String DATA_TYPE_PLAIN_TEXT = "plain-text";
	public static final String DATA_TYPE_XML = "xml";

	public static final String TAG_ROOT_RESPONSE = "ModbusData";
	public static final String TAG_VALUE = "Value";
	public static final String ATTR_REGISTER = "register";
	public static final String ATTR_BIT = "bit";

	public static final String TAG_HOLDING_REGISTERS = "HoldingRegisters";
	public static final String TAG_DISCRETE_INPUT = "DiscreteInputs";
	public static final String TAG_INPUT_REGISTERS = "InputRegisters";
	public static final String TAG_COILS = "Coils";
}
