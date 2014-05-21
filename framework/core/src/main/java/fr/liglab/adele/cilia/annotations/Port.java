package fr.liglab.adele.cilia.annotations;

/**
 * User: garciai@imag.fr
 * Date: 9/24/13
 * Time: 4:45 PM
 */
public @interface Port {
    String name();

    Class dataType() default Object.class;

    String semanticType() default "*";
}
