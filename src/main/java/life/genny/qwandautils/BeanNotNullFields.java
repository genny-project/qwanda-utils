package life.genny.qwandautils;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class BeanNotNullFields extends BeanUtilsBean {
    private final Log log = LogFactory.getLog(BeanUtils.class);

//  @Override
//  public void copyProperty(Object dest, String name, Object value) {
//    if (value == null)
//      return;
//    try {
//      super.copyProperty(dest, name, value);
//    } catch (IllegalAccessException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    } catch (InvocationTargetException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    }
//  }

    @Override
    public void copyProperties(final Object dest, final Object orig)
            throws IllegalAccessException, InvocationTargetException {

        // Validate existence of the specified beans
        if (dest == null) {
            throw new IllegalArgumentException("No destination bean specified");
        }
        if (orig == null) {
            throw new IllegalArgumentException("No origin bean specified");
        }
        if (log.isDebugEnabled()) {
            log.debug("BeanUtils.copyProperties(" + dest + ", " + orig + ")");
        }

        // Copy the properties, converting as necessary
        if (orig instanceof DynaBean) {
            log.info("INSTANCE OF DYNABEAN\n\n");
            final DynaProperty[] origDescriptors = ((DynaBean) orig).getDynaClass().getDynaProperties();
            for (DynaProperty origDescriptor : origDescriptors) {
                final String name = origDescriptor.getName();
                // Need to check isReadable() for WrapDynaBean
                // (see Jira issue# BEANUTILS-61)
                if (getPropertyUtils().isReadable(orig, name)
                        && getPropertyUtils().isWriteable(dest, name)) {
                    final Object newValue = ((DynaBean) orig).get(name);
                    final Object oldValue = ((DynaBean) dest).get(name);
                    if(newValue==null)
                        copyProperty(dest, name, oldValue);
                    else
                        copyProperty(dest, name, newValue);
                }
                log.info("1 init here");
            }
        } else if (orig instanceof Map) {
            log.info("INSTANCE OF MAP\n\n");
            @SuppressWarnings("unchecked")
            final
            // Map properties are always of type <String, Object>
            Map<String, Object> propMap = (Map<String, Object>) orig;
            for (final Map.Entry<String, Object> entry : propMap.entrySet()) {
                final String name = entry.getKey();
                if (getPropertyUtils().isWriteable(dest, name)) {
                    copyProperty(dest, name, entry.getValue());
                }
            }
        } else /* if (orig is a standard JavaBean) */ {
            final PropertyDescriptor[] origDescriptors = getPropertyUtils().getPropertyDescriptors(orig);
            for (PropertyDescriptor origDescriptor : origDescriptors) {
                final String name = origDescriptor.getName();
                if ("class".equals(name)) {
                    continue; // No point in trying to set an object's class
                }
                if (getPropertyUtils().isReadable(orig, name)
                        && getPropertyUtils().isWriteable(dest, name)) {
                    try {
                        final Object newValue = getPropertyUtils().getSimpleProperty(orig, name);
                        final Object oldValue = getPropertyUtils().getSimpleProperty(dest, name);
                        if(newValue==null)
                            copyProperty(dest, name, oldValue);
                        else
                            copyProperty(dest, name, newValue);

                    } catch (final NoSuchMethodException e) {
                        // Should not happen
                    }
                }
            }
            // log.info("Java property on standard JavaBean");
        }

    }

}
