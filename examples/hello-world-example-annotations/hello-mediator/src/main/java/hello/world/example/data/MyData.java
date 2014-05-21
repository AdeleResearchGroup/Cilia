package hello.world.example.data;

import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

/**
 * User: garciai@imag.fr
 * Date: 9/24/13
 * Time: 5:05 PM
 */
public class MyData {

    private Date creationDate;

    private Map content = new Hashtable();

    private String name;

    public MyData(String name) {
        this.name = name;
        creationDate = new Date();
    }

    public Object get(Object key) {
        return content.get(key);
    }

    public Object put(Object key, Object value) {
        return content.put(key, value);
    }

    public String getName() {
        return name;
    }

    public Date getCreationDate() {
        return creationDate;
    }
}
