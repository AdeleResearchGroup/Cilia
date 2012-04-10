///*
// * Copyright Adele Team LIG
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package fr.liglab.adele.cilia.framework.scheduler.impl;
//
//import java.util.List;
//
//import org.apache.felix.ipojo.ConfigurationException;
//import org.apache.felix.ipojo.metadata.Element;
//
//
//
//public class SchedulerMetadata {
//    /**
//     *method attribute to configure handler. 
//     */
//    private final static String METHOD = "method";
//    /**
//     *method attribute to configure handler. 
//     */
//    private final static String DATA_TYPE = "data.type";
//    /**
//     * callback attribute to configure handler.
//     */
//    private final static String CALLBACK = "callback";
//    /**
//     * Default Method Name.
//     */
//    private final static String DEFAULT_METHOD = "process";
//    /**
//     * Parametred and returned data type.
//     */
//    private final String RETURN_PARAMETER_TYPE = List.class.getName();
//    /**
//     * process Method. 
//     */
//    private String method;
//
//    private String []returnDataType = new String[1];
//    /**
//     * 
//     * @param element
//     * @throws ConfigurationException
//     */
//    public SchedulerMetadata(Element element) throws ConfigurationException {
//        if (element.containsAttribute(METHOD)) {
//            method = element.getAttribute(METHOD);
//        } else if (element.containsAttribute(CALLBACK)) {
//            method = element.getAttribute(CALLBACK);
//        } else {
//            method = DEFAULT_METHOD;
//        }
//        //get dataType it allows Data and List
//
//        if (element.containsAttribute(DATA_TYPE)) {
//            returnDataType[0] = element.getAttribute(DATA_TYPE);
//        } else {
//            returnDataType[0] = RETURN_PARAMETER_TYPE;
//        }
//
//    }
//    /**
//     * get Method Name.
//     * @return
//     */
//    public String getMethod() {
//        return method;
//    }
//
//    public String []getReturnedDataType(){
//        return returnDataType;
//    }
//}

