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

package fr.liglab.adele.cilia.framework.data;

/*
 * Copyright 2009 OW2 Chameleon
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
import java.util.List;

import fr.liglab.adele.cilia.Data;
/**
 * Cilia Expression to analize Data
 * 
 * @author torito
 *
 */
public interface CiliaExpression {
	
   /**
	 * Evaluate and expression.
	 * @param expr expression to be evaluated.
	 * @param data data to be used in the expression evaluation.
	 * @return the expression result.
	 */
	boolean evaluateBooleanExpression(String expr, Data data);

	/**
	 * Used to remplace variables in the given expression.
	 * @param expr expression to be parsed.
	 * @param data data which will contain the variable values.
	 * @return the modified expression or variables.
	 */
	String resolveVariables(String expr, Data data);
	/**
	 * Evaluate an expression.
	 * @param expre expression to be evaluated.
	 * @param data data to be used when parsing the expression.
	 * @return the result.
	 */
	List evaluateExpression(String expre, Data data);
}
