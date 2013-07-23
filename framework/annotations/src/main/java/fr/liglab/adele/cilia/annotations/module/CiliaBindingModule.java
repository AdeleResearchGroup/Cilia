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
package fr.liglab.adele.cilia.annotations.module;

import fr.liglab.adele.cilia.annotations.*;
import fr.liglab.adele.cilia.annotations.visitors.CiliaComponentVisitor;
import fr.liglab.adele.cilia.annotations.visitors.ProcessMethodVisitor;
import org.apache.felix.ipojo.manipulator.spi.AbsBindingModule;
import org.apache.felix.ipojo.manipulator.spi.AnnotationVisitorFactory;
import org.apache.felix.ipojo.manipulator.spi.BindingContext;
import org.objectweb.asm.AnnotationVisitor;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 */
public class CiliaBindingModule extends AbsBindingModule {

    public void configure() {
    	System.out.println("Congiguring Cilia Binding Module");
        bind(Processor.class)
            .to(new AnnotationVisitorFactory() {
                public AnnotationVisitor newAnnotationVisitor(BindingContext context) {
                    return new CiliaComponentVisitor("processor",context);
                }
            });
        bind(Scheduler.class)
                .to(new AnnotationVisitorFactory() {
                    public AnnotationVisitor newAnnotationVisitor(BindingContext context) {
                        return new CiliaComponentVisitor("scheduler",context);
                    }
                });
        bind(Dispatcher.class)
                .to(new AnnotationVisitorFactory() {
                    public AnnotationVisitor newAnnotationVisitor(BindingContext context) {
                        return new CiliaComponentVisitor("dispatcher",context);
                    }
                });
        bind(Collector.class)
                .to(new AnnotationVisitorFactory() {
                    public AnnotationVisitor newAnnotationVisitor(BindingContext context) {
                        return new CiliaComponentVisitor("collector",context);
                    }
                });
        bind(Sender.class)
                .to(new AnnotationVisitorFactory() {
                    public AnnotationVisitor newAnnotationVisitor(BindingContext context) {
                        return new CiliaComponentVisitor("sender",context);
                    }
                });
        bind(Sender.class)
                .to(new AnnotationVisitorFactory() {
                    public AnnotationVisitor newAnnotationVisitor(BindingContext context) {
                        return new CiliaComponentVisitor("IOAdapter",context);
                    }
                });
        bind(ProcessData.class)
                .to(new AnnotationVisitorFactory() {
                    public AnnotationVisitor newAnnotationVisitor(BindingContext context) {
                        return new ProcessMethodVisitor(context);
                    }
                });
   }

}
