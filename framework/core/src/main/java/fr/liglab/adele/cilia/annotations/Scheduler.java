package fr.liglab.adele.cilia.annotations;

import fr.liglab.adele.cilia.util.Const;

/**
 * User: torito
 * Date: 7/1/13
 * Time: 6:14 PM
 */
public @interface Scheduler {
    String name();

    String namespace() default Const.CILIA_NAMESPACE;

}
