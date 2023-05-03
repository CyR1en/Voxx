module Project.Voxx.voxx.commons.main {
    requires org.jetbrains.annotations;
    requires static org.apache.logging.log4j;
    requires org.json;

    exports com.cyr1en.voxx.commons.esal;
    exports com.cyr1en.voxx.commons.esal.events;
    exports com.cyr1en.voxx.commons.esal.events.server;
    exports com.cyr1en.voxx.commons.esal.events.annotation;

    exports com.cyr1en.voxx.commons.model;
    exports com.cyr1en.voxx.commons.protocol;
}