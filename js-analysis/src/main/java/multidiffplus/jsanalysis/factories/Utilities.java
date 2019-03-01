package multidiffplus.jsanalysis.factories;

import java.util.Map;

import org.mozilla.javascript.ast.Name;

import multidiffplus.jsanalysis.abstractdomain.Address;
import multidiffplus.jsanalysis.abstractdomain.BValue;
import multidiffplus.jsanalysis.abstractdomain.Property;
import multidiffplus.jsanalysis.abstractdomain.Store;

public class Utilities {

    /**
     * Adds a property to the object and allocates the property's value on the
     * store.
     * 
     * @param prop
     *            The name of the property to add to the object.
     */
    public static Store addProp(String prop, Integer definerID, BValue propVal,
	    Map<String, Property> ext, Store store) {
	Address propAddr = Address.createBuiltinAddr(prop);
	store = store.alloc(propAddr, propVal, new Name());
	ext.put(prop, new Property(definerID, prop, propAddr));
	return store;
    }

}