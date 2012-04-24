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
package fr.liglab.adele.cilia.builder.impl;

import java.util.Hashtable;
import java.util.StringTokenizer;

import fr.liglab.adele.cilia.builder.Architecture;
import fr.liglab.adele.cilia.builder.Binder;
import fr.liglab.adele.cilia.builder.BinderToSetter;
import fr.liglab.adele.cilia.exceptions.BuilderConfigurationException;

/**
 *
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project Team</a>
 *
 */
public class BinderImpl implements Binder {

	private Architecture architecture;

	private String to;
	
	private BindingInfo toInfo;
	
	private BindingInfo fromInfo;

	private String from;

	private String using;

	protected Hashtable properties;

	

	public BinderImpl(Architecture arch) {
		architecture = arch;
	}
	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.builder.From#to(java.lang.String)
	 */
	@Override
	public Binder to(String to) throws  BuilderConfigurationException {
		if (to == null) {
			throw new BuilderConfigurationException ("to parameter must not be null");
		}
		this.to = to;
		toInfo = new BindingInfo(to);
		return this;
	}

	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.builder.Binder#from(java.lang.String)
	 */
	@Override
	public BinderToSetter from(String from) throws BuilderConfigurationException {
		if (from == null) {
			throw new BuilderConfigurationException ("<from> parameter in bind must not be null");
		}
		fromInfo = new BindingInfo(from);
		this.from = from;
		return this;
	}

	public String getFromMediator(){
		return fromInfo.mediator;
	}
	
	public String getFromPort(){
		return fromInfo.port;
	}
	
	public String getToMediator(){
		return toInfo.mediator;
	}
	
	public String getToPort(){
		return toInfo.port;
	}
	
	
	/* (non-Javadoc)
	 * @see fr.liglab.adele.cilia.builder.To#using(java.lang.String)
	 */
	@Override
	public Binder using(String using) {
		this.using = using;
		return this;
	}
	
	private class BindingInfo {
		String mediator;
		String port;
		
		public BindingInfo(String info) throws BuilderConfigurationException{
			String splittedinfo[] = split(info,":");
			if(splittedinfo.length !=2) {
				throw new BuilderConfigurationException("Unexpected configuration in bind: It must be <mediatorId>:<port>");
			}
			mediator = splittedinfo[0];
			port = splittedinfo[1];
		}
		
		private String[] split(String toSplit, String separator) {
			StringTokenizer tokenizer = new StringTokenizer(toSplit, separator);
			String[] result = new String[tokenizer.countTokens()];
			int index = 0;
			while (tokenizer.hasMoreElements()) {
				result[index] = tokenizer.nextToken().trim();
				index++;
			}
			return result;
		}
	}
	
	public String getUsing() {
		return using;
	}
	public Architecture configure(Hashtable props) {
		if (props != null) {
			properties.putAll(props);
		}
		return architecture;
	}
		
	protected Hashtable getConfiguration() {
		return properties;
	}

}
