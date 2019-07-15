import java.time.ZoneId
import java.util.TimeZone

import com.google.inject.AbstractModule

class Module extends AbstractModule{
    override def configure(): Unit = {
        bind(classOf[TimeZone]).toInstance(TimeZone.getDefault)
        bind(classOf[ZoneId]).toInstance(ZoneId.of(TimeZone.getDefault.getID))
    }
}
