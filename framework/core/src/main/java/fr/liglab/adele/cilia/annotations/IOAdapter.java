package fr.liglab.adele.cilia.annotations;

import fr.liglab.adele.cilia.util.Const;

/**
 * User: torito
 * Date: 7/2/13
 * Time: 9:34 AM
 */
public @interface IOAdapter {
    String name();

    String namespace() default Const.CILIA_NAMESPACE;

    Port in_port();

    Port out_port();
}

