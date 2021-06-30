package life.genny.eventbus;

import java.lang.invoke.MethodHandles;
import org.apache.logging.log4j.Logger;
import io.vertx.core.eventbus.EventBus;


public class EventBusMock  implements EventBusInterface
{
    private static final Logger log = org.apache.logging.log4j.LogManager
            .getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());


    public EventBusMock ()
    {

    }

    public EventBusMock(EventBus eventBus)
    {

    }




}
